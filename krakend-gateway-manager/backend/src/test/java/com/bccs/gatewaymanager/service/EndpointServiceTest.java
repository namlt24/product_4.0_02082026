package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private EndpointService service;

    @BeforeEach
    void setUp() {
        service = new EndpointService(repository, mapper, registryCache, dependencyAnalyzer);
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
                false, false, 300, null, null, List.of(), List.of(), java.util.Map.of());
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
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 2, "a", null, null,
                1, MappingTargetType.QUERY, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankSourceFieldForStepResponse() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "  ", null, null,
                2, MappingTargetType.QUERY, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_rejectsBlankArrayAggregateFields() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE, 1,
                null, "", "code", 2, MappingTargetType.BODY_FIELD, "a", 0);
        assertThatThrownBy(() -> service.create(requestWithMapping(mapping)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-003");
    }

    @Test
    void create_validMapping_succeeds() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "code", null, null,
                2, MappingTargetType.QUERY, "a", 0);
        EndpointResponseDto result = service.create(requestWithMapping(mapping));
        assertThat(result).isNotNull();
        verify(registryCache).reload();
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
    }

    @Test
    void create_noCycle_reloadsRegistryCache() {
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of());
        service.create(dto);
        verify(registryCache).reload();
    }
}
