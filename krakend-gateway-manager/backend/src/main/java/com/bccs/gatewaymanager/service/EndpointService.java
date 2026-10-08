package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.GwmFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Control-Plane-only (@Profile) - CRUD cau hinh, doc/ghi truc tiep DB dung
 * chung. KHONG con dieu khien EndpointRegistryCache nua (bo tu 2026-09, xem
 * javadoc class do) - cache dinh tuyen phuc vu traffic that gio la trach
 * nhiem RIENG cua Data Plane (tu dong bo qua RemoteConfigSyncService, doc
 * dinh ky qua API export, khong con "co hieu luc ngay lap tuc trong cung 1
 * tien trinh" nhu truoc - xem SAD ADR-07).
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointConfigRepository repository;
    private final EndpointMapper mapper;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final EndpointVersionService versionService;
    private final GatewayCacheService cacheService;
    private final UpstreamHttpExecutor upstreamHttpExecutor;
    private final GwmFlagRepository flagRepository;

    @Transactional(readOnly = true)
    public List<EndpointResponseDto> list(String search) {
        String teamCode = CurrentTeamContext.require();
        List<EndpointConfig> found = (search == null || search.isBlank())
                ? repository.findAllByTeamCodeOrderByUpdatedAtDesc(teamCode)
                : repository.search(teamCode, search.trim());
        return found.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public EndpointResponseDto get(String id) {
        return mapper.toResponseDto(findOrThrow(id));
    }

    @Transactional
    public EndpointResponseDto create(EndpointRequestDto dto) {
        rejectReservedPath(dto.path());
        validate(dto);
        if (repository.existsByTeamCodeAndPath(CurrentTeamContext.require(), dto.path())) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        EndpointConfig entity = mapper.toEntity(dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        rejectIfCyclic();
        versionService.recordSnapshot(saved, EndpointChangeType.CREATED);
        log.info("Da tao endpoint moi: {} {}", saved.getMethod(), saved.getPath());
        return result;
    }

    @Transactional
    public EndpointResponseDto update(String id, EndpointRequestDto dto) {
        return update(id, dto, EndpointChangeType.UPDATED);
    }

    /**
     * Khoi phuc endpoint id ve dung noi dung cua 1 phien ban cu (xem
     * EndpointVersionService) - chay qua CUNG duong validate/save/cycle-check
     * voi sua tay binh thuong (chi khac changeType de danh dau ro trong lich
     * su la 1 lan Khoi phuc, khong phai 1 lan sua thu cong).
     */
    @Transactional
    public EndpointResponseDto rollback(String id, String versionId) {
        EndpointRequestDto dto = versionService.toRequestDtoForRollback(id, versionId);
        return update(id, dto, EndpointChangeType.ROLLED_BACK);
    }

    private EndpointResponseDto update(String id, EndpointRequestDto dto, EndpointChangeType changeType) {
        rejectReservedPath(dto.path());
        validate(dto);
        EndpointConfig entity = findOrThrow(id);
        if (repository.existsByTeamCodeAndPathAndIdNot(CurrentTeamContext.require(), dto.path(), id)) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        // Chup lai TRUOC khi mapper.updateEntity() sua entity tai cho - can ban
        // "truoc" de so sanh voi ket qua sau save, phat hien cau hinh anh huong
        // cache co doi khong (xem purgeStaleCacheIfChanged()).
        EndpointResponseDto before = mapper.toResponseDto(entity);
        mapper.updateEntity(entity, dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        rejectIfCyclic();
        versionService.recordSnapshot(saved, changeType);
        purgeStaleCacheIfChanged(before, result);
        invalidateChangedStepBulkheads(before, result);
        log.info("Da cap nhat endpoint ({}): {} {}", changeType, saved.getMethod(), saved.getPath());
        return result;
    }

    /**
     * Xoa cache Redis CU (Control Plane, phuc vu "Thu ngay"/"Thu nhanh") cho
     * MOI phan cau hinh anh huong toi key cache thuc su doi giua "before"/"result" -
     * xem bug that: bat cache (response loi bi cache) -> tat cache (het loi, bo
     * qua cache) -> BAT LAI cache -> loi tai xuat hien vi key cache KHONG doi
     * khi cacheEnabled doi, entry Redis cu (mang response loi) van con nguyen
     * toi khi het TTL. Goi o day dam bao MOI lan luu co doi field anh huong
     * cache deu don sach entry cu truoc, khong can doi TTL tu het.
     *
     * Chi xoa duoc cache CUA CHINH CONTROL PLANE nay (Redis rieng, phuc vu Thu
     * ngay/Thu nhanh) - cache tren (cac) Data Plane that (Redis rieng tung doi,
     * khac mang/khac tien trinh) duoc don o RemoteConfigSyncService.
     * invalidateChangedEndpointCaches() theo dung mirror pattern nay, cho
     * doi toi chu ky sync ke tiep (toi da sync-interval-seconds giay).
     */
    private void purgeStaleCacheIfChanged(EndpointResponseDto before, EndpointResponseDto result) {
        boolean responseCacheKeyMayChange = !before.path().equals(result.path()) || before.method() != result.method();
        // ResponseMapping doi -> noi dung response tra client doi (khong doi key cache
        // nhung entry cu van mang response dung cau hinh cu) - phai don cung response-cache.
        if (before.responseCacheEnabled() != result.responseCacheEnabled()
                || ((before.responseCacheEnabled() || result.responseCacheEnabled()) && responseCacheKeyMayChange)
                || !java.util.Objects.equals(before.responseMapping(), result.responseMapping())) {
            cacheService.deleteByPattern("gwm:response-cache:" + result.id() + ":*");
        }

        Map<Integer, BackendStepDto> beforeByOrder = new HashMap<>();
        for (BackendStepDto s : before.steps()) {
            beforeByOrder.put(s.stepOrder(), s);
        }
        for (BackendStepDto after : result.steps()) {
            BackendStepDto old = beforeByOrder.get(after.stepOrder());
            if (old == null) {
                continue; // Step moi them - chua co gi duoc cache duoi stepOrder nay.
            }
            boolean cacheKeyMayChange = !old.upstreamServiceName().equals(after.upstreamServiceName())
                    || old.method() != after.method() || !old.urlPattern().equals(after.urlPattern());
            if (old.cacheEnabled() != after.cacheEnabled()
                    || ((old.cacheEnabled() || after.cacheEnabled()) && cacheKeyMayChange)) {
                cacheService.deleteByPattern("gwm:cache:" + old.upstreamServiceName() + ":" + old.method() + ":*");
                if (!old.upstreamServiceName().equals(after.upstreamServiceName()) || old.method() != after.method()) {
                    cacheService.deleteByPattern("gwm:cache:" + after.upstreamServiceName() + ":" + after.method() + ":*");
                }
            }
        }
    }

    /**
     * Don Bulkhead RIENG theo step (xem BackendStep.maxConcurrentCallsOverride,
     * UpstreamHttpExecutor.bulkheadFor()) khi CHINH override cua step do doi - Resilience4j
     * registry chi tao instance 1 LAN DAU (dung y het ly do UpstreamHttpExecutor.invalidate()
     * ton tai cho Upstream) nen doi xong khong don thi KHONG co hieu luc toi khi restart app.
     * Mirror dung cau truc voi purgeStaleCacheIfChanged() o tren (so sanh theo stepOrder,
     * step moi them/step da xoa thi bo qua - khong co gi de don/chua co gi de don).
     *
     * Chi don Bulkhead cua CHINH Control Plane nay (dung cho "Thu ngay"/"Thu nhanh") - Data
     * Plane that don Bulkhead rieng cua no o RemoteConfigSyncService.invalidateChangedStepBulkheads(),
     * cho toi chu ky sync ke tiep (toi da sync-interval-seconds giay).
     */
    private void invalidateChangedStepBulkheads(EndpointResponseDto before, EndpointResponseDto result) {
        Map<Integer, BackendStepDto> beforeByOrder = new HashMap<>();
        for (BackendStepDto s : before.steps()) {
            beforeByOrder.put(s.stepOrder(), s);
        }
        for (BackendStepDto after : result.steps()) {
            BackendStepDto old = beforeByOrder.get(after.stepOrder());
            if (old == null) {
                continue; // Step moi them - chua tung co Bulkhead rieng nao de don.
            }
            boolean overrideChanged = !java.util.Objects.equals(old.maxConcurrentCallsOverride(), after.maxConcurrentCallsOverride())
                    || !java.util.Objects.equals(old.maxWaitDurationMsOverride(), after.maxWaitDurationMsOverride());
            // Doi Upstream cua step (upstreamServiceName) MA truoc do dang co override rieng
            // cung phai don - khoa Bulkhead gom ca ten Upstream ("upstreamName#step:stepId"),
            // doi Upstream ma khong don se de lai 1 Bulkhead "mo coi" khoa theo ten Upstream CU.
            boolean upstreamChangedWithOldOverride = old.maxConcurrentCallsOverride() != null
                    && !old.upstreamServiceName().equals(after.upstreamServiceName());
            if (overrideChanged || upstreamChangedWithOldOverride) {
                upstreamHttpExecutor.invalidateStepBulkhead(old.upstreamServiceName(), old.id());
            }
        }
    }

    /**
     * Xoa NGAY cache Redis cua Control Plane (dung khi "Thu ngay"/"Thu nhanh")
     * cho 1 endpoint - dung khi nguoi dung muon don cache bay gio, KHONG doi
     * cau hinh gi (vi du cache bi nhiem response loi do test draft qua "Thu
     * nhanh" nhieu lan - EndpointTryService.tryAdhoc() dung CHUNG keyspace
     * cache voi step da luu neu trung (upstreamName, method), purgeStaleCacheIfChanged()
     * o tren khong bat duoc truong hop nay vi khong co gi "doi" giua 2 lan luu).
     *
     * CHI xoa duoc Redis cua CHINH Control Plane nay - KHONG xoa duoc cache
     * tren Data Plane that cua doi (khac Redis/khac mang, xem javadoc
     * purgeStaleCacheIfChanged()) - FE PHAI hien ro gioi han nay cho nguoi dung,
     * khong duoc ngu y da xoa toan bo.
     */
    @Transactional(readOnly = true)
    public void clearCache(String id) {
        EndpointConfig entity = findOrThrow(id);
        EndpointResponseDto dto = mapper.toResponseDto(entity);
        if (dto.responseCacheEnabled()) {
            cacheService.deleteByPattern("gwm:response-cache:" + dto.id() + ":*");
        }
        for (BackendStepDto step : dto.steps()) {
            if (step.cacheEnabled()) {
                cacheService.deleteByPattern("gwm:cache:" + step.upstreamServiceName() + ":" + step.method() + ":*");
            }
        }
        log.info("Da xoa cache (Control Plane) theo yeu cau thu cong cho endpoint: {} {}", dto.method(), dto.path());
    }

    @Transactional
    public void delete(String id) {
        EndpointConfig entity = findOrThrow(id);
        versionService.deleteAllForEndpoint(id);
        repository.delete(entity);
        log.info("Da xoa endpoint: {} {}", entity.getMethod(), entity.getPath());
    }

    /**
     * Chan dat path cua 1 Data Plane endpoint trung tien to danh cho Control
     * Plane (/api) hoac Actuator (/actuator). ApiKeyAuthFilter dang ky theo
     * urlPattern Servlet "/api/*" (khop TAT CA request bat dau bang /api,
     * KHONG can biet Spring MVC se route no toi controller nao) - neu admin
     * lo dat 1 endpoint composite tai path "/api/orders" chang han, request
     * cua client that se bi ApiKeyAuthFilter tu choi 401 truoc khi toi duoc
     * DynamicDispatcherController, du muc dich la Data Plane KHONG auth.
     * Chan ngay luc luu se an toan hon xu ly muon o tang filter.
     */
    private void rejectReservedPath(String path) {
        if (path != null && (path.equals("/api") || path.startsWith("/api/")
                || path.equals("/actuator") || path.startsWith("/actuator/"))) {
            throw new BusinessException("GW-001",
                    "Path '" + path + "' bi cam vi trung tien to danh rieng cho Control Plane (/api) hoac Actuator (/actuator).");
        }
    }

    /** Tra theo id VA CurrentTeamContext - 1 doi khong the sua/xoa endpoint cua doi khac du doan dung ID (IDOR). */
    private EndpointConfig findOrThrow(String id) {
        return repository.findByIdAndTeamCode(id, CurrentTeamContext.require())
                .orElseThrow(() -> new BusinessException("GW-404", "Khong tim thay endpoint id=" + id));
    }

    /**
     * Chan luu (rollback @Transactional) neu cau hinh hien tai (sau khi save) tao
     * thanh 1 vong lap phu thuoc giua cac endpoint (A goi nguoc B, B goi lai A...).
     * Goi TRUOC registryCache.reload() - la thoi diem endpoint thuc su "len live" -
     * de vong lap khong bao gio duoc kich hoat qua duong CRUD binh thuong (truoc day
     * chi POST /api/config/deploy rieng biet moi kiem tra, CRUD thuong bo qua hoan
     * toan cho du comment cua ConfigController tung khang dinh sai la "bi chan han").
     */
    private void rejectIfCyclic() {
        List<String> warnings = dependencyAnalyzer.detectCycleWarningsOnly();
        if (!warnings.isEmpty()) {
            throw new BusinessException("GW-CYCLE",
                    "Phat hien vong lap phu thuoc giua cac endpoint. " + String.join(" | ", warnings));
        }
    }

    /**
     * Kiem tra: stepOrder phai bat dau tu 1, khong trung, va mapping phai tham chieu step
     * co that + hop le (cong ca cac validate con goi tu day - branching/parallel group/
     * compensation/response-cache).
     *
     * Package-private (khong con private) de EndpointTryService.tryAdhoc() ("Thu nhanh"
     * cho 1 draft CHUA LUU) goi lai DUNG 1 logic voi luc luu that, khong copy/lech hanh
     * vi - draft chi duoc validate, KHONG di qua rejectReservedPath()/existsByPath() (bat
     * buoc khi luu that, khong lien quan 1 lan thu tam thoi khong dong bo routing table).
     */
    void validate(EndpointRequestDto dto) {
        // parallelExecution (muc 4) chi co y nghia voi step DOC LAP (sequential=false) - xem
        // javadoc CompositeOrchestratorEngine.handle(). Bat ca 2 cung luc la cau hinh mau
        // thuan (sequential=true khong bao gio di qua nhanh code parallel), chan som tai day
        // thay vi luu 1 cau hinh gay hieu lam roi sau nay khong ai biet vi sao khong chay
        // song song.
        if (dto.sequential() && dto.parallelExecution()) {
            throw new BusinessException("GW-003",
                    "parallelExecution chi ap dung duoc khi sequential=false (step doc lap) - "
                            + "endpoint sequential=true luon chay tuan tu theo con tro, khong dung thread pool song song.");
        }

        var orders = dto.steps().stream().map(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder).toList();
        if (orders.stream().distinct().count() != orders.size()) {
            throw new BusinessException("GW-002", "stepOrder bi trung lap giua cac backend step.");
        }
        int maxOrder = orders.stream().mapToInt(Integer::intValue).max().orElse(0);
        // P1-5: khi endpoint co re nhanh, thu tu THUC THI khong con dam bao theo
        // dung thu tu so stepOrder nua (vi du step 5 co the chay TRUOC step 3 neu
        // duong re nhanh di nhu vay) - rule "sourceStepOrder < targetStepOrder"
        // ben duoi vi vay khong con dung tinh cho endpoint loai nay, phai bo qua
        // va dua vao co che graceful-null luc runtime (xem
        // CompositeOrchestratorEngine.evaluateCondition()/JsonPathUtil.getByDotPath()
        // da xu ly an toan tham chieu toi step chua chay).
        boolean usesBranching = usesBranching(dto);
        if (dto.mappings() != null) {
            for (var m : dto.mappings()) {
                boolean needsSourceStep = m.sourceType() != com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY
                        && m.sourceType() != com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM
                        && m.sourceType() != com.bccs.gatewaymanager.entity.FieldMappingSourceType.CONSTANT;
                if (needsSourceStep && (m.sourceStepOrder() == null || m.sourceStepOrder() > maxOrder)) {
                    throw new BusinessException("GW-003", "FieldMapping (sourceType=" + m.sourceType()
                            + ") thieu sourceStepOrder hop le (max step = " + maxOrder + ").");
                }
                if (m.targetStepOrder() > maxOrder) {
                    throw new BusinessException("GW-003", "FieldMapping tham chieu step khong ton tai (max step = " + maxOrder + ").");
                }
                // sourceStepOrder phai < targetStepOrder (dung invariant FieldMapping da tu ghi
                // chu) - step nguon phai chay TRUOC step dich, neu khong ket qua se luon null
                // luc runtime (engine thuc thi tuan tu, step sau moi thay du lieu step truoc).
                // BO QUA cho mapping targetContext=COMPENSATION (muc 6) - truong hop pho bien
                // nhat la sourceStepOrder==targetStepOrder (bu tru step N dung CHINH response
                // cua step N, vi du lay "orderId" vua tao de dien vao URL DELETE).
                if (!usesBranching && needsSourceStep && m.targetContext() == com.bccs.gatewaymanager.entity.MappingTargetContext.MAIN
                        && m.sourceStepOrder() != null && m.sourceStepOrder() >= m.targetStepOrder()) {
                    throw new BusinessException("GW-003", "FieldMapping co sourceStepOrder (" + m.sourceStepOrder()
                            + ") phai nho hon targetStepOrder (" + m.targetStepOrder() + ") - step nguon phai chay truoc step dich.");
                }
                if ((m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                        || m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY
                        || m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM)
                        && (m.sourceField() == null || m.sourceField().isBlank())) {
                    throw new BusinessException("GW-003", "FieldMapping (sourceType=" + m.sourceType() + ") thieu sourceField.");
                }
                if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE
                        && ((m.sourceArrayField() == null || m.sourceArrayField().isBlank())
                            || (m.sourceElementField() == null || m.sourceElementField().isBlank()))) {
                    throw new BusinessException("GW-003",
                            "FieldMapping (sourceType=STEP_RESPONSE_ARRAY_AGGREGATE) thieu sourceArrayField/sourceElementField.");
                }
                if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.CONSTANT
                        && (m.constantValue() == null || m.constantValue().isBlank())) {
                    throw new BusinessException("GW-003", "FieldMapping (sourceType=CONSTANT) thieu constantValue.");
                }
                if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE) {
                    if (m.sourceArrayField() == null || m.sourceArrayField().isBlank()) {
                        throw new BusinessException("GW-003",
                                "FieldMapping (sourceType=STEP_RESPONSE_ARRAY_MERGE) thieu sourceArrayField.");
                    }
                    // Object gop khong flatten duoc thanh chuoi cho PATH/QUERY/HEADER - chi hop
                    // ly khi bom thang vao body (xem javadoc FieldMappingSourceType).
                    if (m.targetType() != com.bccs.gatewaymanager.entity.MappingTargetType.BODY_FIELD) {
                        throw new BusinessException("GW-003",
                                "FieldMapping (sourceType=STEP_RESPONSE_ARRAY_MERGE) chi dung duoc voi targetType=BODY_FIELD.");
                    }
                }
                if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_FIND
                        && ((m.sourceArrayField() == null || m.sourceArrayField().isBlank())
                            || (m.filterField() == null || m.filterField().isBlank())
                            || (m.filterValue() == null || m.filterValue().isBlank())
                            || (m.sourceElementField() == null || m.sourceElementField().isBlank()))) {
                    throw new BusinessException("GW-003", "FieldMapping (sourceType=STEP_RESPONSE_ARRAY_FIND) thieu "
                            + "sourceArrayField/filterField/filterValue/sourceElementField.");
                }
            }
        }

        validateBranching(dto, orders);
        validateParallelGroups(dto);
        validateCompensationConfig(dto);
        validateResponseCache(dto);
        validateFlagRules(dto, orders);
        validateResponseMappings(dto, orders);
    }

    /**
     * Kiem tra cau hinh ResponseMapping (ghép field tu bat ky step nao vao response
     * cuoi) - mirror cac validate* ben tren:
     * (1) mode phai la MERGE hoac REPLACE (den day spec chac chan khong rong - rong
     *     da duoc coi nhu "khong dung" va bo qua);
     * (2) moi mapping: sourceStepOrder PHAI ton tai trong steps (chan - khac voi
     *     validateFlagRules bo qua step khong chay: o day la loi cau hinh CHAC CHAN,
     *     vi mapping doc ctx.stepResults cua step do);
     * (3) sourceField/targetField bat buoc non-blank.
     * targetField TRUNG nhau giua cac mapping KHONG chan - mapping co mappingOrder
     * lon hon ghi sau (de len), la hanh vi chu dong khi can ghi de.
     * Package-private de EndpointTryService.tryAdhoc() dung chung qua validate().
     */
    void validateResponseMappings(com.bccs.gatewaymanager.dto.EndpointRequestDto dto, List<Integer> orders) {
        var spec = dto.responseMapping();
        if (com.bccs.gatewaymanager.dto.ResponseMappingSpecDto.isEmpty(spec)) {
            return;
        }
        if (!com.bccs.gatewaymanager.dto.ResponseMappingSpecDto.MODE_MERGE.equals(spec.mode())
                && !com.bccs.gatewaymanager.dto.ResponseMappingSpecDto.MODE_REPLACE.equals(spec.mode())) {
            throw new BusinessException("GW-003",
                    "ResponseMapping: mode phai la MERGE hoac REPLACE (gia tri hien tai: '" + spec.mode() + "').");
        }
        var orderSet = new java.util.HashSet<>(orders);
        for (var m : spec.mappings()) {
            if (m.sourceField() == null || m.sourceField().isBlank()
                    || m.targetField() == null || m.targetField().isBlank()) {
                throw new BusinessException("GW-003",
                        "ResponseMapping (step " + m.sourceStepOrder() + "): sourceField va targetField bat buoc khong de trong.");
            }
            if (!orderSet.contains(m.sourceStepOrder())) {
                throw new BusinessException("GW-003",
                        "ResponseMapping: sourceStepOrder=" + m.sourceStepOrder() + " khong ton tai trong steps cua endpoint.");
            }
        }
    }

    /**
     * Kiem tra mang RULE CỜ (V5) - mirror cac validateBranching/validateParallelGroups:
     * (1) chi dung khi sequential=true (Rule chon step bat dau cua 1 chuoi tuan tu;
     *     endpoint non-sequential khong co "step bat dau" co y nghia);
     * (2) actionType hop le + all-or-nothing (START_STEP phai co startStepOrder ton
     *     tai trong steps; BLOCK phai co blockErrorCode);
     * (3) priority duy nhat (thu tu danh gia nghiem ngat, 2 rule cung priority khong
     *     xac dinh ai khop truoc);
     * (4) khong nhom dieu kien nao rong (nhom AND rong luon "dung" -> rule khop vo
     *     dieu kien, de lay nham traffic cua ca endpoint);
     * (5) flagCode trong 1 nhom KHONG trung (2 dieu kien cho cung 1 cờ trong 1 nhom
     *     la mau thuan hoac du thua - A=T ∧ A=F khong bao gio khop, A=T ∧ A=T thua);
     * (6) flagCode PHAI ton tai thuoc doi hien tai (GWM_FLAG) - tham chieu cheo-doi
     *     tuong tu findUpstreamOrThrow.
     * Package-private de EndpointTryService.tryAdhoc() dung chung qua validate().
     */
    void validateFlagRules(com.bccs.gatewaymanager.dto.EndpointRequestDto dto, List<Integer> orders) {
        if (dto.flagRules() == null || dto.flagRules().isEmpty()) {
            return;
        }
        if (!dto.sequential()) {
            throw new BusinessException("GW-003",
                    "Rule cờ chỉ áp dụng được khi sequential=true (chọn step bắt đầu của 1 chuỗi tuân tự) - "
                            + "endpoint sequential=false mỗi step độc lập, không có khái niệm 'bắt đầu từ step nào'.");
        }
        var orderSet = new java.util.HashSet<>(orders);
        var priorities = new java.util.HashSet<Integer>();
        for (var rule : dto.flagRules()) {
            if (rule.actionType() == null || rule.actionType().isBlank()) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': thiếu actionType (START_STEP hoặc BLOCK).");
            }
            if (rule.conditions() == null || rule.conditions().isEmpty()
                    || rule.conditions().stream().anyMatch(List::isEmpty)) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': mỗi nhóm điều kiện phải có ít nhất 1 điều kiện (nhóm rỗng luôn khớp - vô nghĩa).");
            }
            if (!priorities.add(rule.priority())) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': priority " + rule.priority() + " bị trùng với Rule khác - thứ tự đánh giá không xác định.");
            }
            boolean block = com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK.equals(rule.actionType());
            boolean startStep = com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP.equals(rule.actionType());
            if (!block && !startStep) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': actionType phải là START_STEP hoặc BLOCK (đang là '" + rule.actionType() + "').");
            }
            if (startStep && (rule.startStepOrder() == null || !orderSet.contains(rule.startStepOrder()))) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': startStepOrder trỏ tới step không tồn tại.");
            }
            if (block && (rule.blockErrorCode() == null || rule.blockErrorCode().isBlank())) {
                throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                        + "': actionType=BLOCK phải khai blockErrorCode (mã lỗi trả về client).");
            }
            for (List<com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto> group : rule.conditions()) {
                var codesInGroup = new java.util.HashSet<String>();
                for (var cond : group) {
                    if (cond.flagCode() == null || cond.flagCode().isBlank()) {
                        throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                                + "': có điều kiện thiếu flagCode.");
                    }
                    if (!codesInGroup.add(cond.flagCode())) {
                        throw new BusinessException("GW-003", "Rule cờ '" + rule.name()
                                + "': flagCode '" + cond.flagCode() + "' khai 2 lần trong 1 nhóm (AND) - mau thuan hoặc dư thừa.");
                    }
                    if (!flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(CurrentTeamContext.require(), cond.flagCode())) {
                        throw new BusinessException("GW-FLAG-404", "Rule cờ '" + rule.name()
                                + "': cờ '" + cond.flagCode() + "' chưa được khai báo trong trang Cờ nghiệp vụ của đội này.");
                    }
                }
            }
        }
    }

    /**
     * Cache TOAN BO response cho MOI client cung tham so (khac Idempotency-Key - xem
     * EndpointConfig.responseCacheEnabled) - CHAN CUNG (khong chi canh bao mem nhu
     * retryEnabled) neu endpoint hoac BAT KY step nao khong phai GET/POST: cho phep
     * tren step mutating se khien 2 client khac nhau goi cung tham so nhan NHAM ket
     * qua mutation cua nhau (khong co lenh goi that nao chay cho client thu 2).
     *
     * POST duoc cho phep tu 2026-09 (truoc do chi GET) - mirror dung quyet dinh da ap
     * dung cho cache per-step (xem UpstreamHttpExecutor.call()): POST dung lam API tim
     * kiem/tra cuu truyen filter qua body van la thuan doc, khong mutating. Nguoi quan
     * ly cau hinh (bat responseCacheEnabled) tu chiu trach nhiem dam bao dieu do dung -
     * he thong chi con chan cung PUT/PATCH/DELETE (gan nhu chac chan la mutating).
     */
    private void validateResponseCache(EndpointRequestDto dto) {
        if (!dto.responseCacheEnabled()) {
            return;
        }
        if (!isGetOrPost(dto.method())) {
            throw new BusinessException("GW-003",
                    "Cache toan bo response chi bat duoc khi endpoint la GET/POST (dang la " + dto.method() + ").");
        }
        for (var s : dto.steps()) {
            if (!isGetOrPost(s.method())) {
                throw new BusinessException("GW-003", "Cache toan bo response chi bat duoc khi TOAN BO step deu la GET/POST "
                        + "(step '" + s.name() + "' dang la " + s.method() + ").");
            }
        }
    }

    private boolean isGetOrPost(com.bccs.gatewaymanager.entity.GatewayMethod method) {
        return method == com.bccs.gatewaymanager.entity.GatewayMethod.GET
                || method == com.bccs.gatewaymanager.entity.GatewayMethod.POST;
    }

    /**
     * Kiem tra cau hinh bu tru/rollback nghiep vu (saga best-effort, muc 6, xem
     * BackendStep.compensationUpstreamService + CompositeOrchestratorEngine.
     * runCompensations()) - 3 rang buoc V1:
     * (1) chi dung duoc khi sequential=true (bu tru chi kich hoat tu nhanh do trong
     *     CompositeOrchestratorEngine.handle());
     * (2) ca 3 field bu tru (compensationUpstreamServiceId/Method/UrlPattern) PHAI
     *     cung co hoac cung KHONG co (all-or-nothing) - tranh cau hinh nua vo;
     * (3) FieldMapping targetContext=COMPENSATION PHAI tro toi 1 step DA CO cau
     *     hinh bu tru (khong "mo coi", vo nghia neu step do khong bao gio bu tru).
     * Ton tai compensationUpstreamServiceId duoc validate MIEN PHI qua
     * EndpointMapper.findUpstreamOrThrow() luc map entity (quan he @ManyToOne that),
     * khong can kiem tra rieng o day.
     */
    private void validateCompensationConfig(EndpointRequestDto dto) {
        var stepsWithCompensation = new java.util.HashSet<Integer>();
        for (var s : dto.steps()) {
            boolean hasAny = s.compensationUpstreamServiceId() != null || s.compensationMethod() != null
                    || (s.compensationUrlPattern() != null && !s.compensationUrlPattern().isBlank());
            if (!hasAny) {
                continue;
            }
            if (!dto.sequential()) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': cau hinh bu tru/rollback chi ap dung duoc khi sequential=true.");
            }
            boolean hasAll = s.compensationUpstreamServiceId() != null && s.compensationMethod() != null
                    && s.compensationUrlPattern() != null && !s.compensationUrlPattern().isBlank();
            if (!hasAll) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': cau hinh bu tru phai co DU CA 3 (Upstream/Method/URL pattern), khong duoc thieu 1 phan.");
            }
            stepsWithCompensation.add(s.stepOrder());
        }

        if (dto.mappings() == null) {
            return;
        }
        for (var m : dto.mappings()) {
            if (m.targetContext() == com.bccs.gatewaymanager.entity.MappingTargetContext.COMPENSATION
                    && !stepsWithCompensation.contains(m.targetStepOrder())) {
                throw new BusinessException("GW-003", "FieldMapping (targetContext=COMPENSATION) tro toi step "
                        + m.targetStepOrder() + " nhung step do CHUA cau hinh bu tru.");
            }
        }
    }

    /**
     * Kiem tra "wave" song song trong 1 chuoi sequential (BackendStep.parallelGroup) -
     * 4 rang buoc V1 (rui ro thap, xem plan da duyet): (1) chi dung khi sequential=true;
     * (2) step trong wave khong duoc co conditionOperator/onErrorStepOrder rieng (V1
     * chua ho tro re nhanh/fallback TU 1 thanh vien wave); (3) khong step nao khac
     * duoc nhay/fallback TOI 1 step dang trong wave (wave chi vao duoc qua tien trinh
     * tuan tu tu nhien, tranh nhap nhang "nhay vao thi vao dung thanh vien nao"); (4)
     * stepOrder cua 1 wave PHAI LIEN TIEP (vi du {2,3}, khong duoc {2,4}) - neu khong,
     * logic runtime "tu dau wave nhay thang toi sau cuoi wave"
     * (CompositeOrchestratorEngine.executeSequentialChain()) se VO TINH BO QUA 1 step
     * "la" nam giua thuoc nhom/khong nhom khac.
     */
    private void validateParallelGroups(EndpointRequestDto dto) {
        boolean anyGroup = dto.steps().stream().anyMatch(s -> s.parallelGroup() != null);
        if (!anyGroup) {
            return;
        }
        if (!dto.sequential()) {
            throw new BusinessException("GW-003",
                    "parallelGroup chi ap dung duoc khi sequential=true (wave song song nam trong 1 chuoi tuan tu) - "
                            + "endpoint sequential=false da co co rieng parallelExecution cho toan bo step.");
        }
        for (var s : dto.steps()) {
            if (s.parallelGroup() != null && (s.conditionOperator() != null || s.onErrorStepOrder() != null || hasSwitch(s))) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': step trong 1 nhom song song (parallelGroup) khong duoc khai bao "
                        + "conditionOperator/onErrorStepOrder rieng (chua ho tro o V1).");
            }
        }

        var groupedStepOrders = dto.steps().stream()
                .filter(s -> s.parallelGroup() != null)
                .map(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder)
                .collect(java.util.stream.Collectors.toSet());
        for (var s : dto.steps()) {
            boolean jumpsIntoGroup = (s.nextStepOrderIfTrue() != null && groupedStepOrders.contains(s.nextStepOrderIfTrue()))
                    || (s.nextStepOrderIfFalse() != null && groupedStepOrders.contains(s.nextStepOrderIfFalse()))
                    || (hasSwitch(s) && s.switchBranches().stream().anyMatch(b -> b.nextStepOrder() != null
                            && groupedStepOrders.contains(b.nextStepOrder())))
                    || (s.onErrorStepOrder() != null && groupedStepOrders.contains(s.onErrorStepOrder()));
            if (jumpsIntoGroup) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': khong duoc re nhanh/fallback TOI 1 step dang trong nhom song song (parallelGroup) - "
                        + "wave chi duoc vao qua tien trinh tuan tu tu nhien.");
            }
        }

        Map<Integer, List<Integer>> byGroup = dto.steps().stream()
                .filter(s -> s.parallelGroup() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        com.bccs.gatewaymanager.dto.BackendStepDto::parallelGroup,
                        java.util.stream.Collectors.mapping(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder,
                                java.util.stream.Collectors.toList())));
        for (var entry : byGroup.entrySet()) {
            List<Integer> sorted = entry.getValue().stream().sorted().toList();
            int min = sorted.get(0);
            int max = sorted.get(sorted.size() - 1);
            if (max - min + 1 != sorted.size()) {
                throw new BusinessException("GW-003", "Nhom song song #" + entry.getKey()
                        + ": cac stepOrder " + sorted + " phai LIEN TIEP (khong co khoang trong) - "
                        + "neu khong 1 step khac nam giua se bi bo qua khi thuc thi.");
            }
        }
    }

    /** true neu endpoint dung BAT KY co che "nhay" nao lam thu tu thuc thi khong con dam bao tang dan theo stepOrder - re nhanh (conditionOperator) HOAC fallback loi (onErrorStepOrder). */
    private boolean usesBranching(EndpointRequestDto dto) {
        return dto.steps().stream().anyMatch(s -> s.conditionOperator() != null || s.onErrorStepOrder() != null || hasSwitch(s));
    }

    /** Kiem tra field re nhanh (P1-5) tham chieu dung step co that + khong tao vong lap. */
    private void validateBranching(EndpointRequestDto dto, List<Integer> orders) {
        var orderSet = new java.util.HashSet<>(orders);
        for (var s : dto.steps()) {
            if (s.nextStepOrderIfTrue() != null && !orderSet.contains(s.nextStepOrderIfTrue())) {
                throw new BusinessException("GW-003", "Step '" + s.name() + "': nextStepOrderIfTrue tro toi step "
                        + s.nextStepOrderIfTrue() + " khong ton tai.");
            }
            if (s.nextStepOrderIfFalse() != null && !orderSet.contains(s.nextStepOrderIfFalse())) {
                throw new BusinessException("GW-003", "Step '" + s.name() + "': nextStepOrderIfFalse tro toi step "
                        + s.nextStepOrderIfFalse() + " khong ton tai.");
            }
            if (s.onErrorStepOrder() != null && !orderSet.contains(s.onErrorStepOrder())) {
                throw new BusinessException("GW-003", "Step '" + s.name() + "': onErrorStepOrder tro toi step "
                        + s.onErrorStepOrder() + " khong ton tai.");
            }
            boolean hasFalseError = s.falseErrorCode() != null && !s.falseErrorCode().isBlank();
            if (!hasFalseError && s.falseErrorMessage() != null && !s.falseErrorMessage().isBlank()) {
                throw new BusinessException("GW-003", "Step '" + s.name() + "': co thong diep bao loi nhung thieu ma loi.");
            }
            if (hasFalseError) {
                if (s.conditionOperator() == null && !hasSwitch(s)) {
                    throw new BusinessException("GW-003", "Step '" + s.name()
                            + "': chi bao loi duoc cho nhanh SAI cua rẽ nhánh (can dieu kien hoac switch).");
                }
                if (s.nextStepOrderIfFalse() != null) {
                    throw new BusinessException("GW-003", "Step '" + s.name()
                            + "': nhanh SAI chi duoc chon MOT trong hai: chuyen sang step khac HOAC bao loi.");
                }
                if (s.falseErrorMessage() == null || s.falseErrorMessage().isBlank()) {
                    throw new BusinessException("GW-003", "Step '" + s.name() + "': bao loi nhanh SAI can co thong diep.");
                }
                if (s.falseErrorCode().length() > 60 || s.falseErrorMessage().length() > 500) {
                    throw new BusinessException("GW-003", "Step '" + s.name()
                            + "': ma loi toi da 60 ky tu, thong diep toi da 500 ky tu.");
                }
            }
            if (s.conditionOperator() != null
                    && s.conditionSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                    && (s.conditionSourceStepOrder() == null || !orderSet.contains(s.conditionSourceStepOrder()))) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (sourceType=STEP_RESPONSE) thieu conditionSourceStepOrder hop le.");
            }
            // V6 - conditionGroups: dieu kien NHIEU NGUON (REQUEST_BODY/STEP_RESPONSE)
            // NHIEU NHOM, tong quat hoa conditionFlagGroups (V5b) sang cho du lieu
            // request/response thuc. Khac null/rong -> engine UU TIEN danh gia NHOM
            // nay (bo qua HOAN TOAN dieu kien 1 co don le VA nhanh SYSTEM_FLAG, xem
            // CompositeOrchestratorEngine.evaluateCondition()) nen validate CHINH noi
            // dung nhom nay, moi dieu kien LA phai co du nguon/field/toan tu hop le.
            boolean hasConditionGroups = s.conditionOperator() != null
                    && s.conditionGroups() != null && !s.conditionGroups().isEmpty();
            if (hasConditionGroups) {
                for (int gi = 0; gi < s.conditionGroups().size(); gi++) {
                    var group = s.conditionGroups().get(gi);
                    if (group == null || group.isEmpty()) {
                        throw new BusinessException("GW-003", "Step '" + s.name()
                                + "': nhom dieu kien thu " + (gi + 1) + " dang rong - hay them dieu kien hoac xoa nhom rong.");
                    }
                    for (int ci = 0; ci < group.size(); ci++) {
                        validateConditionNode("Step '" + s.name() + "' (nhom #" + (gi + 1) + ", dieu kien #" + (ci + 1) + ")",
                                group.get(ci), orderSet, 1);
                    }
                }
            }
            if (hasSwitch(s)) {
                for (int bi = 0; bi < s.switchBranches().size(); bi++) {
                    var branch = s.switchBranches().get(bi);
                    String branchLabel = "Step '" + s.name() + "' (nhanh #" + (bi + 1) + ")";
                    if (branch == null || branch.groups() == null || branch.groups().isEmpty()) {
                        throw new BusinessException("GW-003", branchLabel + ": nhanh chua co dieu kien.");
                    }
                    if (branch.nextStepOrder() == null || !orderSet.contains(branch.nextStepOrder())) {
                        throw new BusinessException("GW-003", branchLabel + ": nextStepOrder tro toi step khong ton tai.");
                    }
                    for (int gi = 0; gi < branch.groups().size(); gi++) {
                        var group = branch.groups().get(gi);
                        if (group == null || group.isEmpty()) {
                            throw new BusinessException("GW-003", branchLabel + ": nhom dieu kien thu " + (gi + 1) + " dang rong.");
                        }
                        for (int ci = 0; ci < group.size(); ci++) {
                            validateConditionNode(branchLabel + " (nhom #" + (gi + 1) + ", dieu kien #" + (ci + 1) + ")",
                                    group.get(ci), orderSet, 1);
                        }
                    }
                }
            }
            // SYSTEM_FLAG (V5): conditionSourceField = MA CỜ (khong phai dot-path),
            // khong dung conditionSourceStepOrder. Cờ phai ton tai thuoc team +
            // conditionExpectedValue phai la "true"/"false" (engine so sanh boolean).
            // V5b (theo "vande"): conditionFlagGroups = dieu kien NHIEU co NHIEU NHOM
            // (AND trong nhom, OR giua nhom) - khi co groups thi KHONG bat buoc dieu
            // kien 1 co don le nua; engine uu tien danh gia groups.
            if (s.conditionOperator() != null
                    && s.conditionSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.SYSTEM_FLAG) {
                boolean hasGroups = s.conditionFlagGroups() != null && !s.conditionFlagGroups().isEmpty();
                if (hasGroups) {
                    // Nhom: moi dieu kien phai tham chieu cờ ton tai thuoc team.
                    for (int gi = 0; gi < s.conditionFlagGroups().size(); gi++) {
                        var group = s.conditionFlagGroups().get(gi);
                        if (group == null || group.isEmpty()) {
                            throw new BusinessException("GW-003", "Step '" + s.name()
                                    + "': nhom dieu kien cờ thứ " + (gi + 1) + " trong (sourceType=SYSTEM_FLAG) đang rỗng - hay thêm điều kiện hoặc xóa nhóm trống.");
                        }
                        for (var cond : group) {
                            if (cond.flagCode() == null || cond.flagCode().isBlank()) {
                                throw new BusinessException("GW-003", "Step '" + s.name()
                                        + "': dieu kien cờ trong nhóm thứ " + (gi + 1) + " thieu ma cờ.");
                            }
                            if (!flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(CurrentTeamContext.require(), cond.flagCode())) {
                                throw new BusinessException("GW-FLAG-404", "Step '" + s.name()
                                        + "': cờ '" + cond.flagCode()
                                        + "' chưa được khai báo trong trang Cờ nghiệp vụ của đội này.");
                            }
                        }
                    }
                } else {
                    if (s.conditionSourceField() == null || s.conditionSourceField().isBlank()) {
                        throw new BusinessException("GW-003", "Step '" + s.name()
                                + "': dieu kien re nhanh (sourceType=SYSTEM_FLAG) thieu conditionSourceField (ma cờ).");
                    }
                    if (!flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(CurrentTeamContext.require(), s.conditionSourceField())) {
                        throw new BusinessException("GW-FLAG-404", "Step '" + s.name()
                                + "': cờ '" + s.conditionSourceField()
                                + "' chưa được khai báo trong trang Cờ nghiệp vụ của đội này.");
                    }
                    String expected = s.conditionExpectedValue();
                    if (expected == null || (!"true".equalsIgnoreCase(expected.trim()) && !"false".equalsIgnoreCase(expected))) {
                        throw new BusinessException("GW-003", "Step '" + s.name()
                                + "': dieu kien re nhanh (sourceType=SYSTEM_FLAG) can conditionExpectedValue la 'true' hoac 'false' (dang la '"
                                + expected + "').");
                    }
                }
            }
            // V5b: khi SYSTEM_FLAG co conditionFlagGroups (nhieu nhom), dieu kien 1 co
            // don le khong con duoc doc boi engine nen khong bat buoc expected nua.
            boolean systemFlagWithGroups = s.conditionOperator() != null
                    && s.conditionSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.SYSTEM_FLAG
                    && s.conditionFlagGroups() != null && !s.conditionFlagGroups().isEmpty();
            // V6: conditionGroups cung lam dieu kien 1 co don le khong con duoc doc -
            // xem hasConditionGroups da tinh o tren, mirror dung ly do systemFlagWithGroups.
            if (!systemFlagWithGroups && !hasConditionGroups && s.conditionOperator() != null) {
                checkOperatorValue("Step '" + s.name() + "'", s.conditionOperator(), s.conditionExpectedValue());
            }
            // 4 toan tu so sanh SO (>,>=,<,<=): conditionExpectedValue BAT BUOC parse
            // duoc thanh so - khac EQUALS/NOT_EQUALS chap nhan bat ky chuoi nao.
            if (!hasConditionGroups && isNumericConditionOperator(s.conditionOperator())
                    && s.conditionExpectedValue() != null && !s.conditionExpectedValue().isBlank()
                    && !isParsableAsNumber(s.conditionExpectedValue())) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (" + s.conditionOperator() + ") can conditionExpectedValue la SO, nhung dang la '"
                        + s.conditionExpectedValue() + "'.");
            }
        }
        detectBranchCycle(dto);
    }

    private static final tools.jackson.databind.ObjectMapper CONDITION_JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();
    private static final int MAX_REGEX_LENGTH = 200;

    private static void checkOperatorValue(String label, com.bccs.gatewaymanager.entity.ConditionOperator op, String expected) {
        if (op.needsExpectedValue() && (expected == null || expected.isBlank())) {
            throw new BusinessException("GW-003", label + ": toan tu (" + op + ") thieu expectedValue.");
        }
        if (op == com.bccs.gatewaymanager.entity.ConditionOperator.IN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.NOT_IN) {
            tools.jackson.databind.JsonNode list;
            try {
                list = CONDITION_JSON.readTree(expected);
            } catch (RuntimeException e) {
                list = null;
            }
            if (list == null || !list.isArray()) {
                throw new BusinessException("GW-003", label + ": toan tu (" + op
                        + ") can expectedValue la mang JSON, vd [\"A\",\"B\"] (dang la '" + expected + "').");
            }
        }
        if (op == com.bccs.gatewaymanager.entity.ConditionOperator.MATCHES_REGEX) {
            if (expected.length() > MAX_REGEX_LENGTH) {
                throw new BusinessException("GW-003", label + ": regex dai qua " + MAX_REGEX_LENGTH + " ky tu.");
            }
            try {
                java.util.regex.Pattern.compile(expected);
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new BusinessException("GW-003", label + ": regex khong hop le (" + e.getDescription() + ").");
            }
        }
    }

    private static final int MAX_CONDITION_DEPTH = 5;
    private static final java.util.Set<com.bccs.gatewaymanager.entity.FieldMappingSourceType> VALUE_SOURCES = java.util.Set.of(
            com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY,
            com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE,
            com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM,
            com.bccs.gatewaymanager.entity.FieldMappingSourceType.HEADER);

    /** Kiem tra 1 nut cua cay dieu kien (de quy): nhom (AND/OR, long nhau <= MAX_CONDITION_DEPTH) hoac la (nguon, field, toan tu, gia tri/vế phải). */
    private void validateConditionNode(String label, com.bccs.gatewaymanager.dto.ConditionRuleDto node,
                                       java.util.Set<Integer> orderSet, int depth) {
        if (node == null) {
            throw new BusinessException("GW-003", label + ": dieu kien rong.");
        }
        if (depth > MAX_CONDITION_DEPTH) {
            throw new BusinessException("GW-003", label + ": cay dieu kien long nhau qua " + MAX_CONDITION_DEPTH + " cap.");
        }
        if (node.hasQuantifier()) {
            validateQuantifierNode(label, node, orderSet, depth);
            return;
        }
        if (node.isGroup()) {
            if (node.children().isEmpty()) {
                throw new BusinessException("GW-003", label + ": nhom dieu kien dang rong.");
            }
            if (node.logic() != null && !"AND".equalsIgnoreCase(node.logic()) && !"OR".equalsIgnoreCase(node.logic())) {
                throw new BusinessException("GW-003", label + ": logic nhom phai la AND hoac OR (dang la '" + node.logic() + "').");
            }
            for (int i = 0; i < node.children().size(); i++) {
                validateConditionNode(label + " > nhom con #" + (i + 1), node.children().get(i), orderSet, depth + 1);
            }
            return;
        }
        if (node.operator() == null) {
            throw new BusinessException("GW-003", label + ": thieu toan tu (operator).");
        }
        if (node.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.SYSTEM_FLAG) {
            if (node.sourceField() == null || node.sourceField().isBlank()) {
                throw new BusinessException("GW-003", label + ": thieu ma co (sourceField).");
            }
            if (node.operator() != com.bccs.gatewaymanager.entity.ConditionOperator.EQUALS
                    && node.operator() != com.bccs.gatewaymanager.entity.ConditionOperator.NOT_EQUALS) {
                throw new BusinessException("GW-003", label + ": cờ chi dung toan tu EQUALS/NOT_EQUALS (dang la " + node.operator() + ").");
            }
            String expected = node.expectedValue();
            if (expected == null || (!"true".equalsIgnoreCase(expected.trim()) && !"false".equalsIgnoreCase(expected.trim()))) {
                throw new BusinessException("GW-003", label + ": cờ can expectedValue la 'true' hoac 'false'.");
            }
            if (!flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(CurrentTeamContext.require(), node.sourceField())) {
                throw new BusinessException("GW-FLAG-404", label + ": cờ '" + node.sourceField()
                        + "' chưa được khai báo trong trang Cờ nghiệp vụ của đội này.");
            }
            return;
        }
        if (!VALUE_SOURCES.contains(node.sourceType())) {
            throw new BusinessException("GW-003", label + ": nguon khong ho tro (" + node.sourceType() + ").");
        }
        if (node.sourceField() == null || node.sourceField().isBlank()) {
            throw new BusinessException("GW-003", label + ": thieu sourceField.");
        }
        if (node.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                && (node.sourceStepOrder() == null || !orderSet.contains(node.sourceStepOrder()))) {
            throw new BusinessException("GW-003", label + ": sourceType=STEP_RESPONSE thieu sourceStepOrder hop le.");
        }
        if (node.compareSourceField() != null) {
            if (node.compareSourceField().isBlank()) {
                throw new BusinessException("GW-003", label + ": thieu field ve phai (compareSourceField).");
            }
            if (node.expectedValue() != null && !node.expectedValue().isBlank()) {
                throw new BusinessException("GW-003", label + ": chi duoc chon MOT trong 2: expectedValue hoac vế phải (field khac).");
            }
            if (!VALUE_SOURCES.contains(node.compareSourceType())) {
                throw new BusinessException("GW-003", label + ": nguon ve phai khong hop le (" + node.compareSourceType() + ").");
            }
            if (node.compareSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                    && (node.compareSourceStepOrder() == null || !orderSet.contains(node.compareSourceStepOrder()))) {
                throw new BusinessException("GW-003", label + ": vế phải sourceType=STEP_RESPONSE thieu sourceStepOrder hop le.");
            }
            return;
        }
        checkOperatorValue(label, node.operator(), node.expectedValue());
        if (isNumericConditionOperator(node.operator()) && node.expectedValue() != null
                && !isParsableAsNumber(node.expectedValue())) {
            throw new BusinessException("GW-003", label + ": toan tu (" + node.operator()
                    + ") can expectedValue la SO, nhung dang la '" + node.expectedValue() + "'.");
        }
    }

    private static boolean isNumericConditionOperator(com.bccs.gatewaymanager.entity.ConditionOperator op) {
        return op == com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN_OR_EQUAL
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN_OR_EQUAL
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.SIZE_EQUALS
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.SIZE_GREATER_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.SIZE_GREATER_THAN_OR_EQUAL
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.SIZE_LESS_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.SIZE_LESS_THAN_OR_EQUAL;
    }

    /** Lap mang: mang phai lay tu nguon gia tri hop le; ANY/ALL/COUNT; COUNT can toan tu so sanh + nguong so. */
    private void validateQuantifierNode(String label, com.bccs.gatewaymanager.dto.ConditionRuleDto node,
                                        java.util.Set<Integer> orderSet, int depth) {
        if (node.children().isEmpty()) {
            throw new BusinessException("GW-003", label + ": lap mang dang rong - hay them dieu kien cho tung phan tu.");
        }
        if (!VALUE_SOURCES.contains(node.sourceType())) {
            throw new BusinessException("GW-003", label + ": mang can lap phai lay tu nguon REQUEST_BODY/STEP_RESPONSE/QUERY_PARAM/HEADER.");
        }
        if (node.sourceField() == null || node.sourceField().isBlank()) {
            throw new BusinessException("GW-003", label + ": thieu field mang can lap (sourceField).");
        }
        if (node.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                && (node.sourceStepOrder() == null || !orderSet.contains(node.sourceStepOrder()))) {
            throw new BusinessException("GW-003", label + ": mang STEP_RESPONSE thieu sourceStepOrder hop le.");
        }
        String q = node.quantifier();
        if (!com.bccs.gatewaymanager.dto.ConditionRuleDto.QUANTIFIER_ANY.equals(q)
                && !com.bccs.gatewaymanager.dto.ConditionRuleDto.QUANTIFIER_ALL.equals(q)
                && !com.bccs.gatewaymanager.dto.ConditionRuleDto.QUANTIFIER_COUNT.equals(q)) {
            throw new BusinessException("GW-003", label + ": quantifier phai la ANY, ALL hoac COUNT (dang la '" + q + "').");
        }
        if (node.logic() != null && !"AND".equalsIgnoreCase(node.logic()) && !"OR".equalsIgnoreCase(node.logic())) {
            throw new BusinessException("GW-003", label + ": logic phai la AND hoac OR (dang la '" + node.logic() + "').");
        }
        if (com.bccs.gatewaymanager.dto.ConditionRuleDto.QUANTIFIER_COUNT.equals(q)) {
            var op = node.countOperator();
            if (op != com.bccs.gatewaymanager.entity.ConditionOperator.EQUALS
                    && op != com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN
                    && op != com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN_OR_EQUAL
                    && op != com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN
                    && op != com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN_OR_EQUAL) {
                throw new BusinessException("GW-003", label + ": COUNT can toan tu so sanh (EQUALS/GT/GTE/LT/LTE).");
            }
            if (node.countValue() == null || !isParsableAsNumber(node.countValue())) {
                throw new BusinessException("GW-003", label + ": COUNT can nguong la SO (dang la '" + node.countValue() + "').");
            }
        }
        for (int i = 0; i < node.children().size(); i++) {
            validateConditionNode(label + " > phan tu, dieu kien #" + (i + 1), node.children().get(i), orderSet, depth + 1);
        }
    }

    private static boolean hasSwitch(com.bccs.gatewaymanager.dto.BackendStepDto s) {
        return s.switchBranches() != null && !s.switchBranches().isEmpty();
    }

    private static boolean isParsableAsNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * DFS 3 mau phat hien cycle trong do thi re nhanh cua 1 endpoint - mirror
     * dung thuat toan DependencyAnalyzer.detectCycle() (o pham vi giua CAC
     * ENDPOINT) nhung ap dung cho pham vi step TRONG 1 endpoint. Voi 1 step,
     * tap "co the di toi" = {nextStepOrderIfTrue, nextStepOrderIfFalse} neu co
     * dieu kien (xet CA 2 nhanh vi luc luu chua biet nhanh nao se chay that),
     * hoac {stepOrder nho nhat lon hon no} neu khong co dieu kien (dung natural-next
     * y het CompositeOrchestratorEngine.determineNextStepOrder()).
     */
    private void detectBranchCycle(EndpointRequestDto dto) {
        var steps = dto.steps();
        List<Integer> allOrders = steps.stream().map(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder).sorted().toList();
        Map<Integer, com.bccs.gatewaymanager.dto.BackendStepDto> byOrder = steps.stream()
                .collect(java.util.stream.Collectors.toMap(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder, s -> s));

        Map<Integer, List<Integer>> adjacency = new java.util.HashMap<>();
        for (var s : steps) {
            List<Integer> next = new ArrayList<>();
            if (hasSwitch(s)) {
                for (var branch : s.switchBranches()) {
                    if (branch.nextStepOrder() != null) next.add(branch.nextStepOrder());
                }
                if (s.nextStepOrderIfFalse() != null) next.add(s.nextStepOrderIfFalse());
            } else if (s.conditionOperator() != null) {
                if (s.nextStepOrderIfTrue() != null) next.add(s.nextStepOrderIfTrue());
                if (s.nextStepOrderIfFalse() != null) next.add(s.nextStepOrderIfFalse());
            } else {
                // Step trong 1 "wave" song song (parallelGroup) cung roi vao day (luon co
                // conditionOperator=null, da validate o validateParallelGroups()) - canh tinh
                // ra la "stepOrder ke tiep lon hon minh", vi du group {2,3} thi step2->step3 (chinh
                // no) roi step3->that-su-sau-wave. Chuoi canh nay LIEN TUC/khong lui, khong tao
                // cycle gia (rule "khong duoc nhay/fallback TOI 1 step trong wave" da dam bao
                // KHONG co canh nao khac tro VAO giua group) - khong can logic rieng cho wave o day.
                allOrders.stream().filter(o -> o > s.stepOrder()).min(Integer::compareTo).ifPresent(next::add);
            }
            // Fallback loi (onErrorStepOrder) la 1 canh THEM, doc lap voi conditionOperator o
            // tren - 1 step co the vua co dieu kien re nhanh vua co fallback loi rieng.
            if (s.onErrorStepOrder() != null) {
                next.add(s.onErrorStepOrder());
            }
            adjacency.put(s.stepOrder(), next);
        }

        var visited = new java.util.HashSet<Integer>();
        for (Integer order : allOrders) {
            if (!visited.contains(order)) {
                detectCycleDfs(order, adjacency, visited, new java.util.LinkedHashSet<>(), byOrder);
            }
        }
    }

    private void detectCycleDfs(int nodeOrder, Map<Integer, List<Integer>> adjacency, java.util.Set<Integer> visited,
                                 java.util.LinkedHashSet<Integer> pathStack, Map<Integer, com.bccs.gatewaymanager.dto.BackendStepDto> byOrder) {
        visited.add(nodeOrder);
        pathStack.add(nodeOrder);
        for (Integer next : adjacency.getOrDefault(nodeOrder, List.of())) {
            if (pathStack.contains(next)) {
                String desc = pathStack.stream().map(o -> "Step " + o + " (" + byOrder.get(o).name() + ")")
                        .collect(java.util.stream.Collectors.joining(" -> "));
                throw new BusinessException("GW-BRANCH-CYCLE",
                        "Phat hien vong lap re nhanh giua cac step: " + desc + " -> Step " + next
                                + " - se goi vo han lan khi chay that. Phai sua truoc khi luu.");
            }
            if (!visited.contains(next)) {
                detectCycleDfs(next, adjacency, visited, pathStack, byOrder);
            }
        }
        pathStack.remove(nodeOrder);
    }
}
