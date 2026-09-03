package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.entity.GatewayMethod;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointTryServiceTest {

    @Mock
    private EndpointService endpointService;
    @Mock
    private CompositeOrchestratorEngine engine;

    @Test
    void chuyenDungQueryParamsSangMangVaGoiEngineVoiDungConfig() {
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "n", null, "/x/{id}", GatewayMethod.GET, true, "json",
                List.of(), List.of(), null, null, false, 86400, false, false, 300);
        when(endpointService.get("ep-1")).thenReturn(config);
        var expected = JsonMapper.builder().build().createObjectNode();
        when(engine.handle(eq(config), any(), any(), any())).thenReturn(expected);

        EndpointTryService svc = new EndpointTryService(endpointService, engine);
        var result = svc.tryCall("ep-1", Map.of("id", "123"), Map.of("q", "v"), "{\"a\":1}");

        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<Map<String, String[]>> queryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(engine).handle(eq(config), eq(Map.of("id", "123")), queryCaptor.capture(), eq("{\"a\":1}"));
        assertThat(queryCaptor.getValue()).containsKey("q");
        assertThat(queryCaptor.getValue().get("q")).containsExactly("v");
    }
}
