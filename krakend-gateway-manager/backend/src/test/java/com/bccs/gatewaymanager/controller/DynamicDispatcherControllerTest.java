package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.audit.AuditLogService;
import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.service.EndpointRegistryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cho DynamicDispatcherController - dac biet phan idempotency-key (muc 2/3 trong ke
 * hoach nang cap da duoc duyet). Day la controller dau tien co unit test rieng - truoc gio
 * chi test tay qua curl. Uu tien cao nhat: endpoint KHONG bat idempotencyEnabled phai chay
 * dung y het hanh vi cu (khong bao gio dung cache, luon goi engine).
 */
@ExtendWith(MockitoExtension.class)
class DynamicDispatcherControllerTest {

    @Mock
    private EndpointRegistryCache registryCache;
    @Mock
    private CompositeOrchestratorEngine engine;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private GatewayCacheService cacheService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private DynamicDispatcherController controller;

    @BeforeEach
    void setUp() {
        controller = new DynamicDispatcherController(registryCache, engine, auditLogService, cacheService, objectMapper);
    }

    private EndpointResponseDto endpoint(boolean idempotencyEnabled) {
        BackendStepDto step = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null, null, null, null, null, null, null, null, null);
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step), List.of(), null, null, idempotencyEnabled, 86400);
    }

    private MockHttpServletRequest request(String idempotencyKey) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        req.setContent(new byte[0]);
        if (idempotencyKey != null) {
            req.addHeader("Idempotency-Key", idempotencyKey);
        }
        return req;
    }

    @Test
    void idempotencyTat_luonGoiEngine_khongDungCacheDuKhachGuiHeader() throws Exception {
        EndpointResponseDto config = endpoint(false);
        lenient().when(registryCache.findExact("GET", "/x")).thenReturn(config);
        when(engine.handle(eq(config), anyMap(), any(), any())).thenReturn(objectMapper.readTree("{\"v\":1}"));

        ResponseEntity<?> response = controller.dispatch(request("abc-123"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(engine, times(1)).handle(eq(config), anyMap(), any(), any());
        verify(cacheService, never()).get(any());
        verify(cacheService, never()).put(any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void idempotencyBat_khongCoHeader_khongDungCache_vanGoiEngineBinhThuong() throws Exception {
        EndpointResponseDto config = endpoint(true);
        lenient().when(registryCache.findExact("GET", "/x")).thenReturn(config);
        when(engine.handle(eq(config), anyMap(), any(), any())).thenReturn(objectMapper.readTree("{\"v\":1}"));

        ResponseEntity<?> response = controller.dispatch(request(null));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(engine, times(1)).handle(eq(config), anyMap(), any(), any());
        verify(cacheService, never()).get(any());
    }

    @Test
    void idempotencyBat_coHeader_cacheMiss_goiEngine_chiCacheKhiThanhCong() throws Exception {
        EndpointResponseDto config = endpoint(true);
        lenient().when(registryCache.findExact("GET", "/x")).thenReturn(config);
        when(cacheService.get("gwm:idempotency:ep-1:abc-123")).thenReturn(Optional.empty());
        JsonNode result = objectMapper.readTree("{\"v\":1}");
        when(engine.handle(eq(config), anyMap(), any(), any())).thenReturn(result);

        ResponseEntity<?> response = controller.dispatch(request("abc-123"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(engine, times(1)).handle(eq(config), anyMap(), any(), any());
        verify(cacheService).put(eq("gwm:idempotency:ep-1:abc-123"), eq(result.toString()), eq(86400));
    }

    @Test
    void idempotencyBat_cacheHit_traThangResponseCu_khongGoiLaiEngine() throws Exception {
        EndpointResponseDto config = endpoint(true);
        lenient().when(registryCache.findExact("GET", "/x")).thenReturn(config);
        when(cacheService.get("gwm:idempotency:ep-1:abc-123")).thenReturn(Optional.of("{\"v\":1}"));

        ResponseEntity<?> response = controller.dispatch(request("abc-123"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(((JsonNode) response.getBody()).get("v").asInt()).isEqualTo(1);
        // Cache hit -> KHONG duoc goi lai engine.handle() (tranh side-effect lap).
        verify(engine, never()).handle(any(), anyMap(), any(), any());
    }

    @Test
    void idempotencyBat_coHeader_engineThrow_khongCacheLoi() {
        EndpointResponseDto config = endpoint(true);
        lenient().when(registryCache.findExact("GET", "/x")).thenReturn(config);
        when(cacheService.get("gwm:idempotency:ep-1:abc-123")).thenReturn(Optional.empty());
        when(engine.handle(eq(config), anyMap(), any(), any()))
                .thenThrow(new RuntimeException("upstream loi"));

        try {
            controller.dispatch(request("abc-123"));
        } catch (Exception ignored) {
            // Mong doi throw - kiem tra hanh vi ben duoi.
        }

        // Loi KHONG duoc cache - client phai retry lai duoc sau khi loi that da het.
        verify(cacheService, never()).put(any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }
}
