package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetContext;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointServiceTest {

    @Mock
    private EndpointConfigRepository repository;
    @Mock
    private EndpointMapper mapper;
    @Mock
    private DependencyAnalyzer dependencyAnalyzer;
    @Mock
    private EndpointVersionService versionService;
    @Mock
    private GatewayCacheService cacheService;
    @Mock
    private UpstreamHttpExecutor upstreamHttpExecutor;
    @Mock
    private com.bccs.gatewaymanager.repository.GwmFlagRepository flagRepository;

    private static final String TEAM = "default";

    private EndpointService service;

    @BeforeEach
    void setUp() {
        service = new EndpointService(repository, mapper, dependencyAnalyzer, versionService, cacheService, upstreamHttpExecutor, flagRepository);
        // Moi method cua EndpointService gio doc CurrentTeamContext.require() - xem
        // ApiKeyAuthFilter, thiet lap gia tri gia dinh nay o tang test (khong co
        // request/filter that trong unit test).
        CurrentTeamContext.set(TEAM);
        lenient().when(repository.existsByTeamCodeAndPath(any(), any())).thenReturn(false);
        lenient().when(mapper.toEntity(any())).thenReturn(EndpointConfig.builder().id("ep-1").build());
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(mapper.toResponseDto(any())).thenReturn(
                new EndpointResponseDto("ep-1", "n", null, "/x", GatewayMethod.GET, true, "json",
                        List.of(), List.of(), null, null, false, 86400, false, false, 300, List.of(), null));
        lenient().when(dependencyAnalyzer.detectCycleWarningsOnly()).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private BackendStepDto step(int order) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /** Step voi dieu kien re nhanh - dung cho cac test P1-5 (validate next-step-not-exist, cycle...). */
    private BackendStepDto stepWithBranch(int order, Integer conditionSourceStepOrder, ConditionOperator operator,
                                           Integer nextIfTrue, Integer nextIfFalse) {
        return stepWithBranch(order, conditionSourceStepOrder, operator, nextIfTrue, nextIfFalse, "value");
    }

    private BackendStepDto stepWithBranch(int order, Integer conditionSourceStepOrder, ConditionOperator operator,
                                           Integer nextIfTrue, Integer nextIfFalse, String conditionExpectedValue) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                FieldMappingSourceType.STEP_RESPONSE, conditionSourceStepOrder, "field", operator, conditionExpectedValue,
                null, null, nextIfTrue, nextIfFalse, null, null, null, null, null, null);
    }

    /** Step voi onErrorStepOrder rieng (doc lap voi conditionOperator) - dung cho test fallback loi. */
    private BackendStepDto stepWithOnError(int order, Integer onErrorStepOrder) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, onErrorStepOrder, null, null, null, null, null);
    }

    /** Step trong 1 nhom song song (parallelGroup) - dung cho test wave/dependency execution. */
    private BackendStepDto stepWithGroup(int order, Integer parallelGroup) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, parallelGroup, null, null, null, null);
    }

    /** Step vua trong 1 nhom song song, vua khai bao conditionOperator rieng - CAU HINH SAI (V1 khong ho tro), dung de test reject. */
    private BackendStepDto stepWithGroupAndCondition(int order, Integer parallelGroup) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                FieldMappingSourceType.REQUEST_BODY, null, "x", ConditionOperator.EXISTS, null,
                null, null, null, null, null, parallelGroup, null, null, null, null);
    }

    /** Step co cau hinh bu tru/rollback (muc 6) - tham so null cho phep dung test "thieu 1 phan" (all-or-nothing). */
    private BackendStepDto stepWithCompensation(int order, String compUpstreamId, GatewayMethod compMethod, String compUrlPattern) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, null,
                compUpstreamId, "upComp", compMethod, compUrlPattern);
    }

    private EndpointRequestDto requestWithMapping(FieldMappingDto mapping) {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(mapping), false, null, false, false, 300, List.of(), null);
    }

    // ---- Ha thap rui ro phat sinh tu fix auth: path Data Plane khong duoc trung tien to /api hoac /actuator ----
    // (ApiKeyAuthFilter dang ky theo Servlet urlPattern "/api/*" - khop MOI request bat dau
    // bang /api bat ke Spring MVC se route no toi controller nao, nen 1 endpoint composite
    // dat path "/api/orders" se vo tinh bi doi API key du muc dich la Data Plane khong auth).

    @Test
    void create_rejectsPathStartingWithApi() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/api/orders", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-001");
    }

    @Test
    void create_rejectsPathStartingWithActuator() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/actuator/custom", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-001");
    }

    @Test
    void create_allowsPathThatOnlyContainsApiAsSegmentNotPrefix() {
        // "/apinormal" khong phai "/api" hay "/api/..." - khong bi chan (chi chan dung tien to).
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/apinormal", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);
        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Finding #6: validate sourceStepOrder < targetStepOrder + required source fields ----

    @Test
    void create_rejectsMappingWhenSourceStepOrderNotBeforeTargetStepOrder() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 2, "a", null, null, null,
                1, MappingTargetType.QUERY, "a", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankSourceFieldForStepResponse() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "  ", null, null, null,
                2, MappingTargetType.QUERY, "a", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankArrayAggregateFields() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE, 1,
                null, "", "code", null, 2, MappingTargetType.BODY_FIELD, "a", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_validMapping_succeeds() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "code", null, null, null,
                2, MappingTargetType.QUERY, "a", 0, null);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
    }

    // ---- QUERY_PARAM (nguon FieldMapping moi - doc query param cua chinh client): giong REQUEST_BODY,
    // KHONG can sourceStepOrder, nhung VAN can sourceField (ten query param can doc). ----

    @Test
    void create_queryParamMapping_khongCanSourceStepOrder_thanhCong() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                2, MappingTargetType.QUERY, "staffCode", 0, null);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
    }

    @Test
    void create_rejectsBlankSourceFieldForQueryParam() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "  ", null, null, null,
                2, MappingTargetType.QUERY, "staffCode", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    // ---- CONSTANT (nguon FieldMapping moi - hang so co dinh, khong doc tu request/response nao): khong
    // can sourceStepOrder/sourceField, nhung BAT BUOC constantValue. ----

    @Test
    void create_constantMapping_khongCanSourceStepOrderVaSourceField_thanhCong() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "low",
                2, MappingTargetType.QUERY, "priority", 0, null);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
    }

    @Test
    void create_rejectsBlankConstantValueForConstant() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "  ",
                2, MappingTargetType.QUERY, "priority", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsNullConstantValueForConstant() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, null,
                2, MappingTargetType.QUERY, "priority", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    // ---- STEP_RESPONSE_ARRAY_MERGE (nguon FieldMapping moi - gop N object trong 1 mang thanh
    // 1 object duy nhat): can sourceStepOrder + sourceArrayField (khong can sourceElementField),
    // CHI dung duoc voi targetType=BODY_FIELD. ----

    @Test
    void create_arrayMergeMapping_sourceArrayFieldVaTargetTypeBodyField_thanhCong() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
    }

    @Test
    void create_rejectsBlankSourceArrayFieldForArrayMerge() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "  ", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsArrayMergeMappingVoiTargetTypeKhacBodyField() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.QUERY, "q", 0, null);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    // ---- Finding #3: cycle-detection phai chan create()/update() TRUOC khi reload cache ----

    @Test
    void create_rejectsWhenDependencyAnalyzerReportsCycle() {
        when(dependencyAnalyzer.detectCycleWarningsOnly())
                .thenReturn(List.of("Endpoint A goi nguoc Endpoint B, B goi lai A"));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CYCLE");

        verify(versionService, never()).recordSnapshot(any(), any());
    }

    @Test
    void update_rejectsWhenDependencyAnalyzerReportsCycle() {
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(java.util.Optional.of(EndpointConfig.builder().id("ep-1").build()));
        when(dependencyAnalyzer.detectCycleWarningsOnly()).thenReturn(List.of("vong lap"));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.update("ep-1", dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CYCLE");

        verify(versionService, never()).recordSnapshot(any(), any());
    }

    // ---- P0-4: versioning + rollback ----

    @Test
    void create_success_recordsSnapshotAsCreated() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.create(dto);

        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.CREATED));
    }

    @Test
    void update_success_recordsSnapshotAsUpdated() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.UPDATED));
    }

    @Test
    void rollback_dungLaiUpdate_vaGhiVersionTagRolledBack() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        EndpointRequestDto snapshotDto = new EndpointRequestDto("n-cu", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);
        when(versionService.toRequestDtoForRollback("ep-1", "v-1")).thenReturn(snapshotDto);

        EndpointResponseDto result = service.rollback("ep-1", "v-1");

        assertThat(result).isNotNull();
        verify(versionService).toRequestDtoForRollback("ep-1", "v-1");
        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.ROLLED_BACK));
    }

    @Test
    void rollback_khiSnapshotViPhamValidate_khongApDungGiCa() {
        // Snapshot cu tro toi path "/api/legacy" (gia su tung hop le o thoi diem do,
        // nay bi cam) - rollback phai chay lai DUNG validate nhu sua tay, khong duoc
        // bo qua chi vi noi dung lay tu 1 phien ban da tung luu thanh cong truoc day.
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        lenient().when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        EndpointRequestDto badSnapshot = new EndpointRequestDto("n", null, "/api/legacy", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);
        when(versionService.toRequestDtoForRollback("ep-1", "v-1")).thenReturn(badSnapshot);

        assertThatThrownBy(() -> service.rollback("ep-1", "v-1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-001");

        verify(versionService, never()).recordSnapshot(any(), any());
    }

    @Test
    void delete_xoaVersionTruocKhiXoaEndpoint() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));

        service.delete("ep-1");

        verify(versionService).deleteAllForEndpoint("ep-1");
        verify(repository).delete(existing);
    }

    // ---- P1-5: validate dieu kien re nhanh luc luu ----

    @Test
    void create_rejectsNextStepOrderIfTrueKhongTonTai() {
        BackendStepDto s1 = stepWithBranch(1, null, null, 99, null); // step 99 khong ton tai
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionSourceStepOrderKhongTonTai() {
        BackendStepDto s1 = stepWithBranch(1, 99, ConditionOperator.EXISTS, 2, null); // sourceStepOrder 99 khong ton tai
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsEqualsThieuConditionExpectedValue() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.EQUALS, 2, null, null); // EQUALS nhung khong co gia tri mong doi
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    // ---- 4 toan tu so sanh SO moi them (>,>=,<,<=): conditionExpectedValue BAT BUOC la so ----

    @Test
    void create_rejectsGreaterThanThieuConditionExpectedValue() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.GREATER_THAN, 2, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsLessThanOrEqualConditionExpectedValueKhongPhaiSo() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.LESS_THAN_OR_EQUAL, 2, null, "abc");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_greaterThanConditionExpectedValueLaSoHopLe_thanhCong() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.GREATER_THAN, 2, null, "3");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- V6: validate conditionGroups (nhieu nguon REQUEST_BODY/STEP_RESPONSE, nhieu nhom AND/OR) ----

    private com.bccs.gatewaymanager.dto.ConditionRuleDto rule(FieldMappingSourceType sourceType, String sourceField,
                                                                Integer sourceStepOrder, ConditionOperator operator, String expectedValue) {
        return new com.bccs.gatewaymanager.dto.ConditionRuleDto(sourceType, sourceField, sourceStepOrder, operator, expectedValue);
    }

    private BackendStepDto stepWithConditionGroups(int order, List<List<com.bccs.gatewaymanager.dto.ConditionRuleDto>> groups,
                                                     Integer nextIfTrue, Integer nextIfFalse) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                FieldMappingSourceType.REQUEST_BODY, null, "x", ConditionOperator.EXISTS, null,
                null, groups, nextIfTrue, nextIfFalse, null, null, null, null, null, null);
    }

    @Test
    void create_rejectsConditionGroups_nhomRong() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(), List.of(rule(FieldMappingSourceType.REQUEST_BODY, "x", null, ConditionOperator.EXISTS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionGroups_thieuSourceField() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, null, null, ConditionOperator.EXISTS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionGroups_stepResponseThieuSourceStepOrder() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(rule(FieldMappingSourceType.STEP_RESPONSE, "ok", null, ConditionOperator.EXISTS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionGroups_stepResponseSourceStepOrderKhongTonTai() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(rule(FieldMappingSourceType.STEP_RESPONSE, "ok", 99, ConditionOperator.EXISTS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionGroups_equalsThieuExpectedValue() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "x", null, ConditionOperator.EQUALS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionGroups_toanTuSoVoiExpectedValueKhongPhaiSo() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "x", null, ConditionOperator.GREATER_THAN, "abc"))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_conditionGroups_hopLe_coCaRequestBodyVaStepResponse_thanhCong() {
        BackendStepDto s1 = stepWithConditionGroups(1,
                List.of(
                        List.of(rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"),
                                rule(FieldMappingSourceType.REQUEST_BODY, "fieldX", null, ConditionOperator.EQUALS, "true")),
                        List.of(rule(FieldMappingSourceType.STEP_RESPONSE, "ok", 2, ConditionOperator.EXISTS, null))),
                2, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    @Test
    void create_rejectsInKhongPhaiMangJson() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.IN, 2, null, "abc");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsRegexKhongHopLe() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.MATCHES_REGEX, 2, null, "(");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_isEmptyKhongCanExpectedValue_thanhCong() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.IS_EMPTY, 2, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Giai doan 2-4: validate cay dieu kien (long nhau, so sanh 2 field, co he thong) ----

    private EndpointRequestDto requestWithGroups(List<List<com.bccs.gatewaymanager.dto.ConditionRuleDto>> groups) {
        BackendStepDto s1 = stepWithConditionGroups(1, groups, 2, null);
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);
    }

    private void assertGw003(EndpointRequestDto dto) {
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_treeNhomConRong_biTuChoi() {
        var empty = com.bccs.gatewaymanager.dto.ConditionRuleDto.group("AND", List.of());
        assertGw003(requestWithGroups(List.of(List.of(empty))));
    }

    @Test
    void create_treeLongNhauQuaSau_biTuChoi() {
        var node = rule(FieldMappingSourceType.REQUEST_BODY, "x", null, ConditionOperator.EXISTS, null);
        for (int i = 0; i < 6; i++) {
            node = com.bccs.gatewaymanager.dto.ConditionRuleDto.group("OR", List.of(node));
        }
        assertGw003(requestWithGroups(List.of(List.of(node))));
    }

    @Test
    void create_soSanhHaiField_cungCoExpectedValue_biTuChoi() {
        var bad = new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY, "a", null,
                ConditionOperator.EQUALS, "1", null, null, FieldMappingSourceType.REQUEST_BODY, "b", null);
        assertGw003(requestWithGroups(List.of(List.of(bad))));
    }

    @Test
    void create_soSanhHaiField_hopLe_thanhCong() {
        var ok = new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY, "a", null,
                ConditionOperator.GREATER_THAN, null, null, null, FieldMappingSourceType.REQUEST_BODY, "b", null);
        assertThat(service.create(requestWithGroups(List.of(List.of(ok))))).isNotNull();
    }

    @Test
    void create_cờHeThongChuaKhaiBao_bi404() {
        // flagRepository mock mac dinh tra false = co chua duoc khai bao
        var flag = new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.SYSTEM_FLAG, "VIP", null,
                ConditionOperator.EQUALS, "true");
        assertThatThrownBy(() -> service.create(requestWithGroups(List.of(List.of(flag)))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-FLAG-404");
    }

    @Test
    void create_cayLongNhauHopLe_thanhCong() {
        var inner = com.bccs.gatewaymanager.dto.ConditionRuleDto.group("OR", List.of(
                rule(FieldMappingSourceType.QUERY_PARAM, "tier", null, ConditionOperator.EQUALS, "gold"),
                rule(FieldMappingSourceType.HEADER, "X-Vip", null, ConditionOperator.IS_NOT_EMPTY, null)));
        var outer = com.bccs.gatewaymanager.dto.ConditionRuleDto.group("AND", List.of(
                rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"),
                inner));
        assertThat(service.create(requestWithGroups(List.of(List.of(outer))))).isNotNull();
    }

    // ---- V7: lap mang (ANY/ALL/COUNT), toan tu kich thuoc, switch nhieu nhanh ----

    private BackendStepDto stepWithSwitchV7(int order, List<com.bccs.gatewaymanager.dto.SwitchBranchDto> branches, Integer nextIfFalse) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null, nextIfFalse,
                null, null, null, null, null, null, branches);
    }

    private EndpointRequestDto requestWithSteps(BackendStepDto... steps) {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(steps), List.of(), false, null, false, false, 300, List.of(), null);
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto quantLeaf() {
        return rule(FieldMappingSourceType.REQUEST_BODY, "status", null, ConditionOperator.EQUALS, "X");
    }

    @Test
    void create_lapMangRongKhongHopLe() {
        var node = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "ANY", "AND", List.of(), null, null);
        assertGw003(requestWithGroups(List.of(List.of(node))));
    }

    @Test
    void create_quantifierSaiTen_biTuChoi() {
        var node = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "SOME", "AND", List.of(quantLeaf()), null, null);
        assertGw003(requestWithGroups(List.of(List.of(node))));
    }

    @Test
    void create_countThieuNguongSo_biTuChoi() {
        var node = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "COUNT", "AND", List.of(quantLeaf()), ConditionOperator.GREATER_THAN_OR_EQUAL, "abc");
        assertGw003(requestWithGroups(List.of(List.of(node))));
    }

    @Test
    void create_lapMangHopLe_thanhCong() {
        var node = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "orders", null,
                "ANY", "AND", List.of(
                        com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                                "ALL", "AND", List.of(quantLeaf()), null, null)), null, null);
        assertThat(service.create(requestWithGroups(List.of(List.of(node))))).isNotNull();
    }

    @Test
    void create_toanTuKichThuocCanSo_biTuChoi() {
        var bad = rule(FieldMappingSourceType.REQUEST_BODY, "items", null, ConditionOperator.SIZE_GREATER_THAN, "abc");
        assertGw003(requestWithGroups(List.of(List.of(bad))));
    }

    @Test
    void create_switchNhanhKhongCoDieuKien_biTuChoi() {
        var branch = new com.bccs.gatewaymanager.dto.SwitchBranchDto(List.of(), 2);
        assertGw003(requestWithSteps(stepWithSwitchV7(1, List.of(branch), null), step(2)));
    }

    @Test
    void create_switchNhanhTroToiStepKhongTonTai_biTuChoi() {
        var branch = new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "t", null, ConditionOperator.EXISTS, null))), 99);
        assertGw003(requestWithSteps(stepWithSwitchV7(1, List.of(branch), null), step(2)));
    }

    @Test
    void create_switchHopLe_thanhCong() {
        var b1 = new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "t", null, ConditionOperator.EQUALS, "gold"))), 2);
        var b2 = new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "t", null, ConditionOperator.EQUALS, "silver"))), 3);
        assertThat(service.create(requestWithSteps(stepWithSwitchV7(1, List.of(b1, b2), 3), step(2), step(3)))).isNotNull();
    }

    @Test
    void create_switchTroVeChinh_biPhatHienChuTrinh() {
        var b = new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "t", null, ConditionOperator.EXISTS, null))), 1);
        assertThatThrownBy(() -> service.create(requestWithSteps(stepWithSwitchV7(1, List.of(b), null))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-BRANCH-CYCLE");
    }

    @Test
    void create_baoLoiNhanhSai_coStepSauKhongHopLe_biTuChoi() {
        var s = condStepWithFalseErrorDto("GW-X", "msg", 2);
        assertGw003(requestWithSteps(s, step(2)));
    }

    @Test
    void create_baoLoiKhongCoDieuKien_biTuChoi() {
        var s = new BackendStepDto(null, 1, "s", GatewayMethod.GET, "/x", "up-1", "up", false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "GW-X", "msg");
        assertGw003(requestWithSteps(s, step(2)));
    }

    @Test
    void create_baoLoiKhongCoThongDiep_biTuChoi() {
        assertGw003(requestWithSteps(condStepWithFalseErrorDto("GW-X", null, null), step(2)));
    }

    @Test
    void create_baoLoiHopLe_thanhCong() {
        assertThat(service.create(requestWithSteps(condStepWithFalseErrorDto("GW-X", "Khong hop le", null), step(2)))).isNotNull();
    }

    private BackendStepDto condStepWithFalseErrorDto(String code, String msg, Integer nextFalse) {
        return new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null, null, null,
                null, null,
                FieldMappingSourceType.REQUEST_BODY, null, "tier", ConditionOperator.EQUALS, "gold",
                null, null, 2, nextFalse, null, null, null, null, null, null, null, code, msg);
    }

    @Test
    void create_rejectsVongLapReNhanh() {
        // step1 dieu kien luon (gia lap) -> nextStepOrderIfTrue tro VE CHINH NO -> vong lap.
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.EXISTS, 1, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-BRANCH-CYCLE");
    }

    @Test
    void create_boQuaRuleSourceTruocTarget_khiEndpointDungReNhanh() {
        // FieldMapping "nguoc" (sourceStepOrder=2 >= targetStepOrder=1) - binh
        // thuong se bi chan GW-003, nhung endpoint nay CO step dung re nhanh (step3)
        // nen thu tu thuc thi khong con dam bao theo stepOrder nua -> phai CHO QUA
        // rule nay (dua vao graceful-null luc runtime).
        FieldMappingDto backwardsMapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 2, "f", null, null, null,
                1, MappingTargetType.QUERY, "q", 0, null);
        // step3 co dieu kien nhung CA 2 nhanh deu ket thuc (null, null) - khong tao
        // cycle (1->2->3, step3 khong di dau ca) - chi de kich usesBranching=true.
        BackendStepDto s3 = stepWithBranch(3, 1, ConditionOperator.EXISTS, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2), s3), List.of(backwardsMapping), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- onErrorStepOrder (nang luc moi - fallback khi step LOI, doc lap voi conditionOperator) ----

    @Test
    void create_onErrorStepOrder_hopLe_thanhCong() {
        BackendStepDto s1 = stepWithOnError(1, 2);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    @Test
    void create_rejectsOnErrorStepOrderKhongTonTai() {
        BackendStepDto s1 = stepWithOnError(1, 99); // step 99 khong ton tai
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsOnErrorStepOrderTaoVongLap() {
        // step1 loi -> fallback ve CHINH NO -> vong lap (dung DFS cycle-detection y het
        // conditionOperator, xem detectBranchCycle()).
        BackendStepDto s1 = stepWithOnError(1, 1);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-BRANCH-CYCLE");
    }

    @Test
    void create_boQuaRuleSourceTruocTarget_khiEndpointChiDungOnErrorStepOrder() {
        // Chi dung onErrorStepOrder (KHONG dung conditionOperator) - usesBranching() van
        // phai nhan dien dung de tha long rule "sourceStepOrder < targetStepOrder".
        // step1 fallback sang step3 (KHONG phai vong lap - step1->{2,3}, step2->3, step3->
        // khong di dau, xem detectBranchCycle()) chi de kich usesBranching()=true.
        FieldMappingDto backwardsMapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 3, "f", null, null, null,
                1, MappingTargetType.QUERY, "q", 0, null);
        BackendStepDto s1 = stepWithOnError(1, 3);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2), step(3)), List.of(backwardsMapping), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Muc 4: parallelExecution (song song hoa step doc lap) ----

    @Test
    void create_rejectsParallelExecutionKhiSequentialTrue() {
        // parallelExecution chi co y nghia voi step doc lap (sequential=false) - bat ca 2
        // cung luc la cau hinh mau thuan, phai chan tai luc luu (xem validateStepOrders()).
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(), false, null, true, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_parallelExecutionKhiSequentialFalse_thanhCong() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, false, "json",
                List.of(step(1), step(2)), List.of(), false, null, true, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Wave song song trong 1 chuoi sequential (parallelGroup) ----

    @Test
    void create_parallelGroupHopLe_sequentialTrue_thanhCong() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(stepWithGroup(1, 10), stepWithGroup(2, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    @Test
    void create_rejectsParallelGroupKhiSequentialFalse() {
        // parallelGroup chi co y nghia trong 1 chuoi sequential=true (nguoc lai voi
        // parallelExecution - chi dung khi sequential=false).
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, false, "json",
                List.of(stepWithGroup(1, 10), stepWithGroup(2, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsParallelGroupStepCoConditionOperatorRieng() {
        // V1 chua ho tro re nhanh TU 1 step trong wave.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(stepWithGroupAndCondition(1, 10), stepWithGroup(2, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsParallelGroupStepCoOnErrorStepOrderRieng() {
        // V1 chua ho tro fallback loi TU 1 step trong wave.
        BackendStepDto s1 = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, 2, 10, null, null, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, stepWithGroup(2, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsNhayVaoParallelGroup() {
        // step1 (onErrorStepOrder=2) fallback TOI step2 - nhung step2 dang trong 1 nhom
        // song song (parallelGroup=10) - wave chi duoc vao qua tien trinh tuan tu tu
        // nhien, khong duoc qua nhay/fallback.
        BackendStepDto s1 = stepWithOnError(1, 2);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, stepWithGroup(2, 10), stepWithGroup(3, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsParallelGroupStepOrderKhongLienTiep() {
        // Nhom 10 = {step1, step3} - step2 (KHONG cung nhom) nam GIUA -> logic runtime
        // "tu dau wave nhay thang toi sau cuoi wave" se BO QUA step2 - phai chan luc luu.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(stepWithGroup(1, 10), step(2), stepWithGroup(3, 10)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    // ---- Muc 6: bu tru/rollback nghiep vu (saga best-effort) ----

    @Test
    void create_rejectsCompensationKhiSequentialFalse() {
        // Bu tru chi ap dung duoc khi sequential=true.
        BackendStepDto s1 = stepWithCompensation(1, "up-comp", GatewayMethod.DELETE, "/orders/cancel");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, false, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsCompensationThieuMotPhan() {
        // Chi khai bao compensationMethod, thieu compensationUpstreamServiceId/UrlPattern -
        // all-or-nothing, phai chan.
        BackendStepDto s1 = stepWithCompensation(1, null, GatewayMethod.DELETE, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsFieldMappingCompensationTroToiStepChuaCauHinhBuTru() {
        // step1 KHONG cau hinh bu tru, nhung co mapping targetContext=COMPENSATION tro toi
        // no - "mo coi", vo nghia, phai chan.
        FieldMappingDto orphanMapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "id",
                null, null, null, 1, MappingTargetType.PATH, "id", 0, MappingTargetContext.COMPENSATION);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(orphanMapping), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_compensationHopLe_voiMappingSourceStepOrderBangTargetStepOrder_thanhCong() {
        // Cau hinh bu tru day du + 1 mapping COMPENSATION voi sourceStepOrder==targetStepOrder
        // (bu tru step1 dung CHINH response cua step1) - rule ">=` cua mapping MAIN KHONG
        // duoc ap dung cho COMPENSATION, phai thanh cong.
        BackendStepDto s1 = stepWithCompensation(1, "up-comp", GatewayMethod.DELETE, "/orders/{orderId}");
        FieldMappingDto compMapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "orderId",
                null, null, null, 1, MappingTargetType.PATH, "orderId", 0, MappingTargetContext.COMPENSATION);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of(compMapping), false, null, false, false, 300, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Cache toan bo response cho MOI client (khac Idempotency-Key) - CHAN CUNG chi-GET ----

    private BackendStepDto stepWithMethod(int order, GatewayMethod method) {
        return new BackendStepDto(null, order, "step" + order, method, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void create_rejectsResponseCacheKhiEndpointKhongPhaiGetPost() {
        // PUT (chu khong phai POST) - tu 2026-09 POST duoc cho phep (mirror cache per-step),
        // chi con PUT/PATCH/DELETE bi chan cung vi gan nhu chac chan la mutating.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.PUT, true, "json",
                List.of(step(1)), List.of(), false, null, false, true, 60, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsResponseCacheKhiCoStepKhongPhaiGetPost() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), stepWithMethod(2, GatewayMethod.PUT)), List.of(), false, null, false, true, 60, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_responseCacheHopLe_toanBoGet_thanhCong() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(), false, null, false, true, 60, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    @Test
    void create_responseCacheHopLe_toanBoPost_thanhCong() {
        // POST duoc cho phep tu 2026-09 - dung cho API tim kiem/tra cuu truyen filter qua body.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.POST, true, "json",
                List.of(stepWithMethod(1, GatewayMethod.POST), step(2)), List.of(), false, null, false, true, 60, List.of(), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Bug that: bat cache (response loi bi cache) -> tat -> BAT LAI -> entry Redis cu
    // van con vi key cache khong phu thuoc cacheEnabled. purgeStaleCacheIfChanged() phai
    // xoa cache Redis (Control Plane) MOI LAN luu co doi field anh huong cache. ----

    private BackendStepDto stepWithCache(int order, boolean cacheEnabled) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, cacheEnabled, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private EndpointResponseDto endpointDtoWithCache(boolean responseCacheEnabled, BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(steps), List.of(), null, null, false, 86400, false, responseCacheEnabled, 300, List.of(), null);
    }

    @Test
    void update_stepCacheEnabledTatDi_xoaCachePatternCuaUpstreamDo() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        // Lan goi 1 (before, TRUOC khi mutate) tra ve cacheEnabled=true; lan goi 2 (result,
        // SAU khi save) tra ve cacheEnabled=false - mo phong dung 1 lan tat cache.
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithCache(1, true)),
                endpointDtoWithCache(false, stepWithCache(1, false)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(cacheService).deleteByPattern("gwm:cache:up:GET:*");
    }

    @Test
    void update_stepCacheEnabledBatLai_xoaCachePatternCuaUpstreamDo() {
        // Nhanh "bat lai" - CHINH nhanh nguoi dung bao loi that: tat (da xoa cache o lan
        // truoc) -> bat lai -> phai xoa THEM 1 lan nua (idempotent, khong sao neu khong
        // con gi de xoa - dam bao entry nao lot luoi o lan tat cung bi don o lan bat lai).
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithCache(1, false)),
                endpointDtoWithCache(false, stepWithCache(1, true)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(cacheService).deleteByPattern("gwm:cache:up:GET:*");
    }

    @Test
    void update_khongDoiGiLienQuanCache_khongGoiXoaCache() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        EndpointResponseDto unchanged = endpointDtoWithCache(false, stepWithCache(1, false));
        when(mapper.toResponseDto(any())).thenReturn(unchanged, unchanged);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(cacheService, never()).deleteByPattern(any());
    }

    @Test
    void update_upstreamCuaStepDoiTen_xoaCaPatternCuLanMoi() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        BackendStepDto oldStep = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up-cu",
                false, true, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        BackendStepDto newStep = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up-moi",
                false, true, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, oldStep), endpointDtoWithCache(false, newStep));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(cacheService).deleteByPattern("gwm:cache:up-cu:GET:*");
        verify(cacheService).deleteByPattern("gwm:cache:up-moi:GET:*");
    }

    @Test
    void update_responseCacheEnabledDoi_xoaResponseCachePattern() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(true, stepWithCache(1, false)),
                endpointDtoWithCache(false, stepWithCache(1, false)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(cacheService).deleteByPattern("gwm:response-cache:ep-1:*");
    }

    // ---- Nut "Xoa cache" thu cong (khong doi cau hinh gi) - dung khi cache bi nhiem qua
    // "Thu nhanh" tren draft chua luu, khong co su kien save nao de tu kich purge o tren. ----

    @Test
    void clearCache_xoaCaResponseCacheLanStepCacheDangBat() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(true, stepWithCache(1, true), stepWithCache(2, false)));

        service.clearCache("ep-1");

        verify(cacheService).deleteByPattern("gwm:response-cache:ep-1:*");
        verify(cacheService).deleteByPattern("gwm:cache:up:GET:*");
        verify(cacheService, org.mockito.Mockito.times(2)).deleteByPattern(any());
    }

    @Test
    void clearCache_khongBatCacheGi_khongGoiXoaGi() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithCache(1, false)));

        service.clearCache("ep-1");

        verify(cacheService, never()).deleteByPattern(any());
    }

    // ---- Bulkhead RIENG theo step (maxConcurrentCallsOverride) - Resilience4j registry chi tao
    // instance 1 LAN DAU, doi override xong khong don thi KHONG co hieu luc toi khi restart app
    // (dung y het ly do purgeStaleCacheIfChanged() o tren ton tai cho cache) ----

    private BackendStepDto stepWithBulkheadOverride(String stepId, String upstreamServiceName, Integer maxConcurrentCallsOverride) {
        return new BackendStepDto(stepId, 1, "step1", GatewayMethod.GET, "/x", "up-1", upstreamServiceName,
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                maxConcurrentCallsOverride, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void update_maxConcurrentCallsOverrideDatMoi_goiInvalidateStepBulkhead() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up", null)),
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up", 50)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(upstreamHttpExecutor).invalidateStepBulkhead("up", "s1");
    }

    @Test
    void update_maxConcurrentCallsOverrideDoiGiaTri_goiInvalidateStepBulkhead() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up", 50)),
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up", 100)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(upstreamHttpExecutor).invalidateStepBulkhead("up", "s1");
    }

    @Test
    void update_khongDoiMaxConcurrentCallsOverride_khongGoiInvalidateStepBulkhead() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        EndpointResponseDto unchanged = endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up", 50));
        when(mapper.toResponseDto(any())).thenReturn(unchanged, unchanged);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        verify(upstreamHttpExecutor, never()).invalidateStepBulkhead(any(), any());
    }

    @Test
    void update_upstreamCuaStepDoiTenTrongKhiConOverride_goiInvalidateStepBulkheadTheoTenUpstreamCu() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findByIdAndTeamCode("ep-1", TEAM)).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any())).thenReturn(
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up-cu", 50)),
                endpointDtoWithCache(false, stepWithBulkheadOverride("s1", "up-moi", 50)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), null);

        service.update("ep-1", dto);

        // Bulkhead rieng cua step khoa CA ten Upstream ("upstreamName#step:stepId") - doi ten
        // Upstream ma khong don se de lai 1 bulkhead "mo coi" khoa theo ten CU.
        verify(upstreamHttpExecutor).invalidateStepBulkhead("up-cu", "s1");
    }

    // ---- V5: validateFlagRules - Rule cờ gan vao endpoint ----

    private com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto flagCond(String code, boolean expected) {
        return new com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto(code, expected);
    }

    private com.bccs.gatewaymanager.dto.FlagRuleDto flagRule(String name, int priority,
            java.util.List<java.util.List<com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto>> groups,
            String actionType, Integer startStepOrder, String blockErrorCode) {
        return new com.bccs.gatewaymanager.dto.FlagRuleDto(null, name, priority, groups,
                actionType, startStepOrder, blockErrorCode, null);
    }

    @Test
    void create_flagRuleThieuFlagKhaiBao_baoGW_FLAG_404() {
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP")).thenReturn(false);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(List.of(flagCond("VIP", true))),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 2, null, null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-FLAG-404");
    }

    @Test
    void create_flagRuleStartStepOrderKhongTonTai_baoGW_003() {
        // Khong stub flagRepository - startStepOrder sai bi cham TRUOC khi tra cờ.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(List.of(flagCond("VIP", true))),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 99, null, null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRuleSequentialFalse_baoGW_003() {
        // Khong stub flagRepository - sequential=false bi cham TRUOC khi tra cờ.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, false, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(List.of(flagCond("VIP", true))),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-X", null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRulePriorityTrung_baoGW_003() {
        // Dieu kien cua rule 1 duoc tra TRUOC khi priority cua rule 2 bi cham ->
        // phai stub flag ton tai de flow cham dung loi priority trung o rule 2.
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP")).thenReturn(true);
        var r1 = new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R1", 1,
                List.of(List.of(flagCond("VIP", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-A", null);
        var r2 = new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R2", 1,
                List.of(List.of(flagCond("VIP", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-B", null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(r1, r2), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRuleBlockThieuBlockErrorCode_baoGW_003() {
        // Khong stub flagRepository - BLOCK thieu blockErrorCode bi cham TRUOC khi tra cờ.
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(List.of(flagCond("VIP", true))),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, null, null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRuleNhomRong_baoGW_003() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-A", null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRuleTrungFlagCodeTrong1Nhom_baoGW_003() {
        // Dieu kien thu 2 (VIP lan 2) bi cham SAU khi Dieu kien thu 1 da tra cờ ->
        // phai stub flag ton tai de flow cham dung loi trung flagCode trong nhom.
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP")).thenReturn(true);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300,
                List.of(new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "R", 1,
                        List.of(List.of(flagCond("VIP", true), flagCond("VIP", false))),
                        com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-A", null)), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_flagRuleHopLe_thanhCong() {
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP")).thenReturn(true);
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "TRIAL")).thenReturn(true);
        var rule = new com.bccs.gatewaymanager.dto.FlagRuleDto(null, "Luong VIP", 1,
                List.of(List.of(flagCond("VIP", true)), List.of(flagCond("TRIAL", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 1, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(), false, null, false, false, 300, List.of(rule), null);

        assertThat(service.create(dto)).isNotNull();
    }

    // ---- V5: dieu kien re nhanh tai step voi nguon SYSTEM_FLAG ----

    @Test
    void create_systemFlagCondition_coKhacThieu_conditionSourceField_baoGW_003() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                        null, null, null, null,
                        FieldMappingSourceType.SYSTEM_FLAG, null, "  ", ConditionOperator.EQUALS, "true", null, null,
                        2, 2, null, null, null, null, null, null),
                        step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_systemFlagCondition_expectedValueKhongPhaiTrueFalse_baoGW_003() {
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP")).thenReturn(true);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                        null, null, null, null,
                        FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", ConditionOperator.EQUALS, "yes", null, null,
                        2, 2, null, null, null, null, null, null),
                        step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_systemFlagCondition_coKhongTonTai_baoGW_FLAG_404() {
        when(flagRepository.existsByTeamCodeAndFlagCodeIgnoreCase(TEAM, "NOPE")).thenReturn(false);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                        null, null, null, null,
                        FieldMappingSourceType.SYSTEM_FLAG, null, "NOPE", ConditionOperator.EQUALS, "true", null, null,
                        2, 2, null, null, null, null, null, null),
                        step(2)), List.of(), false, null, false, false, 300, List.of(), null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-FLAG-404");
    }
    // ==================== Tinh nang ResponseMapping - validation ====================

    @Test
    void create_responseMappingModeSai_baoGW_003() {
        var spec = new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("APPEND",
                List.of(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "code", "code", 0)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), spec);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_responseMappingSourceStepOrderKhongTonTai_baoGW_003() {
        var spec = new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("MERGE",
                List.of(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(99, "code", "code", 0)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), spec);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_responseMappingFieldTrong_baoGW_003() {
        var spec = new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("MERGE",
                List.of(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "  ", "code", 0)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), spec);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_responseMappingHopLe_luuKhongLoi() {
        // mapper o test nay la MOCK (tra DTO default) - chi xac nhan validate khong chan
        // cau hinh dung; round-trip DTO<->CLOB do EndpointMapperTest dam nhiem.
        var spec = new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("REPLACE",
                List.of(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "code", "code", 0)));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, 300, List.of(), spec);

        var result = service.create(dto);

        assertThat(result).isNotNull();
    }
}
