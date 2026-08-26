package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cho finding #4 (invalidate() phai xoa dung cac cache/registry theo ten
 * Upstream) va finding #8 (phan biet UpstreamHttpErrorException/
 * UpstreamTimeoutException thay vi 1 UpstreamCallException chung). Dung
 * com.sun.net.httpserver.HttpServer (co san trong JDK, khong can them thu
 * vien mock HTTP moi) de dung real HTTP server cuc bo thay vi mock RestTemplate
 * (RestTemplate duoc UpstreamHttpExecutor tu tao noi bo, khong inject duoc).
 */
class UpstreamHttpExecutorTest {

    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
    private final BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
    private final UpstreamHttpExecutor executor = new UpstreamHttpExecutor(
            circuitBreakerRegistry, retryRegistry, bulkheadRegistry,
            Mockito.mock(GatewayCacheService.class), new JsonMapper());

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private UpstreamService upstream(String name, int port) {
        return upstream(name, port, false);
    }

    private UpstreamService upstream(String name, int port, boolean retryEnabled) {
        return UpstreamService.builder()
                .name(name)
                .baseHost("http://localhost:" + port)
                .connectTimeoutMs(500)
                .readTimeoutMs(1000)
                .circuitBreakerEnabled(false)
                .retryEnabled(retryEnabled)
                .build();
    }

    // ---- Finding #4: invalidate() phai xoa CircuitBreaker/Retry/Bulkhead da tao truoc do theo ten ----

    @Test
    void invalidate_removesRegisteredResilience4jInstancesByName() {
        String name = "test-upstream";
        // Mo phong dung nhung gi circuitBreakerFor()/retryFor()/bulkheadFor() lam noi bo:
        // tao 1 lan dau tien theo ten (KHONG qua invalidate() thi supplier bi bo qua o lan sau).
        circuitBreakerRegistry.circuitBreaker(name);
        retryRegistry.retry(name);
        bulkheadRegistry.bulkhead(name);
        assertThat(circuitBreakerRegistry.find(name)).isPresent();
        assertThat(retryRegistry.find(name)).isPresent();
        assertThat(bulkheadRegistry.find(name)).isPresent();

        executor.invalidate(name);

        assertThat(circuitBreakerRegistry.find(name)).isEmpty();
        assertThat(retryRegistry.find(name)).isEmpty();
        assertThat(bulkheadRegistry.find(name)).isEmpty();
    }

    @Test
    void invalidate_unknownName_doesNothingHarmful() {
        assertThat(circuitBreakerRegistry.find("never-existed")).isEmpty();
        executor.invalidate("never-existed"); // khong duoc throw
    }

    // ---- Finding #8: phan biet loi HTTP that su (4xx/5xx) voi loi ha tang (timeout/connection refused) ----

