package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.SwitchBranchDto;
import com.bccs.gatewaymanager.dto.ConditionRuleDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.dto.FlagRuleDto;
import com.bccs.gatewaymanager.dto.StepTraceDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.MappingTargetContext;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.FlagRegistryCache;
import com.bccs.gatewaymanager.service.UpstreamRegistryCache;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Bo may thuc thi composition tai request-time - thay the hoan toan
 * KrakendConfigGenerator (sinh krakend.json tinh) va moi engine ben thu 3
 * (KrakenD/Gravitee). Doc EndpointConfig+Steps+Mappings da duoc
 * EndpointRegistryCache nap san trong bo nho, thuc thi TUNG step theo dung
 * thu tu (hien tai luon TUAN TU trong code - xem ghi chu o handle()), goi
 * upstream qua UpstreamHttpExecutor (co cache/circuit-breaker/retry/bulkhead),
 * roi tra ve ket qua cuoi cung.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeOrchestratorEngine {

    private final UpstreamHttpExecutor upstreamHttpExecutor;
    private final UpstreamRegistryCache upstreamRegistryCache;
    private final ObjectMapper objectMapper;
    private final ExecutorService parallelStepExecutor;
    private final FlagRuleEvaluator flagRuleEvaluator;
    private final FlagRegistryCache flagRegistryCache;

    /**
     * Thuc thi 1 EndpointConfig cho 1 request that - KHONG co override cờ
     * (request that tu DynamicDispatcherController; cờ doc tu FlagRegistryCache).
     */
    public JsonNode handle(EndpointResponseDto config, Map<String, String> pathVariables,
                            Map<String, String[]> queryParams, String rawRequestBody) {
        return handle(config, pathVariables, queryParams, rawRequestBody, null);
    }

    /**
     * Thuc thi 1 EndpointConfig cho 1 request that.
     *
     * V5 - RULE CỜ: DAU HAM, truoc khi bat dau bat ky step nao, danh gia
     * flagRules cua endpoint (FlagRuleEvaluator - trong nhom AND, giua nhom OR,
     * priority tang dan, khop dau tien la dung):
     *  - START_STEP -> chuoi step chay TU startStepOrder thay vi tu step nho nhat;
     *  - BLOCK -> FlagRuleEvaluator tu throw BusinessException (khong goi
     *    upstream nao, loi di qua dung luong error/audit co san).
     * Khong rule khop / flagRules rong / flagOverrides=null -> hanh vi cu
     * 100% (chay tu step nho nhat) - an toan cho moi endpoint hien co.
     * flagOverrides KHAC null chi xay ra o "Thu ngay/Thu nhanh" (EndpointTryService
     * - mo phong theo cờ, KHONG anh huong cache cờ tren Data Plane).
     *
     * Non-sequential: moi step DOC LAP, LUON chay het (khong lien quan gi toi
     * re nhanh - xem executeSequentialChain()), roi gop ket qua qua
     * assembleFinalResponse(). Mac dinh (config.parallelExecution()=false) van
     * la 1 vong lap Java tuan tu nhu truoc, chi khac o cho KHONG doc con tro/
     * dieu kien - giu nguyen 100% hanh vi cu cho moi endpoint hien co. Khi
     * parallelExecution=true, cac step duoc submit CHAY THAT SU SONG SONG qua
     * executeStepsInParallel() (xem javadoc rieng ve danh doi side-effect).
     *
     * Sequential (P1-5): xem executeSequentialChain() - moi step co the tu
     * quyet dinh step TIEP THEO se chay (re nhanh that) thay vi luon la
     * "stepOrder ke tiep co dinh".
     */
    public JsonNode handle(EndpointResponseDto config, Map<String, String> pathVariables,
                            Map<String, String[]> queryParams, String rawRequestBody,
                            Map<String, Boolean> flagOverrides) {
        return handle(config, pathVariables, queryParams, rawRequestBody, flagOverrides, Map.of());
    }

    /**
     * {@code headers} = header cua CHINH client (key bat ky hoa thuong, se duoc ha chu) -
     * chi dung cho dieu kien re nhanh nguon HEADER.
     */
    public JsonNode handle(EndpointResponseDto config, Map<String, String> pathVariables,
                            Map<String, String[]> queryParams, String rawRequestBody,
                            Map<String, Boolean> flagOverrides, Map<String, String> headers) {
        // Rule cờ danh gia TRUOC TAT CA - ca non-sequential lam (khong dung rule
        // - validate luc luu chi cho sequential=true - nhung BLOCK van an toan neu
        // du lieu cu/import tay lot qua validate).
        FlagRuleEvaluator.Result flagResult = flagRuleEvaluator.evaluate(config, flagOverrides);
        TraceCollector.recordFlagRuleMatch(config.name(), flagResult.matchedRule());

        JsonNode requestBodyNode = parseBodyOrNull(rawRequestBody);
        ExecutionContext ctx = new ExecutionContext(pathVariables, queryParams, requestBodyNode, lowerCaseKeys(headers));

        List<BackendStepDto> orderedSteps = config.steps().stream()
                .sorted((a, b) -> Integer.compare(a.stepOrder(), b.stepOrder()))
                .toList();

        if (!config.sequential()) {
            if (config.parallelExecution()) {
                executeStepsInParallel(config, orderedSteps, ctx);
            } else {
                for (BackendStepDto step : orderedSteps) {
                    JsonNode transformed = executeStep(config, step, ctx);
                    ctx.putStepResult(step.stepOrder(), transformed);
                }
            }
            return applyResponseMappings(assembleFinalResponse(orderedSteps, ctx), config, ctx);
        }

        // Bu tru/rollback nghiep vu (saga best-effort, muc 6) - CHI ap dung nhanh
        // sequential=true (khong dung cho nhanh non-sequential o tren, hoan toan
        // KHONG doi). Boc o DAY (handle(), khong phai ben trong
        // executeSequentialChain()) de giu method do nguyen ven, giam rui ro tuong
        // tac - ctx duoc truyen THEO THAM CHIEU nen moi putStepResult() da chay
        // ben trong executeSequentialChain() truoc khi no throw van "nhin thay
        // duoc" o day. Chi kich hoat khi loi THOAT HAN ra ngoai executeSequentialChain()
        // - nghia la onErrorStepOrder (neu co) da co co hoi xu ly truoc do (fallback
        // thanh cong thi ham tra ve binh thuong, khong co exception nao de bat o
        // day, runCompensations() khong bao gio duoc goi).
        // ResponseMapping ap NGOAI try/catch - loi cua chinh viec ghep field (cau
        // hinh sai) KHONG duoc kich hoat bu tru/rollback nghiep vu (runCompensations
        // chi danh cho loi goi upstream cua chuoi).
        JsonNode chainResult;
        try {
            chainResult = executeSequentialChain(config, orderedSteps, ctx, flagResult.initialStepOrder());
        } catch (RuntimeException e) {
            runCompensations(config, orderedSteps, ctx);
            throw e;
        }
        return applyResponseMappings(chainResult, config, ctx);
    }

    /**
     * Ap tinh nang ResponseMapping (ghép field tu bat ky step nao vao RESPONSE
     * CUOI) - goi DUY NHAT o cuoi handle() cho CA 2 che do sequential/non-sequential,
     * SAU khi assembleFinalResponse()/executeSequentialChain() da tra ket qua tong
     * hop. Null/rong -> tra nguyen ket qua (100% endpoint cu khong doi gi).
     *
     * mode:
     *  - MERGE (mac dinh): giu nguyen ket qua tong hop, chi BO SUNG cac field khai
     *    bao (targetField trung ten bi mapping sau ghi de theo mappingOrder).
     *  - REPLACE: BO ket qua tong hop - response cuoi = DUNG VA CHI cac field khai
     *    bao (giai case "giu envelope BCCS + loc data": step target=null giu raw,
     *    khai mapping code/message/... + data.xxx).
     *
     * Nguon doc: ctx.stepResults - gia tri DA boc step.target + DA transform
     * (nhat quan voi FieldMapping STEP_RESPONSE). Field nguon thieu/step khong chay
     * (re nhanh bo qua) -> log.warn + skip (fail-open, KHONG chặn request - khac
     * validate luc luu chi chan sourceStepOrder khong ton tai voi bat ky step nao).
     */
    private JsonNode applyResponseMappings(JsonNode finalResult, EndpointResponseDto config, ExecutionContext ctx) {
        com.bccs.gatewaymanager.dto.ResponseMappingSpecDto spec = config.responseMapping();
        if (com.bccs.gatewaymanager.dto.ResponseMappingSpecDto.isEmpty(spec)) {
            return finalResult;
        }
        boolean replace = com.bccs.gatewaymanager.dto.ResponseMappingSpecDto.MODE_REPLACE.equals(spec.mode());
        // LUON lam ban sao (deep copy) truoc khi ghi - KHONG mutat thang finalResult:
        // voi sequential, finalResult THUONG CHINH LA cung object JsonNode voi
        // ctx.stepResults cua step cuoi (putStepResult khong copy) - ghi de mapping
        // len object do se lam BIEU DIEN du lieu nguon cua CAC MAPPING SAU (duoc doc
        // tu ctx.stepResults) bi doi theo, ket qua sai don le nhan.
        ObjectNode out = replace ? objectMapper.createObjectNode()
                : (finalResult instanceof ObjectNode obj ? obj.deepCopy() : objectMapper.createObjectNode());
        if (!replace && !(finalResult instanceof ObjectNode)) {
            // Ket qua tong hop khong phai object (hien khang dinh chi co the la
            // ObjectNode/mang tu assembleFinalResponse) - REPLACE moi co y nghia,
            // MERGE khong co nen "de len" object rong de gan field khai bao.
            log.warn("Endpoint '{}': ResponseMapping mode=MERGE nhung ket qua tong hop khong phai JSON object - bo qua ket qua goc, tra cac field khai bao.", config.name());
        }
        List<com.bccs.gatewaymanager.dto.ResponseFieldMappingDto> ordered = spec.mappings().stream()
                .sorted(java.util.Comparator.comparingInt(
                        com.bccs.gatewaymanager.dto.ResponseFieldMappingDto::mappingOrder))
                .toList();
        for (com.bccs.gatewaymanager.dto.ResponseFieldMappingDto m : ordered) {
            JsonNode source = ctx.getStepResult(m.sourceStepOrder());
            JsonNode value = JsonPathUtil.getByDotPath(source, m.sourceField());
            if (value == null || value.isNull()) {
                log.warn("Endpoint '{}': ResponseMapping (step {}, field '{}') bo qua - gia tri nguon khong ton tai (step chua chay do re nhanh, hoac field thuc te khong co).",
                        config.name(), m.sourceStepOrder(), m.sourceField());
                continue;
            }
            if (!JsonPathUtil.setByDotPath(out, m.targetField(), value)) {
                log.warn("Endpoint '{}': ResponseMapping (target '{}') khong ghi duoc - node dich khong dung kieu (vd ghi index mang vuot gioi han).",
                        config.name(), m.targetField());
            }
        }
        return out;
    }

    /**
     * Index danh sach step theo stepOrder - dung chung cho ca thuc thi tuan tu
     * (executeSequentialChain) va bu tru (runCompensations), tranh lap logic
     * Collectors.toMap o 2 noi.
     */
    private static Map<Integer, BackendStepDto> indexStepsByOrder(List<BackendStepDto> orderedSteps) {
        return orderedSteps.stream().collect(Collectors.toMap(BackendStepDto::stepOrder, s -> s));
    }

    /**
     * Thuc thi chuoi step TUAN TU theo CON TRO (khong phai vong lap for co
     * dinh) - bat dau tu initialStepOrder (V5: step bat dau do Rule cờ chon;
     * null = step co stepOrder NHO NHAT - hanh vi cu), sau moi step tu hoi
     * "step tiep theo la gi" qua determineNextStepOrder(). Step KHONG khai
     * bao dieu kien (conditionOperator=null - dung cho 100% endpoint tu
     * TRUOC P1-5) tai tao DUNG hanh vi cu: van chay het tat ca step theo
     * dung thu tu stepOrder tang dan, khong bo sot/doi thu tu gi ca.
     *
     * Ket qua tra ve = response cua STEP CUOI CUNG THUC SU chay trong lan
     * nay - KHAC voi truoc day (luon la step co stepOrder lon nhat), vi voi
     * re nhanh, chuoi co the ket thuc o BAT KY step nao tuy theo dieu kien.
     */
    private JsonNode executeSequentialChain(EndpointResponseDto config, List<BackendStepDto> orderedSteps,
                                            ExecutionContext ctx, Integer initialStepOrder) {
        Map<Integer, BackendStepDto> stepsByOrder = indexStepsByOrder(orderedSteps);
        List<Integer> allOrders = orderedSteps.stream().map(BackendStepDto::stepOrder).sorted().toList();
        // Tap hop stepOrder la DICH cua it nhat 1 "buoc nhay dac biet" - re nhanh
        // (nextStepOrderIfTrue/nextStepOrderIfFalse) HOAC fallback loi (onErrorStepOrder) -
        // dung de xu ly dung "nhanh la"/"step fallback" trong determineNextStepOrder() (xem
        // javadoc o do): ca 2 loai buoc nhay nay deu KHONG phai "chay tuan tu tu nhien", nen
        // step dich cua chung, neu ban than khong co dieu kien rieng, phai DUNG LAI tai do
        // thay vi am tham roi tiep sang stepOrder lon hon ke tiep.
        Set<Integer> branchTargetOrders = new HashSet<>();
        for (BackendStepDto s : orderedSteps) {
            if (s.conditionOperator() != null) {
                if (s.nextStepOrderIfTrue() != null) {
                    branchTargetOrders.add(s.nextStepOrderIfTrue());
                }
                if (s.nextStepOrderIfFalse() != null) {
                    branchTargetOrders.add(s.nextStepOrderIfFalse());
                }
            }
            if (s.switchBranches() != null && !s.switchBranches().isEmpty()) {
                for (SwitchBranchDto branch : s.switchBranches()) {
                    if (branch.nextStepOrder() != null) {
                        branchTargetOrders.add(branch.nextStepOrder());
                    }
                }
                if (s.nextStepOrderIfFalse() != null) {
                    branchTargetOrders.add(s.nextStepOrderIfFalse());
                }
            }
            if (s.onErrorStepOrder() != null) {
                branchTargetOrders.add(s.onErrorStepOrder());
            }
        }

        // V5: step bat dau = step do Rule cờ chon (initialStepOrder); null = step
        // nho nhat (hanh vi cu 100%). initialStepOrder da duoc validate ton tai luc
        // luu, nhung du phong null-safe cho du lieu cu/import tay.
        Integer currentOrder = initialStepOrder != null && allOrders.contains(initialStepOrder)
                ? initialStepOrder
                : (allOrders.isEmpty() ? null : allOrders.get(0));
        // Chong lap vo han: 1 step KHONG DUOC chay lai trong CUNG 1 request - neu
        // nextStepOrderIfTrue/False vo tinh (hoac co chu dich sai) tao thanh vong
        // lap, phai fail-fast tai day thay vi treo request/goi Upstream vo han lan.
        Set<Integer> executedOrders = new HashSet<>();
        JsonNode lastResult = null;

        while (currentOrder != null) {
            if (!executedOrders.add(currentOrder)) {
                throw new BusinessException("GW-BRANCH-LOOP", "Endpoint '" + config.name()
                        + "': phat hien vong lap re nhanh khi thuc thi that (step " + currentOrder
                        + " se bi goi lai) - kiem tra lai cau hinh nextStepOrderIfTrue/nextStepOrderIfFalse.");
            }
            BackendStepDto step = stepsByOrder.get(currentOrder);
            if (step == null) {
                throw new BusinessException("GW-BRANCH-TARGET-404", "Endpoint '" + config.name()
                        + "': dieu kien re nhanh tro toi step " + currentOrder + " khong ton tai.");
            }
            // "Wave" song song trong chuoi sequential (xem BackendStep.parallelGroup) - step
            // nay la 1 THANH VIEN cua 1 nhom chay dong thoi. Gom TAT CA thanh vien cung nhom
            // (EndpointService da validate: stepOrder cua 1 nhom LIEN TIEP, chi vao duoc qua
            // tien trinh tu nhien, khong co conditionOperator/onErrorStepOrder rieng), chay
            // qua executeStepsInParallel() **dung LAI nguyen ven** ham cua muc 4 (khong viet
            // logic song song moi) - doi CA nhom xong (throw loi dau tien neu co), roi nhay
            // thang toi natural-next SAU stepOrder LON NHAT trong nhom, dung CHINH XAC cong
            // thuc natural-next da co ben duoi (determineNextStepOrder()) thay vi logic moi.
            // Neu chuoi KET THUC ngay sau wave (khong con step nao), lastResult = ket qua GOP
            // cua ca nhom qua assembleFinalResponse() - dung lai code cua endpoint non-sequential,
            // nhat quan hanh vi giua 2 noi.
            if (step.parallelGroup() != null) {
                List<BackendStepDto> waveSteps = orderedSteps.stream()
                        .filter(s -> step.parallelGroup().equals(s.parallelGroup()))
                        .toList();
                executeStepsInParallel(config, waveSteps, ctx);
                lastResult = assembleFinalResponse(waveSteps, ctx);
                int maxOrderInWave = waveSteps.stream().mapToInt(BackendStepDto::stepOrder).max().orElse(currentOrder);
                waveSteps.forEach(member -> executedOrders.add(member.stepOrder()));
                currentOrder = allOrders.stream().filter(o -> o > maxOrderInWave).min(Integer::compareTo).orElse(null);
                continue;
            }
            // Fallback khi step LOI (onErrorStepOrder, doc lap voi conditionOperator o tren) -
            // step KHONG khai bao (onErrorStepOrder=null) throw ngay y HET truoc day (bat
            // buoc, de moi endpoint hien co khong doi hanh vi 1 chut nao). Chi bat loi cua
            // CHINH executeStep() nay - GW-BRANCH-LOOP/GW-BRANCH-TARGET-404 o tren van throw
            // thang (do la loi cau hinh, khong phai loi goi upstream, khong nen bi fallback
            // am tham nuot). Hop that/loi da duoc UpstreamHttpExecutor.call() tu ghi audit
            // (finally) TRUOC KHI exception toi day, khong can log lai o day.
            try {
                lastResult = executeStep(config, step, ctx);
            } catch (RuntimeException e) {
                if (step.onErrorStepOrder() == null) {
                    throw e;
                }
                log.warn("Endpoint '{}': step '{}' (order={}) loi, fallback sang step {} theo onErrorStepOrder: {}",
                        config.name(), step.name(), currentOrder, step.onErrorStepOrder(), e.getMessage());
                currentOrder = step.onErrorStepOrder();
                continue;
            }
            ctx.putStepResult(currentOrder, lastResult);
            currentOrder = determineNextStepOrder(step, ctx, allOrders, branchTargetOrders);
        }
        return lastResult == null ? objectMapper.createObjectNode() : lastResult;
    }

    /**
     * Chay N step THAT SU SONG SONG qua parallelStepExecutor (muc 4, xem
     * ParallelExecutionConfig) - dung o **2 noi**: (1) toan bo step cua 1 endpoint
     * KHONG sequential khi EndpointConfig.parallelExecution=true (xem handle()); (2) 1
     * "wave" (nhom step co cung parallelGroup) NAM TRONG 1 chuoi sequential=true, xen
     * giua cac step tuan tu khac (xem executeSequentialChain()) - ca 2 noi goi TRUYEN
     * VAO danh sach step CAN chay song song (khong nhat thiet la orderedSteps day du),
     * method nay khong quan tam ngu canh goi. Submit TAT CA step trong danh sach cung
     * luc, roi cho TAT CA future hoan thanh (vong for lan luot future.get(), khong phai
     * cho tung cai roi moi submit cai tiep) truoc khi tra ve, dam bao khong con thread
     * nao dang chay ngam sau khi method nay return. ctx.putStepResult() an toan goi
     * dong thoi tu nhieu thread (xem javadoc ExecutionContext.stepResults).
     *
     * DANH DOI PHAI BIET (da canh bao ro tren UI luc cau hinh - xem field-note cua
     * parallelExecution o endpoint-form/endpoint-canvas): khac han vong lap tuan tu
     * (loi step nao dung NGAY tai do, step SAU khong bao gio chay), o day TAT CA step
     * DA duoc submit truoc khi biet step nao loi - step co side-effect that (POST/PUT/
     * DELETE) co the DA CHAY XONG truoc khi loi cua step khac duoc phat hien. Chi nen
     * bat parallelExecution cho endpoint co step khong side-effect quan trong/idempotent.
     *
     * Loi: doi TAT CA future xong (khong bo cuoc som khi gap loi dau tien) roi moi
     * throw loi DAU TIEN gap phai (theo thu tu step trong orderedSteps, khong phai
     * thu tu hoan thanh thuc te - de hanh vi loi de doan hon, nhat quan voi thu tu
     * hien tren UI) - cac loi khac (neu co nhieu step cung loi) chi duoc log WARN.
     *
     * MDC ("requestId", dung de noi hop audit log voi dung request - xem
     * UpstreamHttpExecutor.call()) la ThreadLocal, KHONG tu dong lan sang thread cua
     * parallelStepExecutor - neu khong copy tay, hop cua MOI step chay trong ham nay
     * se bi ghi audit VOI requestId=null (mat hoan toan khoi "Tra cuu Log" khi tim
     * theo dung request) - bug THAT phat hien khi verify song tinh nang wave (chi
     * thay 1/3 hop trong audit, dung ra phai co 3). Fix: chup MDC cua thread GOI
     * (request thread) 1 LAN truoc khi submit, roi set lai dung ban chup do TRONG
     * MOI task truoc khi chay + phuc hoi MDC cu cua worker thread trong finally (tranh
     * ro ri context sang task KHAC tai su dung CUNG thread cua pool sau nay).
     *
     * TraceCollector ("Thu nhanh" - xem EndpointTryService) can y HET fix tren:
     * cung la ThreadLocal, cung KHONG tu dong lan sang worker thread. Khac MDC
     * (moi thread giu 1 ban String rieng), o day TAT CA worker thread duoc gan
     * CHUNG 1 list (khong copy) de cac step song song cung ghi duoc vao 1 ket
     * qua duy nhat cho 1 lan "Thu nhanh".
     */
    private void executeStepsInParallel(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<String, String> callerMdcContext = MDC.getCopyOfContextMap();
        List<StepTraceDto> callerTrace = TraceCollector.current();
        List<Future<?>> futures = orderedSteps.stream()
                .<Future<?>>map(step -> parallelStepExecutor.submit(() -> {
                    Map<String, String> workerMdcContext = MDC.getCopyOfContextMap();
                    List<StepTraceDto> workerTrace = TraceCollector.current();
                    if (callerMdcContext != null) {
                        MDC.setContextMap(callerMdcContext);
                    }
                    if (callerTrace != null) {
                        TraceCollector.attach(callerTrace);
                    }
                    try {
                        JsonNode transformed = executeStep(config, step, ctx);
                        ctx.putStepResult(step.stepOrder(), transformed);
                    } finally {
                        if (workerMdcContext != null) {
                            MDC.setContextMap(workerMdcContext);
                        } else {
                            MDC.clear();
                        }
                        TraceCollector.attach(workerTrace);
                    }
                }))
                .toList();

        RuntimeException firstError = null;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                RuntimeException wrapped = (cause instanceof RuntimeException re) ? re
                        : new BusinessException("GW-PARALLEL-STEP-ERROR", "Endpoint '" + config.name()
                                + "': loi khi thuc thi step song song: " + cause.getMessage());
                if (firstError == null) {
                    firstError = wrapped;
                } else {
                    log.warn("Endpoint '{}': step song song loi them (sau loi dau tien '{}'): {}",
                            config.name(), firstError.getMessage(), wrapped.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("GW-PARALLEL-INTERRUPTED",
                        "Endpoint '" + config.name() + "': thuc thi step song song bi ngat.");
            }
        }
        if (firstError != null) {
            throw firstError;
        }
    }

    /**
     * Step tiep theo se chay - null = ket thuc chuoi tai day (ket qua step vua
     * chay la response cuoi cung).
     */
    private Integer determineNextStepOrder(BackendStepDto step, ExecutionContext ctx, List<Integer> allOrders, Set<Integer> branchTargetOrders) {
        if (step.switchBranches() != null && !step.switchBranches().isEmpty()) {
            return evaluateSwitch(step, ctx);
        }
        if (step.conditionOperator() == null) {
            if (branchTargetOrders.contains(step.stepOrder())) {
                // Step nay LA DICH cua 1 nhanh re (co step khac tro toi qua
                // nextStepOrderIfTrue/nextStepOrderIfFalse) nhung BAN THAN no khong
                // khai bao dieu kien rieng -> day la 1 "nhanh la" (branch leaf), PHAI
                // DUNG LAI tai day. Neu ap dung "next tu nhien theo stepOrder tang dan"
                // (nhu ben duoi) cho ca truong hop nay, 2 nhanh re LOAI TRU LAN NHAU se
                // bi CHAY GOP: sau khi vao dung 1 nhanh, engine se tu dong chay tiep
                // sang stepOrder lon hon ke tiep (chinh la nhanh CON LAI) va tra ve
                // nham response cua nhanh do thay vi nhanh vua duoc chon dung dieu kien
                // (bug phat hien khi verify "Numeric Branch Demo" - ca 2 gia tri thoa
                // dieu kien <=3 lan >3 deu bi tra ve ket qua cua nhanh >3).
                return null;
            }
            // Khong khai bao dieu kien VA khong phai dich cua bat ky nhanh re nao: day
            // la endpoint THUAN TUY SEQUENTIAL (khong dung P1-5) - next TU NHIEN la
            // stepOrder NHO NHAT con lai LON HON step hien tai (khong cung "+1", khong
            // doi hoi stepOrder lien tuc) - day CHINH LA cach tai tao dung 100% hanh vi
            // cu (chay het moi step theo dung thu tu stepOrder tang dan).
            return allOrders.stream().filter(o -> o > step.stepOrder()).min(Integer::compareTo).orElse(null);
        }
        boolean matched = evaluateCondition(step, ctx);
        if (!matched && hasFalseError(step)) {
            throwFalseError(step);
        }
        return matched ? step.nextStepOrderIfTrue() : step.nextStepOrderIfFalse();
    }

    /**
     * So sanh 1 field (STEP_RESPONSE/REQUEST_BODY/SYSTEM_FLAG) voi conditionExpectedValue.
     * Tham chieu toi 1 step CHUA TUNG chay (vi 1 nhanh re khac da di - hoan
     * toan hop le trong 1 do thi co dieu kien) tra ve null AN TOAN qua
     * JsonPathUtil.getByDotPath(null, ...) - KHONG throw, coi nhu "khong ton tai".
     *
     * SYSTEM_FLAG (V5): conditionSourceField = MA CỜ (khong phai dot-path);
     * doc gia tri tu cờ he thong (FlagRegistryCache - sync tu CP), KHONG tu
     * request/response nao. Toan tu hop le: EQUALS voi "true"/"false"
     * (validate luc luu); de phong du lieu khac, so sanh theo boolean.
     *
     * SYSTEM_FLAG + conditionFlagGroups (V5b, theo "vande"): dieu kien NHIEU co
     * NHIEU NHOM - AND trong nhom, OR giua nhom (mirror Rule FlagRuleDto):
     * re nhanh DUNG neu CO IT NHAT 1 nhom thoa. Khi co groups, bo qua dieu
     * kien 1 co don le. Cờ mat khoi cache = false (fail-safe giong get()).
     *
     * conditionGroups (V6): TONG QUAT HOA conditionFlagGroups sang cho du lieu
     * REQUEST_BODY/STEP_RESPONSE thuc (khong chi SYSTEM_FLAG) - uu tien danh gia
     * CAO NHAT, truoc ca SYSTEM_FLAG: khac null/rong -> bo qua HOAN TOAN dieu
     * kien 1 co don le VA nhanh SYSTEM_FLAG/conditionFlagGroups rieng, du step
     * khai bao gi o do. Xem evaluateRuleGroups()/evaluateSingleRule().
     */
    private boolean evaluateCondition(BackendStepDto step, ExecutionContext ctx) {
        if (step.conditionGroups() != null && !step.conditionGroups().isEmpty()) {
            return evaluateRuleGroups(step, step.conditionGroups(), ctx);
        }
        if (step.conditionSourceType() == FieldMappingSourceType.SYSTEM_FLAG) {
            if (step.conditionFlagGroups() != null && !step.conditionFlagGroups().isEmpty()) {
                for (List<FlagRuleDto.FlagConditionDto> group : step.conditionFlagGroups()) {
                    if (group == null || group.isEmpty()) {
                        continue;
                    }
                    boolean allTrue = true;
                    for (FlagRuleDto.FlagConditionDto cond : group) {
                        boolean currentValue = flagRegistryCache.get(cond.flagCode());
                        if (currentValue != cond.expected()) {
                            allTrue = false;
                            break;
                        }
                    }
                    if (allTrue) {
                        return true;
                    }
                }
                return false;
            }
            boolean currentValue = flagRegistryCache.get(step.conditionSourceField());
            boolean expected = !"false".equalsIgnoreCase(step.conditionExpectedValue());
            return currentValue == expected;
        }
        String label = "Step '" + step.name() + "'";
        return evaluateSingleRule(label, step.stepOrder(), step.conditionSourceType(), step.conditionSourceField(),
                step.conditionSourceStepOrder(), step.conditionOperator(), step.conditionExpectedValue(), ctx, null);
    }

    /**
     * V6 - Dieu kien NHIEU NGUON (REQUEST_BODY/STEP_RESPONSE) NHIEU NHOM - AND
     * trong nhom, OR giua nhom (mirror dung ngu nghia SYSTEM_FLAG+conditionFlagGroups
     * o evaluateCondition(), chi khac moi la MOI dieu kien la 1 {@link ConditionRuleDto}
     * DAY DU nguon rieng thay vi flagCode+expected - nen 1 nhom co the tron
     * REQUEST_BODY va STEP_RESPONSE). Nhom rong/null bi bo qua (khong co gi de
     * danh gia, khong tinh la thoa/khong thoa).
     */
    /** Rẽ nhiều nhánh: nhanh DAU TIEN khop duoc chon, khong nhanh nao khop -> nextStepOrderIfFalse. */
    private Integer evaluateSwitch(BackendStepDto step, ExecutionContext ctx) {
        List<SwitchBranchDto> branches = step.switchBranches();
        for (int i = 0; i < branches.size(); i++) {
            SwitchBranchDto branch = branches.get(i);
            if (evaluateRuleGroups(step, branch.groups(), ctx)) {
                TraceCollector.recordCondition(step.stepOrder(), "Step '" + step.name() + "': nhanh #" + (i + 1)
                        + " khop -> step " + branch.nextStepOrder());
                return branch.nextStepOrder();
            }
        }
        if (hasFalseError(step)) {
            throwFalseError(step);
        }
        TraceCollector.recordCondition(step.stepOrder(), "Step '" + step.name() + "': khong nhanh nao khop -> step "
                + step.nextStepOrderIfFalse());
        return step.nextStepOrderIfFalse();
    }

    private static boolean hasFalseError(BackendStepDto step) {
        return step.falseErrorCode() != null && !step.falseErrorCode().isBlank();
    }

    /** Nhanh SAI duoc cau hinh la BAO LOI: dung endpoint va tra ve ma/thong diep da khai bao. */
    private void throwFalseError(BackendStepDto step) {
        TraceCollector.recordCondition(step.stepOrder(), "Step '" + step.name() + "': khong khop -> BAO LOI "
                + step.falseErrorCode());
        throw new BusinessException(step.falseErrorCode(), step.falseErrorMessage());
    }

    private boolean evaluateRuleGroups(BackendStepDto step, List<List<ConditionRuleDto>> groups, ExecutionContext ctx) {
        for (int gi = 0; gi < groups.size(); gi++) {
            List<ConditionRuleDto> group = groups.get(gi);
            if (group == null || group.isEmpty()) {
                continue;
            }
            boolean allTrue = true;
            for (int ci = 0; ci < group.size(); ci++) {
                ConditionRuleDto rule = group.get(ci);
                String label = "Step '" + step.name() + "' (nhom dieu kien #" + (gi + 1) + ", dieu kien #" + (ci + 1) + ")";
                if (!evaluateNode(label, step.stepOrder(), rule, ctx, null)) {
                    allTrue = false;
                    break;
                }
            }
            if (allTrue) {
                return true;
            }
        }
        return false;
    }

    /**
     * Danh gia DUNG 1 dieu kien (resolve field tu REQUEST_BODY/STEP_RESPONSE roi so
     * toan tu) - trich tu evaluateCondition() de DUNG LAI cho ca dieu kien 1 co don
     * le hien co VA moi dieu kien LA trong conditionGroups (V6, xem evaluateRuleGroups()),
     * tranh chep lai logic so toan tu 2 lan. {@code label} chi de dung trong thong bao
     * loi (GW-CONDITION-NOT-NUMERIC) - phan biet duoc dieu kien don le ("Step 'X'") voi
     * dieu kien trong 1 nhom cu the ("Step 'X' (nhom #N, dieu kien #M)") khi bao loi.
     */
    /**
     * Danh gia 1 nut cua cay dieu kien: LA -> evaluateSingleRule (hoac cờ he thong),
     * NHOM -> AND/OR cac nut con (de quy, cho phep long nhau).
     */
    private boolean evaluateNode(String label, int stepOrder, ConditionRuleDto node, ExecutionContext ctx, JsonNode scope) {
        if (node.hasQuantifier()) {
            return evaluateQuantifier(label, stepOrder, node, ctx, scope);
        }
        if (node.isGroup()) {
            return evaluateChildren(label, stepOrder, node.children(), node.logic(), ctx, scope);
        }
        if (node.sourceType() == FieldMappingSourceType.SYSTEM_FLAG) {
            return evaluateFlagLeaf(label, stepOrder, node);
        }
        // Ve phai lay tu field khac (so sanh 2 field) thay cho expectedValue hang so.
        String expected = node.compareSourceField() != null
                ? referenceAsText(resolveConditionValue(node.compareSourceType(), node.compareSourceField(),
                        node.compareSourceStepOrder(), ctx, scope))
                : node.expectedValue();
        return evaluateSingleRule(label, stepOrder, node.sourceType(), node.sourceField(), node.sourceStepOrder(),
                node.operator(), expected, ctx, scope);
    }

    /** AND/OR cac nut con. Trong pham vi phan tu (scope != null) cung dung logic nay. */
    private boolean evaluateChildren(String label, int stepOrder, List<ConditionRuleDto> children, String logic,
                                     ExecutionContext ctx, JsonNode scope) {
        boolean or = "OR".equalsIgnoreCase(logic);
        for (ConditionRuleDto child : children) {
            boolean childResult = evaluateNode(label, stepOrder, child, ctx, scope);
            if (or && childResult) {
                return true;
            }
            if (!or && !childResult) {
                return false;
            }
        }
        return !or;
    }

    /**
     * Lap mang: ANY = co it nhat 1 phan tu khop; ALL = moi phan tu khop (mang rong = KHONG khop,
     * fail-safe); COUNT = so phan tu khop so sanh countOperator/countValue. Gia tri khong phai mang
     * = KHONG khop. Dieu kien con doc field TU PHAN TU (xem ConditionRuleDto).
     */
    private boolean evaluateQuantifier(String label, int stepOrder, ConditionRuleDto node, ExecutionContext ctx, JsonNode scope) {
        JsonNode array = resolveConditionValue(node.sourceType(), node.sourceField(), node.sourceStepOrder(), ctx, scope);
        String quantifier = node.quantifier();
        boolean result;
        int matched = 0;
        int total = 0;
        if (array == null || !array.isArray()) {
            result = false;
        } else {
            total = array.size();
            for (JsonNode element : array) {
                boolean ok = evaluateChildren(label, stepOrder, node.children(), node.logic(), ctx, element);
                if (ok) {
                    matched++;
                }
                if (ConditionRuleDto.QUANTIFIER_ANY.equals(quantifier) && ok) {
                    break;
                }
                if (ConditionRuleDto.QUANTIFIER_ALL.equals(quantifier) && !ok) {
                    break;
                }
            }
            if (ConditionRuleDto.QUANTIFIER_ANY.equals(quantifier)) {
                result = matched > 0;
            } else if (ConditionRuleDto.QUANTIFIER_ALL.equals(quantifier)) {
                result = total > 0 && matched == total;
            } else {
                String countLabel = label;
                result = compareNumbers(node.countOperator(), matched,
                        expectedValueAsDouble(countLabel, node.countOperator(), node.countValue()));
            }
        }
        if (scope == null) {
            String text = label + ": " + quantifier + "(" + node.sourceField() + ")"
                    + (array != null && array.isArray() ? " = " + matched + "/" + total + " phan tu khop" : " = KHONG PHAI MANG")
                    + (ConditionRuleDto.QUANTIFIER_COUNT.equals(quantifier) ? " | " + node.countOperator() + " " + node.countValue() : "")
                    + " -> " + (result ? "DUNG" : "SAI");
            TraceCollector.recordCondition(stepOrder, text);
        }
        return result;
    }

    private static boolean compareNumbers(ConditionOperator op, double left, double right) {
        return switch (op) {
            case EQUALS -> left == right;
            case GREATER_THAN -> left > right;
            case GREATER_THAN_OR_EQUAL -> left >= right;
            case LESS_THAN -> left < right;
            case LESS_THAN_OR_EQUAL -> left <= right;
            default -> false;
        };
    }

    private String referenceAsText(JsonNode value) {
        return value == null || value.isNull() ? null : conditionValueAsString(value);
    }

    /** Co cờ he thong (FlagRegistryCache, mat khoi cache = false - fail-safe). expectedValue "true"/"false". */
    private boolean evaluateFlagLeaf(String label, int stepOrder, ConditionRuleDto node) {
        boolean current = flagRegistryCache.get(node.sourceField());
        boolean expected = !"false".equalsIgnoreCase(node.expectedValue());
        boolean result = node.operator() == ConditionOperator.NOT_EQUALS ? current != expected : current == expected;
        TraceCollector.recordCondition(stepOrder, describeCondition(label, FieldMappingSourceType.SYSTEM_FLAG,
                node.sourceField(), null, objectMapper.getNodeFactory().booleanNode(current),
                node.operator(), node.expectedValue(), result));
        return result;
    }

    private boolean evaluateSingleRule(String label, int stepOrder, FieldMappingSourceType sourceType, String sourceField,
                                        Integer sourceStepOrder, ConditionOperator operator, String expectedValue,
                                        ExecutionContext ctx, JsonNode scope) {
        JsonNode value = resolveConditionValue(sourceType, sourceField, sourceStepOrder, ctx, scope);
        boolean exists = value != null && !value.isNull();

        boolean result = expectedValue == null && operator.needsExpectedValue() ? false : switch (operator) {
            case EXISTS -> exists;
            case NOT_EXISTS -> !exists;
            case EQUALS -> exists && conditionValueAsString(value).equals(expectedValue);
            case NOT_EQUALS -> !exists || !conditionValueAsString(value).equals(expectedValue);
            // So sanh SO: gia tri response khong ton tai (!exists) coi la false, GIONG HET
            // cach EQUALS dang xu ly - khong throw chi vi step truoc chua chay (nhanh re
            // khac trong cung do thi dieu kien).
            case GREATER_THAN -> exists && conditionValueAsDouble(label, sourceField, operator, value) > expectedValueAsDouble(label, operator, expectedValue);
            case GREATER_THAN_OR_EQUAL -> exists && conditionValueAsDouble(label, sourceField, operator, value) >= expectedValueAsDouble(label, operator, expectedValue);
            case LESS_THAN -> exists && conditionValueAsDouble(label, sourceField, operator, value) < expectedValueAsDouble(label, operator, expectedValue);
            case LESS_THAN_OR_EQUAL -> exists && conditionValueAsDouble(label, sourceField, operator, value) <= expectedValueAsDouble(label, operator, expectedValue);
            // Gia tri da resolve la 1 MANG - kiem tra co PHAN TU nao khop expectedValue
            // (dang text) hay khong. KHONG phai mang (vd 1 chuoi/so don) -> coi la
            // KHONG khop (fail-safe, mirror triet ly "graceful null" cua JsonPathUtil) -
            // xem javadoc ConditionOperator.CONTAINS.
            case CONTAINS -> exists && arrayContainsValue(value, expectedValue);
            case NOT_CONTAINS -> !exists || !arrayContainsValue(value, expectedValue);
            case IN -> exists && inExpectedList(label, operator, expectedValue).contains(conditionValueAsString(value));
            case NOT_IN -> !exists || !inExpectedList(label, operator, expectedValue).contains(conditionValueAsString(value));
            case STARTS_WITH -> exists && conditionValueAsString(value).startsWith(expectedValue);
            case ENDS_WITH -> exists && conditionValueAsString(value).endsWith(expectedValue);
            case STRING_CONTAINS -> exists && conditionValueAsString(value).contains(expectedValue);
            case IS_EMPTY -> isEmptyValue(value);
            case IS_NOT_EMPTY -> !isEmptyValue(value);
            case MATCHES_REGEX -> exists && matchesRegex(label, expectedValue, conditionValueAsString(value));
            case SIZE_EQUALS -> exists && isSizable(value) && sizeOf(value) == expectedValueAsDouble(label, operator, expectedValue);
            case SIZE_GREATER_THAN -> exists && isSizable(value) && sizeOf(value) > expectedValueAsDouble(label, operator, expectedValue);
            case SIZE_GREATER_THAN_OR_EQUAL -> exists && isSizable(value) && sizeOf(value) >= expectedValueAsDouble(label, operator, expectedValue);
            case SIZE_LESS_THAN -> exists && isSizable(value) && sizeOf(value) < expectedValueAsDouble(label, operator, expectedValue);
            case SIZE_LESS_THAN_OR_EQUAL -> exists && isSizable(value) && sizeOf(value) <= expectedValueAsDouble(label, operator, expectedValue);
        };

        if (scope == null) {
            TraceCollector.recordCondition(stepOrder,
                    describeCondition(label, sourceType, sourceField, sourceStepOrder, value, operator, expectedValue, result));
        }
        return result;
    }

    /** Gia tri co kich thuoc: mang (so phan tu), object (so key), chuoi (so ky tu code point). */
    private static boolean isSizable(JsonNode value) {
        return value.isArray() || value.isObject() || value.isTextual();
    }

    private static double sizeOf(JsonNode value) {
        if (value.isTextual()) {
            String text = value.asText();
            return text.codePointCount(0, text.length());
        }
        return value.size();
    }

    /**
     * Nguon REQUEST_BODY: trong pham vi phan tu (scope != null) doc tu PHAN TU, nguoc lai doc
     * tu body goc cua client. Cac nguon khac khong bi anh huong boi pham vi.
     */
    private JsonNode resolveConditionValue(FieldMappingSourceType sourceType, String sourceField,
                                           Integer sourceStepOrder, ExecutionContext ctx, JsonNode scope) {
        if (sourceType == FieldMappingSourceType.REQUEST_BODY) {
            return JsonPathUtil.getByDotPath(scope != null ? scope : ctx.requestBody(), sourceField);
        }
        if (sourceType == FieldMappingSourceType.QUERY_PARAM) {
            String value = firstQueryParamValue(ctx, sourceField);
            return value == null ? null : objectMapper.getNodeFactory().stringNode(value);
        }
        if (sourceType == FieldMappingSourceType.HEADER) {
            String value = ctx.headers().get(sourceField.toLowerCase(Locale.ROOT));
            return value == null ? null : objectMapper.getNodeFactory().stringNode(value);
        }
        // sourceStepOrder la Integer (nullable) - KHONG duoc goi thang
        // ctx.getStepResult(int) o day vi auto-unbox null se NPE ngay tai
        // call site; step chua khai bao du dieu kien (thieu sourceStepOrder)
        // coi nhu khong co gia tri, khong crash.
        JsonNode source = sourceStepOrder == null ? null : ctx.getStepResult(sourceStepOrder);
        return JsonPathUtil.getByDotPath(source, sourceField);
    }

    /** Dong mo ta 1 dieu kien da danh gia, hien trong "Thu nhanh" - ghi ro field thieu (KHONG CO) de khoi phai doan. */
    private String describeCondition(String label, FieldMappingSourceType sourceType, String sourceField,
                                     Integer sourceStepOrder, JsonNode value, ConditionOperator operator,
                                     String expectedValue, boolean result) {
        String source;
        if (sourceType == FieldMappingSourceType.REQUEST_BODY) {
            source = "body." + sourceField;
        } else if (sourceType == FieldMappingSourceType.QUERY_PARAM) {
            source = "query." + sourceField;
        } else if (sourceType == FieldMappingSourceType.HEADER) {
            source = "header." + sourceField;
        } else if (sourceType == FieldMappingSourceType.SYSTEM_FLAG) {
            source = "flag." + sourceField;
        } else {
            source = "response step " + sourceStepOrder + "." + sourceField;
        }
        boolean exists = value != null && !value.isNull();
        String actual = exists ? truncate(conditionValueAsString(value), 200) : "KHONG CO";
        String expected = expectedValue == null ? "" : " '" + truncate(expectedValue, 200) + "'";
        return label + ": " + source + " = " + actual + " | " + operator + expected + " -> " + (result ? "DUNG" : "SAI");
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private boolean isEmptyValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return true;
        }
        if (value.isTextual()) {
            return value.asText().isEmpty();
        }
        if (value.isArray() || value.isObject()) {
            return value.size() == 0;
        }
        return false;
    }

    /** conditionExpectedValue phai la MANG JSON (vd ["A","B"]) - validate luc luu, kiem tra lai o day cho du lieu cu. */
    private Set<String> inExpectedList(String label, ConditionOperator operator, String expectedValue) {
        JsonNode list = null;
        if (expectedValue != null) {
            try {
                list = objectMapper.readTree(expectedValue);
            } catch (RuntimeException e) {
                list = null;
            }
        }
        if (list == null || !list.isArray()) {
            throw new BusinessException("GW-CONDITION-INVALID-VALUE", label + ": toan tu " + operator
                    + " can conditionExpectedValue la mang JSON (vd [\"A\",\"B\"]), nhung dang la '" + expectedValue + "'.");
        }
        Set<String> values = new HashSet<>();
        for (JsonNode element : list) {
            values.add(conditionValueAsString(element));
        }
        return values;
    }

    /**
     * Khop TOAN BO chuoi theo regex. Co gioi han thoi gian (REGEX_TIME_BUDGET) de bieu
     * thuc catastrophic-backtracking khong treo worker thread - kiem tra deadline moi
     * lan doc 1 ky tu.
     */
    private boolean matchesRegex(String label, String expectedValue, String actual) {
        if (expectedValue == null) {
            throw new BusinessException("GW-CONDITION-INVALID-VALUE", label + ": toan tu MATCHES_REGEX thieu regex.");
        }
        Pattern pattern;
        try {
            pattern = regexCache.computeIfAbsent(expectedValue, Pattern::compile);
        } catch (PatternSyntaxException e) {
            throw new BusinessException("GW-CONDITION-INVALID-VALUE", label + ": regex khong hop le '"
                    + expectedValue + "' (" + e.getDescription() + ").");
        }
        long deadline = System.nanoTime() + REGEX_TIME_BUDGET.toNanos();
        return pattern.matcher(new DeadlineCharSequence(actual, deadline, label)).matches();
    }

    private static Map<String, String> lowerCaseKeys(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, String> lowered = new HashMap<>();
        headers.forEach((k, v) -> lowered.put(k.toLowerCase(Locale.ROOT), v));
        return lowered;
    }

    private static final class DeadlineCharSequence implements CharSequence {
        private final CharSequence delegate;
        private final long deadlineNanos;
        private final String label;

        DeadlineCharSequence(CharSequence delegate, long deadlineNanos, String label) {
            this.delegate = delegate;
            this.deadlineNanos = deadlineNanos;
            this.label = label;
        }

        @Override
        public int length() {
            return delegate.length();
        }

        @Override
        public char charAt(int index) {
            if (System.nanoTime() > deadlineNanos) {
                throw new BusinessException("GW-CONDITION-REGEX-TIMEOUT", label
                        + ": regex chay qua lau (vuot " + REGEX_TIME_BUDGET.toMillis() + "ms) - kiem tra lai bieu thuc.");
            }
            return delegate.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new DeadlineCharSequence(delegate.subSequence(start, end), deadlineNanos, label);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    /** true neu value la 1 ArrayNode VA co it nhat 1 phan tu khop expectedValue (dang text). */
    private boolean arrayContainsValue(JsonNode value, String expectedValue) {
        if (!value.isArray()) {
            return false;
        }
        for (JsonNode element : value) {
            if (conditionValueAsString(element).equals(expectedValue)) {
                return true;
            }
        }
        return false;
    }

    private static final Duration REGEX_TIME_BUDGET = Duration.ofMillis(50);
    private final Map<String, Pattern> regexCache = new ConcurrentHashMap<>();

    private String conditionValueAsString(JsonNode value) {
        return value.isTextual() ? value.asText() : value.toString();
    }

    /**
     * Ep gia tri lay tu response (co the la JSON number that, hoac chuoi chua
     * so vd tu REQUEST_BODY) thanh double de so sanh voi toan tu >,>=,<,<=.
     * Throw ro rang neu KHONG phai so - tranh am tham re nhanh sai huong thay
     * vi bao loi (khac han so sanh EQUALS/NOT_EQUALS chap nhan bat ky chuoi nao).
     */
    private double conditionValueAsDouble(String label, String sourceField, ConditionOperator operator, JsonNode value) {
        if (value.isNumber()) {
            return value.asDouble();
        }
        try {
            return Double.parseDouble(value.asText());
        } catch (NumberFormatException e) {
            throw new BusinessException("GW-CONDITION-NOT-NUMERIC", label
                    + ": dieu kien re nhanh (" + operator + ") can gia tri SO tu field '"
                    + sourceField + "' nhung response thuc te la '" + conditionValueAsString(value) + "'.");
        }
    }

    /** conditionExpectedValue da duoc validate la so hop le luc luu (xem EndpointService.validateBranching()) - parse lai o day de phong du lieu cu/import tay khong qua validate. */
    private double expectedValueAsDouble(String label, ConditionOperator operator, String expectedValue) {
        try {
            return Double.parseDouble(expectedValue);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BusinessException("GW-CONDITION-NOT-NUMERIC", label
                    + ": conditionExpectedValue ('" + expectedValue + "') khong phai so hop le cho toan tu " + operator + ".");
        }
    }

    private JsonNode executeStep(EndpointResponseDto config, BackendStepDto step, ExecutionContext ctx) {
        UpstreamService upstream = upstreamRegistryCache.getById(step.upstreamServiceId());
        if (upstream == null) {
            throw new BusinessException("GW-UPSTREAM-404",
                    "Step '" + step.name() + "' tham chieu Upstream Service khong ton tai (id=" + step.upstreamServiceId() + ").");
        }

        // targetContext==MAIN: no-op voi moi du lieu hien co (mac dinh MAIN qua
        // compact constructor cua FieldMappingDto) - mapping targetContext=COMPENSATION
        // (muc 6) chi duoc dung boi executeCompensationStep(), khong bao gio lot vao
        // loi goi CHINH nay.
        List<FieldMappingDto> mappingsForStep = config.mappings().stream()
                .filter(m -> m.targetStepOrder() == step.stepOrder() && m.targetContext() == MappingTargetContext.MAIN)
                .toList();

        String resolvedPath = resolvePath(step.urlPattern(), step.name(), ctx, mappingsForStep);
        String resolvedUrl = buildUrl(upstream.getBaseHost(), resolvedPath, mappingsForStep, ctx);
        HttpHeaders headers = buildHeaders(mappingsForStep, ctx);
        JsonNode body = buildBody(step, mappingsForStep, ctx);

        HttpMethod httpMethod = HttpMethod.valueOf(step.method().name());
        JsonNode rawResponse = upstreamHttpExecutor.call(upstream, httpMethod, resolvedUrl, headers, body,
                step.cacheEnabled(), step.cacheTtlSeconds(), step.stepOrder(), step.name(),
                step.connectTimeoutMs(), step.readTimeoutMs(),
                step.id(), step.maxConcurrentCallsOverride(), step.maxWaitDurationMsOverride());

        JsonNode unwrapped = (step.target() == null || step.target().isBlank())
                ? rawResponse
                : orDefault(JsonPathUtil.getByDotPath(rawResponse, step.target()), objectMapper.createObjectNode());

        return ResponseTransformUtil.transform(unwrapped, step.allowFields(), step.denyFields(), step.fieldRenameMapping());
    }

    /**
     * Thay {token} trong urlPattern: uu tien FieldMapping targetType=PATH, sau do
     * fallback pathVariables cua endpoint (token trung ten).
     *
     * Chu ky nhan `urlPattern`/`stepLabel` RIENG (khong phai nguyen `BackendStepDto step`)
     * de TAI DUNG duoc CHO CA loi goi bu tru (muc 6, xem executeCompensationStep() -
     * urlPattern la `step.compensationUrlPattern()`, khac `step.urlPattern()` cua loi
     * goi chinh) - ham CHI tung dung dung 2 gia tri nay tu step (urlPattern + name
     * cho thong bao loi), doi chu ky KHONG doi 1 dong logic ben trong, call site
     * hien co (executeStep()) truyen y HET gia tri cu, khong doi hanh vi.
     */
    private String resolvePath(String urlPattern, String stepLabel, ExecutionContext ctx, List<FieldMappingDto> mappingsForStep) {
        String pattern = urlPattern;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([a-zA-Z0-9_]+)}").matcher(pattern);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            String token = matcher.group(1);
            String value = mappingsForStep.stream()
                    .filter(m -> m.targetType() == MappingTargetType.PATH && m.targetParamName().equals(token))
                    .findFirst()
                    .map(m -> resolveMappingValueAsString(m, ctx))
                    .orElseGet(() -> ctx.pathVariables().get(token));
            if (value == null) {
                throw new BusinessException("GW-PATH-TOKEN-MISSING",
                        "Khong tim duoc gia tri cho token '{" + token + "}' o step '" + stepLabel + "'.");
            }
            result.append(pattern, lastEnd, matcher.start()).append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
            lastEnd = matcher.end();
        }
        result.append(pattern.substring(lastEnd));
        return result.toString();
    }

    private String buildUrl(String baseHost, String resolvedPath, List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        // Spring Framework 7 (Boot 4): fromHttpUrl() bi go bo, dung fromUriString() thay the.
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseHost + resolvedPath);
        mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.QUERY)
                .forEach(m -> builder.queryParam(m.targetParamName(), resolveMappingValueAsString(m, ctx)));
        return builder.build().toUriString();
    }

    private HttpHeaders buildHeaders(List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.HEADER)
                .forEach(m -> headers.add(m.targetParamName(), resolveMappingValueAsString(m, ctx)));
        return headers;
    }

    private JsonNode buildBody(BackendStepDto step, List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        List<FieldMappingDto> bodyMappings = mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.BODY_FIELD)
                .toList();

        boolean needsBody = step.forwardOriginalBody() || !bodyMappings.isEmpty();
        if (!needsBody) {
            return null;
        }

        // Neu co dung 1 mapping BODY_FIELD voi targetParamName="$body", gia tri do
        // THAY THE TOAN BO body (khong bao trong field nao ca) - dung cho cac API
        // nhan thang 1 mang/gia tri lam body (vi du List<Long> lam @RequestBody,
        // khong phai {"ids": [...]}). Khong ket hop duoc voi forwardOriginalBody
        // hay cac BODY_FIELD khac vi ban chat mau thuan (khong con "object" nao
        // de gop them field vao).
        java.util.Optional<FieldMappingDto> wholeBodyMapping = bodyMappings.stream()
                .filter(m -> "$body".equals(m.targetParamName()))
                .findFirst();
        if (wholeBodyMapping.isPresent()) {
            return resolveMappingValueAsJson(wholeBodyMapping.get(), ctx);
        }

        ObjectNode base = (step.forwardOriginalBody() && ctx.requestBody() != null && ctx.requestBody().isObject())
                ? (ObjectNode) ctx.requestBody().deepCopy()
                : objectMapper.createObjectNode();

        for (FieldMappingDto m : bodyMappings) {
            JsonNode value = resolveMappingValueAsJson(m, ctx);
            JsonPathUtil.setField(base, m.targetParamName(), value);
        }
        return base;
    }

    /**
     * Bu tru/rollback nghiep vu (saga best-effort, muc 6) - sau khi CA CHUOI that
     * bai (goi tu handle(), xem javadoc o do), duyet ctx.completedStepOrders()
     * theo thu tu NGUOC (undo cai gan nhat truoc), voi moi step co
     * compensationUpstreamServiceId da cau hinh, goi lenh bu tru rieng cua no.
     *
     * Best-effort THAT SU: MOI loi goi bu tru rieng le duoc bat + log WARN + BO
     * QUA (chay tiep cac step con lai can bu tru) - method nay KHONG BAO GIO
     * throw, khong doi gia tri handle() se tra/nem ra ngoai (loi GOC luon la loi
     * client nhin thay, khong bao gio bi loi bu tru che mat).
     *
     * NGOAI PHAM VI V1 (ghi ro trong plan da duyet): khong co outbox/retry ben
     * vung neu ban than loi goi bu tru cung that bai - chi thu 1 lan roi bo qua.
     * Cung CHUA co metric rieng dem so lan bu tru thanh cong/that bai (chi co
     * log WARN) - can bo sung neu muon alert tu dong tren ty le rollback loi.
     *
     * LUU Y VE THU TU TRONG 1 WAVE: neu >=2 step cung 1 parallelGroup deu
     * thanh cong va deu co cau hinh bu tru, thu tu bu tru GIUA CHUNG phu
     * thuoc vao thread nao goi ExecutionContext.putStepResult() truoc - KHONG
     * xac dinh (non-deterministic) giua cac lan chay khac nhau. Dieu nay
     * khong sai ve nghiep vu (2 step trong 1 wave von doc lap, khong phu
     * thuoc thu tu lan nhau) nhung KHONG duoc hieu la "thu tu bu tru co y
     * nghia xac dinh" giua cac thanh vien cung wave.
     */
    private void runCompensations(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<Integer, BackendStepDto> stepsByOrder = indexStepsByOrder(orderedSteps);
        List<Integer> completedInOrder = ctx.completedStepOrders();
        for (int i = completedInOrder.size() - 1; i >= 0; i--) {
            BackendStepDto step = stepsByOrder.get(completedInOrder.get(i));
            if (step == null || step.compensationUpstreamServiceId() == null) {
                continue;
            }
            log.warn("Endpoint '{}': chuoi that bai, dang bu tru (rollback best-effort) step '{}' (order={})...",
                    config.name(), step.name(), step.stepOrder());
            try {
                executeCompensationStep(config, step, ctx);
            } catch (RuntimeException ex) {
                log.warn("Endpoint '{}': bu tru cho step '{}' (order={}) that bai, bo qua (best-effort, khong anh huong loi goc): {}",
                        config.name(), step.name(), step.stepOrder(), ex.getMessage());
            }
        }
    }

    /**
     * Goi 1 lenh bu tru rieng cua 1 step - mirror executeStep() nhung dung cau
     * hinh/mapping RIENG cua bu tru (compensationUpstreamServiceId/Method/UrlPattern,
     * mapping targetContext=COMPENSATION), khong dung gi tu loi goi CHINH. Response
     * cua lenh bu tru KHONG duoc dung tiep (khong unwrap/transform, khong
     * putStepResult) - chi de ghi audit hop (qua chinh UpstreamHttpExecutor.call()
     * nhu moi hop khac) va bao hieu thanh cong/that bai cho runCompensations().
     */
    private void executeCompensationStep(EndpointResponseDto config, BackendStepDto step, ExecutionContext ctx) {
        UpstreamService upstream = upstreamRegistryCache.getById(step.compensationUpstreamServiceId());
        if (upstream == null) {
            // Hiem (id sai/upstream vua bi xoa) - throw dung y het executeStep(), an
            // toan vi runCompensations() da boc TUNG loi goi bu tru trong try/catch rieng.
            throw new BusinessException("GW-UPSTREAM-404",
                    "Bu tru cho step '" + step.name() + "' tham chieu Upstream Service khong ton tai (id=" + step.compensationUpstreamServiceId() + ").");
        }
        // Phong thu: EndpointService.validateCompensationConfig() da bat buoc ca 3
        // field bu tru phai cung co/cung khong co tai thoi diem luu, nhung neu 1
        // cau hinh cu/sua tay trong DB lot qua duoc rule do, bao loi ro rang thay
        // vi de NullPointerException khong ro nguyen nhan roi bi runCompensations()
        // nuot am tham.
        if (step.compensationMethod() == null || step.compensationUrlPattern() == null || step.compensationUrlPattern().isBlank()) {
            throw new BusinessException("GW-COMPENSATION-CONFIG-INVALID",
                    "Bu tru cho step '" + step.name() + "' thieu compensationMethod/compensationUrlPattern (du lieu khong toan ven, kiem tra lai cau hinh).");
        }

        List<FieldMappingDto> compensationMappings = config.mappings().stream()
                .filter(m -> m.targetStepOrder() == step.stepOrder() && m.targetContext() == MappingTargetContext.COMPENSATION)
                .toList();

        String stepLabel = step.name() + " (bu tru)";
        String resolvedPath = resolvePath(step.compensationUrlPattern(), stepLabel, ctx, compensationMappings);
        String resolvedUrl = buildUrl(upstream.getBaseHost(), resolvedPath, compensationMappings, ctx);
        HttpHeaders headers = buildHeaders(compensationMappings, ctx);
        JsonNode body = buildCompensationBody(compensationMappings, ctx);

        HttpMethod httpMethod = HttpMethod.valueOf(step.compensationMethod().name());
        // Bu tru KHONG dung Bulkhead rieng cua step chinh (khong co field cau hinh rieng cho
        // nhanh bu tru) - luon dung chung Bulkhead cua Upstream, xem UpstreamHttpExecutor.bulkheadFor().
        upstreamHttpExecutor.call(upstream, httpMethod, resolvedUrl, headers, body,
                false, 0, step.stepOrder(), "[BU TRU] " + step.name(), null, null,
                null, null, null);
    }

    /**
     * Body cho lenh bu tru - CHI gop cac mapping targetType=BODY_FIELD (targetContext=
     * COMPENSATION), KHONG ho tro forwardOriginalBody/"$body" nhu buildBody() chinh
     * (V1 co tinh giu don gian - bu tru la 1 lenh "undo" co chu dich, khong can
     * nguyen body client goc). Tach RIENG method, KHONG sua buildBody() hien co.
     */
    private JsonNode buildCompensationBody(List<FieldMappingDto> compensationMappings, ExecutionContext ctx) {
        List<FieldMappingDto> bodyMappings = compensationMappings.stream()
                .filter(m -> m.targetType() == MappingTargetType.BODY_FIELD)
                .toList();
        if (bodyMappings.isEmpty()) {
            return null;
        }
        ObjectNode base = objectMapper.createObjectNode();
        for (FieldMappingDto m : bodyMappings) {
            JsonPathUtil.setField(base, m.targetParamName(), resolveMappingValueAsJson(m, ctx));
        }
        return base;
    }

    /** Tra ve gia tri mapping duoi dang JsonNode (dung cho BODY_FIELD - giu nguyen kieu, kem ca mang). */
    private JsonNode resolveMappingValueAsJson(FieldMappingDto m, ExecutionContext ctx) {
        return switch (m.sourceType()) {
            case REQUEST_BODY -> orDefault(JsonPathUtil.getByDotPath(ctx.requestBody(), m.sourceField()), objectMapper.nullNode());
            case QUERY_PARAM -> {
                String value = firstQueryParamValue(ctx, m.sourceField());
                yield value == null ? objectMapper.nullNode() : objectMapper.getNodeFactory().stringNode(value);
            }
            case STEP_RESPONSE -> orDefault(JsonPathUtil.getByDotPath(ctx.getStepResult(m.sourceStepOrder()), m.sourceField()), objectMapper.nullNode());
            case STEP_RESPONSE_ARRAY_AGGREGATE -> JsonPathUtil.toArrayNode(objectMapper,
                    JsonPathUtil.aggregateArray(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField(), m.sourceElementField()));
            case CONSTANT -> constantValueAsJson(m.constantValue());
            case STEP_RESPONSE_ARRAY_MERGE -> mergeArrayIntoObject(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField());
            case STEP_RESPONSE_ARRAY_FIND -> orDefault(JsonPathUtil.findInArray(ctx.getStepResult(m.sourceStepOrder()),
                    m.sourceArrayField(), m.filterField(), m.filterValue(), m.sourceElementField()), objectMapper.nullNode());
            // SYSTEM_FLAG CHI dung cho dieu kien re nhanh (conditionSourceType) -
            // validate luc luu da chan khoi xuat hien trong FieldMapping thuong;
            // neu du lieu cu/sua tay lot qua thi bao loi ro rang, khong am tham.
            case SYSTEM_FLAG -> throw new BusinessException("GW-003",
                    "FieldMapping sourceType=SYSTEM_FLAG khong hop le - cờ chi dung cho dieu kien re nhanh tai step.");
            case HEADER -> throw new BusinessException("GW-003",
                    "FieldMapping sourceType=HEADER khong hop le - header chi dung cho dieu kien re nhanh tai step.");
        };
    }

    /**
     * Gop TOAN BO field cua tung phan tu (moi phan tu la 1 object) trong 1 mang thanh 1
     * object duy nhat (union key/value) - xem javadoc FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE.
     * Phan tu KHONG phai object bi bo qua (khong throw, nhat quan triet ly aggregateArray()
     * hien co - du lieu la that tu upstream, khong chan luong vi 1 phan tu la khong hop dinh dang).
     * Key trung nhau: phan tu DEN SAU (thu tu trong mang) ghi de gia tri phan tu truoc -
     * dung ObjectNode.setAll() lan luot theo dung thu tu duyet mang.
     */
    private ObjectNode mergeArrayIntoObject(JsonNode root, String arrayPath) {
        ObjectNode result = objectMapper.createObjectNode();
        JsonNode arrayNode = JsonPathUtil.getByDotPath(root, arrayPath);
        if (arrayNode == null || !arrayNode.isArray()) {
            return result;
        }
        for (JsonNode element : arrayNode) {
            if (element != null && element.isObject()) {
                result.setAll((ObjectNode) element);
            }
        }
        return result;
    }

    /**
     * Gia tri CONSTANT cho BODY_FIELD - thu parse nhu JSON truoc (ho tro fix cung so/boolean/
     * object/mang, vi du constantValue="3" -> so JSON 3, "true" -> boolean true), khong parse
     * duoc (vi du "low") thi fallback ve chuoi text nguyen ban - KHONG throw, vi day la gia tri
     * admin tu khai bao (khac han "response khong phai so" cua toan tu so sanh so).
     */
    private JsonNode constantValueAsJson(String constantValue) {
        if (constantValue == null) {
            return objectMapper.nullNode();
        }
        try {
            return objectMapper.readTree(constantValue);
        } catch (Exception e) {
            return objectMapper.getNodeFactory().stringNode(constantValue);
        }
    }

    /** Query param cua CHINH client (khong phai response step nao) - luon la String[] (co the nhieu gia tri trung ten), lay gia tri dau tien. */
    private String firstQueryParamValue(ExecutionContext ctx, String paramName) {
        String[] values = ctx.queryParams().get(paramName);
        return (values == null || values.length == 0) ? null : values[0];
    }

    /** Tra ve gia tri mapping duoi dang String (dung cho PATH/QUERY/HEADER). Mang (ARRAY_AGGREGATE) duoc noi bang dau phay. */
    private String resolveMappingValueAsString(FieldMappingDto m, ExecutionContext ctx) {
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE) {
            List<String> values = JsonPathUtil.aggregateArray(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField(), m.sourceElementField());
            return String.join(",", values);
        }
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM) {
            return firstQueryParamValue(ctx, m.sourceField());
        }
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.CONSTANT) {
            // PATH/QUERY/HEADER luon la chuoi text tren URL/header - KHONG parse JSON o day
            // (khac buildBody(), noi so/boolean can giu dung kieu JSON goc).
            return m.constantValue();
        }
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE) {
            // Chi hop le voi targetType=BODY_FIELD (EndpointService da chan luc luu) - toi day
            // chi con du lieu cu/import tay lot qua validate, tra ve dang JSON string cua object
            // gop thay vi throw/tra null am tham.
            return mergeArrayIntoObject(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField()).toString();
        }
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_FIND) {
            JsonNode node = JsonPathUtil.findInArray(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField(),
                    m.filterField(), m.filterValue(), m.sourceElementField());
            if (node == null || node.isNull()) {
                return null;
            }
            return node.isTextual() ? node.asText() : node.toString();
        }
        JsonNode node = m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY
                ? JsonPathUtil.getByDotPath(ctx.requestBody(), m.sourceField())
                : JsonPathUtil.getByDotPath(ctx.getStepResult(m.sourceStepOrder()), m.sourceField());
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isTextual() ? node.asText() : node.toString();
    }

    /**
     * Chi con duoc goi cho endpoint KHONG sequential (xem handle()) - sequential
     * gio di qua executeSequentialChain() rieng, tra ket qua truc tiep.
     */
    private JsonNode assembleFinalResponse(List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        if (orderedSteps.size() == 1) {
            int lastStepOrder = orderedSteps.get(orderedSteps.size() - 1).stepOrder();
            return ctx.getStepResult(lastStepOrder);
        }

        // Nhieu step doc lap: gop field cua tat ca step vao 1 object,
        // dung "group" de long rieng neu duoc khai bao (tranh dam field), step khai bao
        // sau se de len field trung ten cua step truoc (giu dung hanh vi cu).
        ObjectNode merged = objectMapper.createObjectNode();
        for (BackendStepDto step : orderedSteps) {
            JsonNode stepResult = ctx.getStepResult(step.stepOrder());
            if (stepResult == null) {
                continue;
            }
            if (step.group() != null && !step.group().isBlank()) {
                merged.set(step.group(), stepResult);
            } else if (stepResult.isObject()) {
                merged.setAll((ObjectNode) stepResult);
            }
        }
        return merged;
    }

    private JsonNode parseBodyOrNull(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new BusinessException("GW-INVALID-BODY", "Body request khong phai JSON hop le: " + e.getMessage());
        }
    }

    private <T> T orDefault(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
