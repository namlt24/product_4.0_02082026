package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.restclient.RestTemplateBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * RemoteConfigSyncService tu tao RestTemplate noi bo (khong inject duoc, giong
 * UpstreamHttpExecutor) - dung com.sun.net.httpserver.HttpServer (co san
 * trong JDK) de dung REAL HTTP server cuc bo thay vi mock RestTemplate, cung
 * phong cach voi UpstreamHttpExecutorTest.
 */
class RemoteConfigSyncServiceTest {

    private final EndpointRegistryCache endpointRegistryCache = new EndpointRegistryCache();
    private final UpstreamRegistryCache upstreamRegistryCache = new UpstreamRegistryCache();
    private final UpstreamHttpExecutor upstreamHttpExecutor = Mockito.mock(UpstreamHttpExecutor.class);

    private HttpServer server;
    private final AtomicReference<String> nextResponseBody = new AtomicReference<>();
    private final AtomicReference<String> lastSeenApiKeyHeader = new AtomicReference<>();

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private RemoteConfigSyncService startServerAndBuildService(String apiKey) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/config/export", exchange -> {
            lastSeenApiKeyHeader.set(exchange.getRequestHeaders().getFirst("X-Gateway-Admin-Key"));
            byte[] body = nextResponseBody.get().getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();
        return new RemoteConfigSyncService(new RestTemplateBuilder(), "http://localhost:" + port,
                apiKey, 3600, endpointRegistryCache, upstreamRegistryCache, upstreamHttpExecutor);
    }

    private String bundleJson(String upstreamId, String name, int connectTimeoutMs) {
        return "{\"schemaVersion\":\"1.0\",\"exportedAt\":\"2026-09-09T12:00:00Z\",\"upstreams\":[" +
                "{\"id\":\"" + upstreamId + "\",\"name\":\"" + name + "\",\"description\":null," +
                "\"baseHost\":\"http://backend:8080\",\"connectTimeoutMs\":" + connectTimeoutMs + "," +
                "\"readTimeoutMs\":3000,\"circuitBreakerEnabled\":true,\"failureRateThreshold\":50," +
                "\"retryEnabled\":true,\"maxConcurrentCalls\":20,\"maxWaitDurationMs\":500," +
                "\"createdAt\":null,\"updatedAt\":null}],\"endpoints\":[]}";
    }

    @Test
    void sync_thanhCong_naplaiCaHaiCacheVaDanhDauReady() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("team-a-key");

        assertThat(service.isReady()).isFalse();
        service.sync();

        assertThat(service.isReady()).isTrue();
        assertThat(upstreamRegistryCache.getById("up-1")).isNotNull();
        assertThat(upstreamRegistryCache.getById("up-1").getName()).isEqualTo("svc");
        assertThat(endpointRegistryCache.all()).isEmpty();
    }

    @Test
    void sync_guiDungHeaderApiKeyCuaChinhDoiMinh() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("chinh-xac-key-cua-doi-nay");

        service.sync();

        assertThat(lastSeenApiKeyHeader.get()).isEqualTo("chinh-xac-key-cua-doi-nay");
    }

    @Test
    void sync_ControlPlaneKhongKetNoiDuoc_khongThrow_khongDanhDauReady() throws IOException {
        // Khong start() server thuc su, tro toi 1 port CHAC CHAN khong ai lang nghe.
        RemoteConfigSyncService service = new RemoteConfigSyncService(new RestTemplateBuilder(),
                "http://localhost:1", "key", 3600, endpointRegistryCache, upstreamRegistryCache, upstreamHttpExecutor);

        // KHONG duoc throw - day la yeu cau quan trong nhat cua class nay (fail-open).
        service.sync();

        assertThat(service.isReady()).isFalse();
        verify(upstreamHttpExecutor, never()).invalidate(any());
    }

    @Test
    void sync_upstreamKhongDoiCauHinh_khongGoiInvalidate() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("key");
        service.sync();

        nextResponseBody.set(bundleJson("up-1", "svc", 1000)); // Y HET lan truoc.
        service.sync();

        verify(upstreamHttpExecutor, never()).invalidate(any());
    }

    @Test
    void sync_upstreamDoiConnectTimeout_goiInvalidateDungTen() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("key");
        service.sync();

        nextResponseBody.set(bundleJson("up-1", "svc", 2000)); // connectTimeoutMs doi.
        service.sync();

        verify(upstreamHttpExecutor).invalidate("svc");
    }

    @Test
    void sync_upstreamDoiTen_goiInvalidateCaTenCuVaTenMoi() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "ten-cu", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("key");
        service.sync();

        nextResponseBody.set(bundleJson("up-1", "ten-moi", 1000)); // cung id, doi ten.
        service.sync();

        verify(upstreamHttpExecutor).invalidate("ten-cu");
        verify(upstreamHttpExecutor).invalidate("ten-moi");
    }

    @Test
    void sync_upstreamMoiHoanToan_khongGoiInvalidate() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc-1", 1000));
        RemoteConfigSyncService service = startServerAndBuildService("key");
        service.sync();

        // Doi lan 2: upstream "up-1" khong con nua, upstream "up-2" HOAN TOAN moi
        // xuat hien - khong co gi de so sanh, khong duoc invalidate 1 ten chua tung
        // ton tai.
        nextResponseBody.set(bundleJson("up-2", "svc-2", 1000));
        service.sync();

        verify(upstreamHttpExecutor, never()).invalidate(any());
    }

    @Test
    void toEntity_mapDungTatCaField() throws IOException {
        nextResponseBody.set(bundleJson("up-1", "svc", 1234));
        RemoteConfigSyncService service = startServerAndBuildService("key");

        service.sync();

        UpstreamService mapped = upstreamRegistryCache.getById("up-1");
        assertThat(mapped.getId()).isEqualTo("up-1");
        assertThat(mapped.getName()).isEqualTo("svc");
        assertThat(mapped.getBaseHost()).isEqualTo("http://backend:8080");
        assertThat(mapped.getConnectTimeoutMs()).isEqualTo(1234);
        assertThat(mapped.getReadTimeoutMs()).isEqualTo(3000);
        assertThat(mapped.isCircuitBreakerEnabled()).isTrue();
        assertThat(mapped.getFailureRateThreshold()).isEqualTo(50);
        assertThat(mapped.isRetryEnabled()).isTrue();
        assertThat(mapped.getMaxConcurrentCalls()).isEqualTo(20);
        assertThat(mapped.getMaxWaitDurationMs()).isEqualTo(500);
    }
}
