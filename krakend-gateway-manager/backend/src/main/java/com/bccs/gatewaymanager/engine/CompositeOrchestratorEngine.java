package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.MappingTargetContext;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * Thuc thi 1 EndpointConfig cho 1 request that.
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
                            Map<String, String[]> queryParams, String rawRequestBody) {
        JsonNode requestBodyNode = parseBodyOrNull(rawRequestBody);
        ExecutionContext ctx = new ExecutionContext(pathVariables, queryParams, requestBodyNode);

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
            return assembleFinalResponse(orderedSteps, ctx);
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
        try {
            return executeSequentialChain(config, orderedSteps, ctx);
        } catch (RuntimeException e) {
            runCompensations(config, orderedSteps, ctx);
            throw e;
        }
    }

    /**
     * Thuc thi chuoi step TUAN TU theo CON TRO (khong phai vong lap for co
     * dinh) - bat dau tu step co stepOrder NHO NHAT, sau moi step tu hoi
     * "step tiep theo la gi" qua determineNextStepOrder(). Step KHONG khai
     * bao dieu kien (conditionOperator=null - dung cho 100% endpoint tu
     * TRUOC P1-5) tai tao DUNG hanh vi cu: van chay het tat ca step theo
     * dung thu tu stepOrder tang dan, khong bo sot/doi thu tu gi ca.
     *
     * Ket qua tra ve = response cua STEP CUOI CUNG THUC SU chay trong lan
     * nay - KHAC voi truoc day (luon la step co stepOrder lon nhat), vi voi
     * re nhanh, chuoi co the ket thuc o BAT KY step nao tuy theo dieu kien.
     */
    private JsonNode executeSequentialChain(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<Integer, BackendStepDto> stepsByOrder = orderedSteps.stream()
                .collect(Collectors.toMap(BackendStepDto::stepOrder, s -> s));
        List<Integer> allOrders = orderedSteps.stream().map(BackendStepDto::stepOrder).sorted().toList();
        // Tap hop stepOrder la DICH cua it nhat 1 "buoc nhay dac biet" - re nhanh
        // (nextStepOrderIfTrue/nextStepOrderIfFalse) HOAC fallback loi (onErrorStepOrder) -
        // dung de xu ly dung "nhanh la"/"step fallback" trong determineNextStepOrder() (xem
        // javadoc o do): ca 2 loai buoc nhay nay deu KHONG phai "chay tuan tu tu nhien", nen
        // step dich cua chung, neu ban than khong co dieu kien rieng, phai DUNG LAI tai do
        // thay vi am tham roi tiep sang stepOrder lon hon ke tiep.
        Set<Integer> branchTargetOrders = orderedSteps.stream()
                .flatMap(s -> Stream.of(
                        s.conditionOperator() != null ? s.nextStepOrderIfTrue() : null,
                        s.conditionOperator() != null ? s.nextStepOrderIfFalse() : null,
                        s.onErrorStepOrder()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Integer currentOrder = allOrders.isEmpty() ? null : allOrders.get(0);
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
     */
    private void executeStepsInParallel(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<String, String> callerMdcContext = MDC.getCopyOfContextMap();
        List<Future<?>> futures = orderedSteps.stream()
                .<Future<?>>map(step -> parallelStepExecutor.submit(() -> {
                    Map<String, String> workerMdcContext = MDC.getCopyOfContextMap();
                    if (callerMdcContext != null) {
                        MDC.setContextMap(callerMdcContext);
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
        return evaluateCondition(step, ctx) ? step.nextStepOrderIfTrue() : step.nextStepOrderIfFalse();
    }

    /**
     * So sanh 1 field (STEP_RESPONSE/REQUEST_BODY) voi conditionExpectedValue.
     * Tham chieu toi 1 step CHUA TUNG chay (vi 1 nhanh re khac da di - hoan
     * toan hop le trong 1 do thi co dieu kien) tra ve null AN TOAN qua
     * JsonPathUtil.getByDotPath(null, ...) - KHONG throw, coi nhu "khong ton tai".
     */
    private boolean evaluateCondition(BackendStepDto step, ExecutionContext ctx) {
        JsonNode source;
        if (step.conditionSourceType() == FieldMappingSourceType.REQUEST_BODY) {
            source = ctx.requestBody();
        } else {
            // sourceStepOrder la Integer (nullable) - KHONG duoc goi thang
            // ctx.getStepResult(int) o day vi auto-unbox null se NPE ngay tai
            // call site; step chua khai bao du dieu kien (thieu sourceStepOrder)
            // coi nhu khong co gia tri, khong crash.
            source = step.conditionSourceStepOrder() == null ? null : ctx.getStepResult(step.conditionSourceStepOrder());
        }
        JsonNode value = JsonPathUtil.getByDotPath(source, step.conditionSourceField());
        boolean exists = value != null && !value.isNull();

        return switch (step.conditionOperator()) {
            case EXISTS -> exists;
            case NOT_EXISTS -> !exists;
            case EQUALS -> exists && conditionValueAsString(value).equals(step.conditionExpectedValue());
            case NOT_EQUALS -> !exists || !conditionValueAsString(value).equals(step.conditionExpectedValue());
            // So sanh SO: gia tri response khong ton tai (!exists) coi la false, GIONG HET
            // cach EQUALS dang xu ly - khong throw chi vi step truoc chua chay (nhanh re
            // khac trong cung do thi dieu kien).
            case GREATER_THAN -> exists && conditionValueAsDouble(step, value) > expectedValueAsDouble(step);
            case GREATER_THAN_OR_EQUAL -> exists && conditionValueAsDouble(step, value) >= expectedValueAsDouble(step);
            case LESS_THAN -> exists && conditionValueAsDouble(step, value) < expectedValueAsDouble(step);
            case LESS_THAN_OR_EQUAL -> exists && conditionValueAsDouble(step, value) <= expectedValueAsDouble(step);
        };
    }

    private String conditionValueAsString(JsonNode value) {
        return value.isTextual() ? value.asText() : value.toString();
    }

    /**
     * Ep gia tri lay tu response (co the la JSON number that, hoac chuoi chua
     * so vd tu REQUEST_BODY) thanh double de so sanh voi toan tu >,>=,<,<=.
     * Throw ro rang neu KHONG phai so - tranh am tham re nhanh sai huong thay
     * vi bao loi (khac han so sanh EQUALS/NOT_EQUALS chap nhan bat ky chuoi nao).
     */
    private double conditionValueAsDouble(BackendStepDto step, JsonNode value) {
        if (value.isNumber()) {
            return value.asDouble();
        }
        try {
            return Double.parseDouble(value.asText());
        } catch (NumberFormatException e) {
            throw new BusinessException("GW-CONDITION-NOT-NUMERIC", "Step '" + step.name()
                    + "': dieu kien re nhanh (" + step.conditionOperator() + ") can gia tri SO tu field '"
                    + step.conditionSourceField() + "' nhung response thuc te la '" + conditionValueAsString(value) + "'.");
        }
    }

    /** conditionExpectedValue da duoc validate la so hop le luc luu (xem EndpointService.validateBranching()) - parse lai o day de phong du lieu cu/import tay khong qua validate. */
    private double expectedValueAsDouble(BackendStepDto step) {
        try {
            return Double.parseDouble(step.conditionExpectedValue());
        } catch (NumberFormatException | NullPointerException e) {
            throw new BusinessException("GW-CONDITION-NOT-NUMERIC", "Step '" + step.name()
                    + "': conditionExpectedValue ('" + step.conditionExpectedValue() + "') khong phai so hop le cho toan tu "
                    + step.conditionOperator() + ".");
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
                step.connectTimeoutMs(), step.readTimeoutMs());

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
     */
    private void runCompensations(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<Integer, BackendStepDto> stepsByOrder = orderedSteps.stream()
                .collect(Collectors.toMap(BackendStepDto::stepOrder, s -> s));
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

        List<FieldMappingDto> compensationMappings = config.mappings().stream()
                .filter(m -> m.targetStepOrder() == step.stepOrder() && m.targetContext() == MappingTargetContext.COMPENSATION)
                .toList();

        String stepLabel = step.name() + " (bu tru)";
        String resolvedPath = resolvePath(step.compensationUrlPattern(), stepLabel, ctx, compensationMappings);
        String resolvedUrl = buildUrl(upstream.getBaseHost(), resolvedPath, compensationMappings, ctx);
        HttpHeaders headers = buildHeaders(compensationMappings, ctx);
        JsonNode body = buildCompensationBody(compensationMappings, ctx);

        HttpMethod httpMethod = HttpMethod.valueOf(step.compensationMethod().name());
        upstreamHttpExecutor.call(upstream, httpMethod, resolvedUrl, headers, body,
                false, 0, step.stepOrder(), "[BU TRU] " + step.name(), null, null);
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
