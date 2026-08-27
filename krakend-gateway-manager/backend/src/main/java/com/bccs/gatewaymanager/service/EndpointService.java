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
                        && m.sourceType() != com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM;
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
            }
        }

        validateBranching(dto, orders);
    }

    private boolean usesBranching(EndpointRequestDto dto) {
        return dto.steps().stream().anyMatch(s -> s.conditionOperator() != null);
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
            if (s.conditionOperator() != null
                    && s.conditionSourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE
                    && (s.conditionSourceStepOrder() == null || !orderSet.contains(s.conditionSourceStepOrder()))) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (sourceType=STEP_RESPONSE) thieu conditionSourceStepOrder hop le.");
            }
            if (s.conditionOperator() != null
                    && (s.conditionOperator() == com.bccs.gatewaymanager.entity.ConditionOperator.EQUALS
                        || s.conditionOperator() == com.bccs.gatewaymanager.entity.ConditionOperator.NOT_EQUALS)
                    && (s.conditionExpectedValue() == null || s.conditionExpectedValue().isBlank())) {
                throw new BusinessException("GW-003", "Step '" + s.name()
                        + "': dieu kien re nhanh (" + s.conditionOperator() + ") thieu conditionExpectedValue.");
            }
        }
        detectBranchCycle(dto);
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
                allOrders.stream().filter(o -> o > s.stepOrder()).min(Integer::compareTo).ifPresent(next::add);
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
