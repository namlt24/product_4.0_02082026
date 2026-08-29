package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointConfigRepository repository;
    private final EndpointMapper mapper;
    private final EndpointRegistryCache registryCache;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final EndpointVersionService versionService;

    @Transactional(readOnly = true)
    public List<EndpointResponseDto> list(String search) {
        List<EndpointConfig> found = (search == null || search.isBlank())
                ? repository.findAllByOrderByUpdatedAtDesc()
                : repository.search(search.trim());
        return found.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public EndpointResponseDto get(String id) {
        return mapper.toResponseDto(findOrThrow(id));
    }

    @Transactional
    public EndpointResponseDto create(EndpointRequestDto dto) {
        rejectReservedPath(dto.path());
        validateStepOrders(dto);
        if (repository.existsByPath(dto.path())) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        EndpointConfig entity = mapper.toEntity(dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        rejectIfCyclic();
        registryCache.reload();
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
        validateStepOrders(dto);
        EndpointConfig entity = findOrThrow(id);
        if (repository.existsByPathAndIdNot(dto.path(), id)) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        mapper.updateEntity(entity, dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        rejectIfCyclic();
        registryCache.reload();
        versionService.recordSnapshot(saved, changeType);
        log.info("Da cap nhat endpoint ({}): {} {}", changeType, saved.getMethod(), saved.getPath());
        return result;
    }

    @Transactional
    public void delete(String id) {
        EndpointConfig entity = findOrThrow(id);
        versionService.deleteAllForEndpoint(id);
        repository.delete(entity);
        registryCache.reload();
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

    private EndpointConfig findOrThrow(String id) {
        return repository.findById(id)
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

    /** Kiem tra: stepOrder phai bat dau tu 1, khong trung, va mapping phai tham chieu step co that + hop le. */
    private void validateStepOrders(EndpointRequestDto dto) {
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
                if (!usesBranching && needsSourceStep && m.sourceStepOrder() != null && m.sourceStepOrder() >= m.targetStepOrder()) {
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
            }
        }

        validateBranching(dto, orders);
        validateParallelGroups(dto);
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
            if (s.parallelGroup() != null && (s.conditionOperator() != null || s.onErrorStepOrder() != null)) {
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
        return dto.steps().stream().anyMatch(s -> s.conditionOperator() != null || s.onErrorStepOrder() != null);
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
            if (s.conditionOperator() != null
                    && s.conditionSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                    && (s.conditionSourceStepOrder() == null || !orderSet.contains(s.conditionSourceStepOrder()))) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (sourceType=STEP_RESPONSE) thieu conditionSourceStepOrder hop le.");
            }
            if (s.conditionOperator() != null
                    && s.conditionOperator() != com.bccs.gatewaymanager.entity.ConditionOperator.EXISTS
                    && s.conditionOperator() != com.bccs.gatewaymanager.entity.ConditionOperator.NOT_EXISTS
                    && (s.conditionExpectedValue() == null || s.conditionExpectedValue().isBlank())) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (" + s.conditionOperator() + ") thieu conditionExpectedValue.");
            }
            // 4 toan tu so sanh SO (>,>=,<,<=): conditionExpectedValue BAT BUOC parse
            // duoc thanh so - khac EQUALS/NOT_EQUALS chap nhan bat ky chuoi nao.
            if (isNumericConditionOperator(s.conditionOperator())
                    && s.conditionExpectedValue() != null && !s.conditionExpectedValue().isBlank()
                    && !isParsableAsNumber(s.conditionExpectedValue())) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (" + s.conditionOperator() + ") can conditionExpectedValue la SO, nhung dang la '"
                        + s.conditionExpectedValue() + "'.");
            }
        }
        detectBranchCycle(dto);
    }

    private static boolean isNumericConditionOperator(com.bccs.gatewaymanager.entity.ConditionOperator op) {
        return op == com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.GREATER_THAN_OR_EQUAL
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN
                || op == com.bccs.gatewaymanager.entity.ConditionOperator.LESS_THAN_OR_EQUAL;
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
            if (s.conditionOperator() != null) {
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
