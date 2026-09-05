package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.TryResultDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointTryServiceTest {

    @Mock
    private EndpointService endpointService;
    @Mock
    private CompositeOrchestratorEngine engine;

    private BackendStepDto step(int order) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private EndpointRequestDto draft() {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(1)), List.of(), false, null, false, false, null);
    }

    @Test
    void tryCall_chuyenDungQueryParamsSangMangVaGoiEngineVoiDungConfig() {
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "n", null, "/x/{id}", GatewayMethod.GET, true, "json",
                List.of(), List.of(), null, null, false, 86400, false, false, 300);
        when(endpointService.get("ep-1")).thenReturn(config);
        var expected = JsonMapper.builder().build().createObjectNode().put("v", 1);
        when(engine.handle(eq(config), any(), any(), any())).thenReturn(expected);

        EndpointTryService svc = new EndpointTryService(endpointService, engine);
        TryResultDto result = svc.tryCall("ep-1", Map.of("id", "123"), Map.of("q", "v"), "{\"a\":1}");

        assertThat(result.success()).isTrue();
        assertThat(result.result()).isEqualTo(expected);
        assertThat(result.hops()).isEmpty(); // engine.handle mocked - khong thuc su goi UpstreamHttpExecutor nen khong co hop nao

        ArgumentCaptor<Map<String, String[]>> queryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(engine).handle(eq(config), eq(Map.of("id", "123")), queryCaptor.capture(), eq("{\"a\":1}"));
        assertThat(queryCaptor.getValue()).containsKey("q");
        assertThat(queryCaptor.getValue().get("q")).containsExactly("v");
    }

    @Test
    void tryCall_engineThatBai_traVeEnvelopeThatBaiKhongThrow() {
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(), List.of(), null, null, false, 86400, false, false, 300);
        when(endpointService.get("ep-1")).thenReturn(config);
        when(engine.handle(eq(config), any(), any(), any()))
                .thenThrow(new BusinessException("GW-UPSTREAM-404", "upstream loi"));

        EndpointTryService svc = new EndpointTryService(endpointService, engine);
        TryResultDto result = svc.tryCall("ep-1", Map.of(), Map.of(), null);

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("GW-UPSTREAM-404");
        assertThat(result.errorMessage()).isEqualTo("upstream loi");
        assertThat(result.result()).isNull();
    }

    @Test
    void tryAdhoc_hopLe_goiValidateRoiEngineVoiSentinelId() {
        EndpointRequestDto draft = draft();
        var expected = JsonMapper.builder().build().createObjectNode().put("v", 1);
        when(engine.handle(argThat(cfg -> cfg != null && cfg.id().equals("adhoc-try")), any(), any(), any()))
                .thenReturn(expected);

        EndpointTryService svc = new EndpointTryService(endpointService, engine);
        TryResultDto result = svc.tryAdhoc(draft, Map.of(), Map.of(), null);

        assertThat(result.success()).isTrue();
        assertThat(result.result()).isEqualTo(expected);
        verify(endpointService).validate(draft);
        verify(engine).handle(argThat(cfg -> cfg.id().equals("adhoc-try")
                && cfg.steps().equals(draft.steps())
                && cfg.path().equals(draft.path())), any(), any(), any());
    }

    @Test
    void tryAdhoc_draftKhongHopLe_traVeEnvelopeThatBaiKhongGoiEngine() {
        EndpointRequestDto draft = draft();
        doThrow(new BusinessException("GW-003", "stepOrder trung lap"))
                .when(endpointService).validate(draft);

        EndpointTryService svc = new EndpointTryService(endpointService, engine);
        TryResultDto result = svc.tryAdhoc(draft, Map.of(), Map.of(), null);

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("GW-003");
        assertThat(result.errorMessage()).isEqualTo("stepOrder trung lap");
        assertThat(result.hops()).isEmpty();
        verify(engine, never()).handle(any(), any(), any(), any());
    }
}
