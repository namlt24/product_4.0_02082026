package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private EndpointRegistryCache registryCache;
    @Mock
    private DependencyAnalyzer dependencyAnalyzer;
    @Mock
    private EndpointVersionService versionService;

    private EndpointService service;

    @BeforeEach
    void setUp() {
        service = new EndpointService(repository, mapper, registryCache, dependencyAnalyzer, versionService);
        lenient().when(repository.existsByPath(any())).thenReturn(false);
        lenient().when(mapper.toEntity(any())).thenReturn(EndpointConfig.builder().id("ep-1").build());
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(mapper.toResponseDto(any())).thenReturn(
                new EndpointResponseDto("ep-1", "n", null, "/x", GatewayMethod.GET, true, "json",
                        List.of(), List.of(), null, null));
        lenient().when(dependencyAnalyzer.detectCycleWarningsOnly()).thenReturn(List.of());
    }

    private BackendStepDto step(int order) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of(), null, null,
                null, null,
                null, null, null, null, null, null, null);
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
                FieldMappingSourceType.STEP_RESPONSE, conditionSourceStepOrder, "field", operator, conditionExpectedValue,
                nextIfTrue, nextIfFalse);
    }

    private EndpointRequestDto requestWithMapping(FieldMappingDto mapping) {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2)), List.of(mapping));
    }

    // ---- Ha thap rui ro phat sinh tu fix auth: path Data Plane khong duoc trung tien to /api hoac /actuator ----
    // (ApiKeyAuthFilter dang ky theo Servlet urlPattern "/api/*" - khop MOI request bat dau
    // bang /api bat ke Spring MVC se route no toi controller nao, nen 1 endpoint composite
    // dat path "/api/orders" se vo tinh bi doi API key du muc dich la Data Plane khong auth).

    @Test
    void create_rejectsPathStartingWithApi() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/api/orders", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-001");
    }

    @Test
    void create_rejectsPathStartingWithActuator() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/actuator/custom", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-001");
    }

    @Test
    void create_allowsPathThatOnlyContainsApiAsSegmentNotPrefix() {
        // "/apinormal" khong phai "/api" hay "/api/..." - khong bi chan (chi chan dung tien to).
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/apinormal", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        assertThat(service.create(dto)).isNotNull();
    }

    // ---- Finding #6: validate sourceStepOrder < targetStepOrder + required source fields ----

    @Test
    void create_rejectsMappingWhenSourceStepOrderNotBeforeTargetStepOrder() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 2, "a", null, null, null,
                1, MappingTargetType.QUERY, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankSourceFieldForStepResponse() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "  ", null, null, null,
                2, MappingTargetType.QUERY, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankArrayAggregateFields() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE, 1,
                null, "", "code", null, 2, MappingTargetType.BODY_FIELD, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_validMapping_succeeds() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "code", null, null, null,
                2, MappingTargetType.QUERY, "a", 0);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
        verify(registryCache).reload();
    }

    // ---- QUERY_PARAM (nguon FieldMapping moi - doc query param cua chinh client): giong REQUEST_BODY,
    // KHONG can sourceStepOrder, nhung VAN can sourceField (ten query param can doc). ----

    @Test
    void create_queryParamMapping_khongCanSourceStepOrder_thanhCong() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                2, MappingTargetType.QUERY, "staffCode", 0);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
        verify(registryCache).reload();
    }

    @Test
    void create_rejectsBlankSourceFieldForQueryParam() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "  ", null, null, null,
                2, MappingTargetType.QUERY, "staffCode", 0);
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
                2, MappingTargetType.QUERY, "priority", 0);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
        verify(registryCache).reload();
    }

    @Test
    void create_rejectsBlankConstantValueForConstant() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "  ",
                2, MappingTargetType.QUERY, "priority", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsNullConstantValueForConstant() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, null,
                2, MappingTargetType.QUERY, "priority", 0);
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
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
        verify(registryCache).reload();
    }

    @Test
    void create_rejectsBlankSourceArrayFieldForArrayMerge() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "  ", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsArrayMergeMappingVoiTargetTypeKhacBodyField() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.QUERY, "q", 0);
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
                List.of(step(1)), List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CYCLE");

        verify(registryCache, never()).reload();
        verify(versionService, never()).recordSnapshot(any(), any());
    }

    @Test
    void update_rejectsWhenDependencyAnalyzerReportsCycle() {
        when(repository.findById("ep-1")).thenReturn(java.util.Optional.of(EndpointConfig.builder().id("ep-1").build()));
        when(dependencyAnalyzer.detectCycleWarningsOnly()).thenReturn(List.of("vong lap"));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());

        assertThatThrownBy(() -> service.update("ep-1", dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CYCLE");

        verify(registryCache, never()).reload();
        verify(versionService, never()).recordSnapshot(any(), any());
    }

    @Test
    void create_noCycle_reloadsRegistryCache() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        service.create(dto);
        verify(registryCache).reload();
    }

    // ---- P0-4: versioning + rollback ----

    @Test
    void create_success_recordsSnapshotAsCreated() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());

        service.create(dto);

        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.CREATED));
    }

    @Test
    void update_success_recordsSnapshotAsUpdated() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findById("ep-1")).thenReturn(Optional.of(existing));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());

        service.update("ep-1", dto);

        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.UPDATED));
    }

    @Test
    void rollback_dungLaiUpdate_vaGhiVersionTagRolledBack() {
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        when(repository.findById("ep-1")).thenReturn(Optional.of(existing));
        EndpointRequestDto snapshotDto = new EndpointRequestDto("n-cu", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        when(versionService.toRequestDtoForRollback("ep-1", "v-1")).thenReturn(snapshotDto);

        EndpointResponseDto result = service.rollback("ep-1", "v-1");

        assertThat(result).isNotNull();
        verify(versionService).toRequestDtoForRollback("ep-1", "v-1");
        verify(versionService).recordSnapshot(any(EndpointConfig.class), eq(EndpointChangeType.ROLLED_BACK));
        verify(registryCache).reload();
    }

    @Test
    void rollback_khiSnapshotViPhamValidate_khongApDungGiCa() {
        // Snapshot cu tro toi path "/api/legacy" (gia su tung hop le o thoi diem do,
        // nay bi cam) - rollback phai chay lai DUNG validate nhu sua tay, khong duoc
        // bo qua chi vi noi dung lay tu 1 phien ban da tung luu thanh cong truoc day.
        EndpointConfig existing = EndpointConfig.builder().id("ep-1").build();
        lenient().when(repository.findById("ep-1")).thenReturn(Optional.of(existing));
        EndpointRequestDto badSnapshot = new EndpointRequestDto("n", null, "/api/legacy", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
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
        when(repository.findById("ep-1")).thenReturn(Optional.of(existing));

        service.delete("ep-1");

        verify(versionService).deleteAllForEndpoint("ep-1");
        verify(repository).delete(existing);
        verify(registryCache).reload();
    }

    // ---- P1-5: validate dieu kien re nhanh luc luu ----

    @Test
    void create_rejectsNextStepOrderIfTrueKhongTonTai() {
        BackendStepDto s1 = stepWithBranch(1, null, null, 99, null); // step 99 khong ton tai
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsConditionSourceStepOrderKhongTonTai() {
        BackendStepDto s1 = stepWithBranch(1, 99, ConditionOperator.EXISTS, 2, null); // sourceStepOrder 99 khong ton tai
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsEqualsThieuConditionExpectedValue() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.EQUALS, 2, null, null); // EQUALS nhung khong co gia tri mong doi
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of());

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
                List.of(s1, step(2)), List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsLessThanOrEqualConditionExpectedValueKhongPhaiSo() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.LESS_THAN_OR_EQUAL, 2, null, "abc");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_greaterThanConditionExpectedValueLaSoHopLe_thanhCong() {
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.GREATER_THAN, 2, null, "3");
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, step(2)), List.of());

        assertThat(service.create(dto)).isNotNull();
    }

    @Test
    void create_rejectsVongLapReNhanh() {
        // step1 dieu kien luon (gia lap) -> nextStepOrderIfTrue tro VE CHINH NO -> vong lap.
        BackendStepDto s1 = stepWithBranch(1, 1, ConditionOperator.EXISTS, 1, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1), List.of());

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
                1, MappingTargetType.QUERY, "q", 0);
        // step3 co dieu kien nhung CA 2 nhanh deu ket thuc (null, null) - khong tao
        // cycle (1->2->3, step3 khong di dau ca) - chi de kich usesBranching=true.
        BackendStepDto s3 = stepWithBranch(3, 1, ConditionOperator.EXISTS, null, null);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1), step(2), s3), List.of(backwardsMapping));

        assertThat(service.create(dto)).isNotNull();
    }
}