    @Test
    void call_upstreamReturns404_throwsUpstreamHttpErrorExceptionWithStatusAndBody() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/x", exchange -> {
            byte[] body = "{\"error\":\"not found\"}".getBytes();
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        assertThatThrownBy(() -> executor.call(upstream("svc", port), HttpMethod.GET,
                "http://localhost:" + port + "/x", new HttpHeaders(), null, false, 300))
                .isInstanceOf(UpstreamHttpExecutor.UpstreamHttpErrorException.class)
                .satisfies(e -> {
                    UpstreamHttpExecutor.UpstreamHttpErrorException ex = (UpstreamHttpExecutor.UpstreamHttpErrorException) e;
                    assertThat(ex.httpStatus()).isEqualTo(404);
                    assertThat(ex.responseBody()).contains("not found");
                });
    }

    // ---- Regression tim thay sau khi them UpstreamHttpErrorException: retryFor() truoc do
    // van retry ca loi 4xx (khong bao gio thanh cong du thu lai), gay cham + tang tai vo ich
    // len upstream. Ca 2 test duoi day dung chung 1 dem so lan HttpServer nhan request that. ----

    @Test
    void call_upstream4xxWithRetryEnabled_isNotRetried() throws IOException {
        java.util.concurrent.atomic.AtomicInteger hitCount = new java.util.concurrent.atomic.AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/x", exchange -> {
            hitCount.incrementAndGet();
            byte[] body = "bad request".getBytes();
            exchange.sendResponseHeaders(400, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        assertThatThrownBy(() -> executor.call(upstream("svc-4xx", port, true), HttpMethod.GET,
                "http://localhost:" + port + "/x", new HttpHeaders(), null, false, 300))
                .isInstanceOf(UpstreamHttpExecutor.UpstreamHttpErrorException.class);

        assertThat(hitCount.get()).isEqualTo(1);
    }

    @Test
    void call_upstream5xxWithRetryEnabled_isRetriedUpToMaxAttempts() throws IOException {
        java.util.concurrent.atomic.AtomicInteger hitCount = new java.util.concurrent.atomic.AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/x", exchange -> {
            hitCount.incrementAndGet();
            byte[] body = "boom".getBytes();
            exchange.sendResponseHeaders(500, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        assertThatThrownBy(() -> executor.call(upstream("svc-5xx", port, true), HttpMethod.GET,
                "http://localhost:" + port + "/x", new HttpHeaders(), null, false, 300))
                .isInstanceOf(UpstreamHttpExecutor.UpstreamHttpErrorException.class);

        // maxAttempts(3) trong retryFor() - 5xx co the la loi tam thoi nen VAN duoc retry.
        assertThat(hitCount.get()).isEqualTo(3);
    }

    @Test
    void call_connectionRefused_throwsUpstreamTimeoutException() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        } // socket dong ngay sau khi lay port trong - dam bao khong co gi lang nghe tai day

        assertThatThrownBy(() -> executor.call(upstream("svc", closedPort), HttpMethod.GET,
                "http://localhost:" + closedPort + "/x", new HttpHeaders(), null, false, 300))
                .isInstanceOf(UpstreamHttpExecutor.UpstreamTimeoutException.class);
    }

    // ---- Cache Redis gio cau hinh theo tung BackendStep (tham so cacheEnabled/cacheTtlSeconds
    // cua call()), khong con theo UpstreamService - test xac nhan lan goi thu 2 cung URL voi
    // cacheEnabled=true KHONG cham toi HttpServer that (lay tu cache), con cacheEnabled=false
    // thi lan nao cung goi that. ----

    @Test
    void call_cacheEnabled_secondCallWithSameUrlHitsCacheNotServer() throws IOException {
        java.util.concurrent.atomic.AtomicInteger hitCount = new java.util.concurrent.atomic.AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/x", exchange -> {
            hitCount.incrementAndGet();
            byte[] body = "{\"value\":1}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        // GatewayCacheService that su luu/tra gia tri (khong mock rong nhu field "executor" dung
        // chung o tren) - dung 1 map in-memory don gian thay Redis that cho du test nay.
        var fakeCache = new java.util.concurrent.ConcurrentHashMap<String, String>();
        GatewayCacheService cacheService = Mockito.mock(GatewayCacheService.class);
        Mockito.when(cacheService.get(Mockito.anyString()))
                .thenAnswer(inv -> java.util.Optional.ofNullable(fakeCache.get(inv.getArgument(0))));
        Mockito.doAnswer(inv -> {
            fakeCache.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(cacheService).put(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        UpstreamHttpExecutor cachingExecutor = new UpstreamHttpExecutor(
                circuitBreakerRegistry, retryRegistry, bulkheadRegistry, cacheService, new JsonMapper());

        UpstreamService up = upstream("svc-cache", port);
        String url = "http://localhost:" + port + "/x";

        JsonNode first = cachingExecutor.call(up, HttpMethod.GET, url, new HttpHeaders(), null, true, 60);
        JsonNode second = cachingExecutor.call(up, HttpMethod.GET, url, new HttpHeaders(), null, true, 60);

        assertThat(first.get("value").asLong()).isEqualTo(1L);
        assertThat(second.get("value").asLong()).isEqualTo(1L);
        assertThat(hitCount.get()).isEqualTo(1); // lan 2 phai lay tu cache, khong cham server that
    }

    @Test
    void call_cacheDisabled_everyCallHitsServer() throws IOException {
        java.util.concurrent.atomic.AtomicInteger hitCount = new java.util.concurrent.atomic.AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/x", exchange -> {
            hitCount.incrementAndGet();
            byte[] body = "{\"value\":1}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();
        String url = "http://localhost:" + port + "/x";

        executor.call(upstream("svc-no-cache", port), HttpMethod.GET, url, new HttpHeaders(), null, false, 300);
        executor.call(upstream("svc-no-cache", port), HttpMethod.GET, url, new HttpHeaders(), null, false, 300);

        assertThat(hitCount.get()).isEqualTo(2); // cacheEnabled=false - khong lan nao duoc cache
    }
}
