package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.audit.AuditLogService;
import com.bccs.gatewaymanager.audit.BodyTruncator;
import com.bccs.gatewaymanager.audit.HopAuditEvent;
import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.SystemException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * Thuc su thuc hien 1 lan goi HTTP ra Upstream, boc trong Redis cache-aside
 * (chi cho GET/POST + cacheEnabled cua tung BackendStep - xem tham so call(),
 * KHONG phai cua UpstreamService, vi 1 Upstream bi nhieu step goi toi nhieu ham/
 * path khac nhau, khong phai ham nao cung nen cache; PUT/PATCH/DELETE luon bi
 * chan cache vi gan nhu chac chan la mutating - xem GatewayCacheService.buildKey()
 * ve cach key phan biet theo body cho POST) va Resilience4j CircuitBreaker/
 * Retry/Bulkhead - rieng 3 cai nay van dat TEN THEO upstream.getName(), tao
 * dong tai lan goi dau (khong the dung @CircuitBreaker/@Cacheable tinh vi
 * backend duoc chon dong theo cau hinh DB, khong phai theo chu ky method co dinh).
 *
 * Thu tu boc decorator (dung khuyen nghi Resilience4j): Bulkhead trong cung,
 * CircuitBreaker boc ngoai Bulkhead, Retry boc ngoai cung - de moi lan retry
 * deu di qua kiem tra circuit breaker/bulkhead, khong bo qua.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpstreamHttpExecutor {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final GatewayCacheService cacheService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    private final Map<RestTemplateKey, RestTemplate> restTemplateCache = new ConcurrentHashMap<>();

    /**
     * RestTemplate cua Spring bake connectTimeout/readTimeout NGAY LUC BUILD qua
     * RestTemplateBuilder (khong sua duoc per-request) - nen key cache phai gom
     * ca 2 gia tri timeout HIEU LUC (khong chi ten upstream) de 1 BackendStep co
     * the override rieng ma khong anh huong RestTemplate dung chung cua cac step
     * khac cung Upstream nhung khong override (xem restTemplateFor()).
     */
    private record RestTemplateKey(String upstreamName, int connectMs, int readMs) {
    }

    // Dem hit/miss cache THEO TEN UPSTREAM (khong theo tung key rieng - qua nhieu key
    // se ton bo nho vo ich) - phuc vu man hinh "Dashboard suc khoe Upstream". Dat o day
    // (khong phai GatewayCacheService) vi upstream.getName() da co san tai day, tranh
    // GatewayCacheService phai parse nguoc ten upstream tu chuoi key.
    private final Map<String, LongAdder> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> cacheMisses = new ConcurrentHashMap<>();

    /** Ket qua 1 lan goi HTTP THAT (khac cache-hit) - giu ca status vi ban than JsonNode khong mang status. */
    private record HttpCallResult(int status, JsonNode body) {
    }

    /**
     * @param stepOrder/stepName chi de ghi audit log (xem HopAuditEvent) - KHONG anh huong hanh vi goi.
     * @param stepConnectTimeoutMs/stepReadTimeoutMs override rieng cua BackendStep - null = dung
     *        dung mac dinh cua UpstreamService (xem BackendStep.connectTimeoutMs/readTimeoutMs).
     */
    public JsonNode call(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                          HttpHeaders headers, JsonNode body, boolean cacheEnabled, int cacheTtlSeconds,
                          int stepOrder, String stepName, Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs) {
        long startNanos = System.nanoTime();
        // POST duoc cho cache TU 2026-09 (truoc do chi GET) - dung cho API tim kiem/tra
        // cuu truyen filter qua body (POST khong sua/tao du lieu that su). PUT/PATCH/DELETE
        // VAN luon bi chan cache vi gan nhu chac chan la mutating. Rui ro cache nham 1 lenh
        // goi co side-effect (tra ket qua cu thay vi thuc thi that) do NGUOI QUAN LY cau hinh
        // (nguoi bat cacheEnabled cho tung BackendStep) tu chiu trach nhiem - he thong KHONG
        // con chan cung theo method nhu truoc, chi con chan PUT/PATCH/DELETE.
        boolean cacheable = cacheEnabled && (method == HttpMethod.GET || method == HttpMethod.POST);
        String cacheKey = cacheable ? GatewayCacheService.buildKey(upstream.getName(), method.name(), resolvedUrl, body) : null;

        // Bien tam de xay HopAuditEvent trong finally, du di theo nhanh nao (cache-hit/
        // that-cong/loi) - JAVA khong cho bien local final duoc gan lai trong try/catch,
        // nen khai bao thuong (mutable) o day.
        boolean cacheHit = false;
        Integer responseStatus = null;
        String responseBodyForAudit = null;
        String errorMessage = null;
        boolean success = true;

        try {
            if (cacheable) {
                Optional<String> cached = cacheService.get(cacheKey);
                if (cached.isPresent()) {
                    try {
                        cacheHits.computeIfAbsent(upstream.getName(), n -> new LongAdder()).increment();
                        cacheHit = true;
                        responseBodyForAudit = cached.get();
                        return objectMapper.readTree(cached.get());
                    } catch (Exception e) {
                        log.warn("Cache gia tri hong cho key={}, bo qua cache: {}", cacheKey, e.getMessage());
                        cacheHit = false;
                    }
                } else {
                    cacheMisses.computeIfAbsent(upstream.getName(), n -> new LongAdder()).increment();
                }
            }

            Supplier<HttpCallResult> callSupplier = () ->
                    doHttpCall(upstream, method, resolvedUrl, headers, body, stepConnectTimeoutMs, stepReadTimeoutMs);
            Supplier<HttpCallResult> decorated = Bulkhead.decorateSupplier(bulkheadFor(upstream), callSupplier);
            if (upstream.isCircuitBreakerEnabled()) {
                decorated = CircuitBreaker.decorateSupplier(circuitBreakerFor(upstream), decorated);
            }
            if (upstream.isRetryEnabled()) {
                decorated = Retry.decorateSupplier(retryFor(upstream), decorated);
            }

            HttpCallResult callResult = decorated.get();
            responseStatus = callResult.status();
            responseBodyForAudit = callResult.body().toString();

            if (cacheable) {
                cacheService.put(cacheKey, callResult.body().toString(), cacheTtlSeconds);
            }
            return callResult.body();
        } catch (RuntimeException e) {
            success = false;
            errorMessage = e.getMessage();
            if (e instanceof UpstreamHttpErrorException httpError) {
                responseStatus = httpError.httpStatus();
                responseBodyForAudit = httpError.responseBody();
            }
            throw e;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            BodyTruncator.Result requestBodyResult = BodyTruncator.truncate(body == null ? null : body.toString());
            BodyTruncator.Result responseBodyResult = BodyTruncator.truncate(responseBodyForAudit);
            HopAuditEvent event = new HopAuditEvent(
                    MDC.get("requestId"), stepOrder, stepName, upstream.getName(), method.name(), resolvedUrl,
                    requestBodyResult.body(), requestBodyResult.truncated(),
                    responseStatus,
                    responseBodyResult.body(), responseBodyResult.truncated(),
                    durationMs, cacheHit, success, errorMessage, Instant.now());
            auditLogService.recordHop(event);
        }
    }

    private HttpCallResult doHttpCall(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                                       HttpHeaders headers, JsonNode body,
                                       Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs) {
        RestTemplate restTemplate = restTemplateFor(upstream, stepConnectTimeoutMs, stepReadTimeoutMs);
        try {
            String bodyString = body == null ? null : body.toString();
            ResponseEntity<String> response = restTemplate.exchange(
                    resolvedUrl, method, new HttpEntity<>(bodyString, headers), String.class);
            String responseBody = response.getBody();
            JsonNode parsed = (responseBody == null || responseBody.isBlank())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(responseBody);
            return new HttpCallResult(response.getStatusCode().value(), parsed);
        } catch (RestClientResponseException e) {
            // Upstream tra ve HTTP loi that su (4xx/5xx) - giu nguyen status/body de client
            // phan biet duoc "input sai"/"upstream bao loi nghiep vu" thay vi 1 mã 500 chung.
            log.warn("Upstream '{}' [{} {}] tra ve HTTP {}: {}", upstream.getName(), method, resolvedUrl,
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new UpstreamHttpErrorException(upstream.getName(), e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // Connect timeout / read timeout / connection refused - ha tang that su co van
            // de, khac han "upstream tra loi 4xx/5xx" o tren.
            log.warn("Loi ket noi Upstream '{}' [{} {}]: {}", upstream.getName(), method, resolvedUrl, e.getMessage());
            throw new UpstreamTimeoutException(upstream.getName(), e);
        } catch (Exception e) {
            log.warn("Loi goi Upstream '{}' [{} {}]: {}", upstream.getName(), method, resolvedUrl, e.getMessage());
            throw new UpstreamCallException(upstream.getName(), e);
        }
    }

    /**
     * Xoa RestTemplate/CircuitBreaker/Retry/Bulkhead da tao san cho 1 Upstream (theo
     * ten) - goi khi UpstreamServiceService.update()/delete() doi cau hinh, vi
     * cac cache/registry o tren chi tao instance 1 LAN DAU (computeIfAbsent/
     * registry.xxx(name, supplier) bo qua supplier moi neu instance da ton tai) -
     * khong xoa thi doi timeout/threshold xong se KHONG co hieu luc cho toi khi
     * restart app. Chi can remove(), khong can replace(): lan goi tiep theo se tu
     * rebuild voi cau hinh moi nhat tu DB qua computeIfAbsent/circuitBreaker(...).
     */
    public void invalidate(String upstreamName) {
        // Sau khi them override timeout theo BackendStep, 1 Upstream co the co
        // NHIEU RestTemplate da cache (moi (connectMs, readMs) hieu luc khac nhau
        // la 1 key rieng - xem RestTemplateKey) - phai xoa HET cac key thuoc
        // upstream nay, khong chi 1 key co dinh nhu truoc.
        restTemplateCache.keySet().removeIf(key -> key.upstreamName().equals(upstreamName));
        circuitBreakerRegistry.remove(upstreamName);
        retryRegistry.remove(upstreamName);
        bulkheadRegistry.remove(upstreamName);
    }

    /** Dung cho UpstreamHealthService - 0 neu upstream chua tung co lan cache-eligible nao. */
    public long cacheHitCount(String upstreamName) {
        LongAdder a = cacheHits.get(upstreamName);
        return a == null ? 0 : a.sum();
    }

    public long cacheMissCount(String upstreamName) {
        LongAdder a = cacheMisses.get(upstreamName);
        return a == null ? 0 : a.sum();
    }

    private RestTemplate restTemplateFor(UpstreamService upstream, Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs) {
        // Spring Boot 4 (spring-boot-restclient): setConnectTimeout/setReadTimeout doi ten
        // thanh connectTimeout/readTimeout (bo tien to "set", theo quy uoc builder moi).
        int effectiveConnectMs = stepConnectTimeoutMs != null ? stepConnectTimeoutMs : upstream.getConnectTimeoutMs();
        int effectiveReadMs = stepReadTimeoutMs != null ? stepReadTimeoutMs : upstream.getReadTimeoutMs();
        RestTemplateKey key = new RestTemplateKey(upstream.getName(), effectiveConnectMs, effectiveReadMs);
        return restTemplateCache.computeIfAbsent(key, k -> new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(k.connectMs()))
                .readTimeout(Duration.ofMillis(k.readMs()))
                .build());
    }

    private CircuitBreaker circuitBreakerFor(UpstreamService upstream) {
        return circuitBreakerRegistry.circuitBreaker(upstream.getName(), () -> CircuitBreakerConfig.custom()
                .failureRateThreshold(upstream.getFailureRateThreshold())
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }

    private Bulkhead bulkheadFor(UpstreamService upstream) {
        return bulkheadRegistry.bulkhead(upstream.getName(), () -> BulkheadConfig.custom()
                .maxConcurrentCalls(upstream.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(upstream.getMaxWaitDurationMs()))
                .build());
    }

    private Retry retryFor(UpstreamService upstream) {
        return retryRegistry.retry(upstream.getName(), () -> RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(200), 2.0, 0.5))
                // retryOnException thay vi ignoreExceptions co dinh: KHONG retry khi circuit
                // breaker da mo (fail fast that su, khong doi backoff vo ich) VA khong retry
                // khi upstream tra ve 4xx that su (loi do client/du lieu sai, retry lai van
                // se tra ve dung 4xx do, chi ton them ~600ms-1s + tang tai vo ich len upstream
                // moi lan). Van RETRY cho 5xx/timeout - nhung truong hop co the la tam thoi.
                .retryOnException(UpstreamHttpExecutor::isRetryable)
                .build());
    }

    private static boolean isRetryable(Throwable e) {
        if (e instanceof CallNotPermittedException) {
            return false;
        }
        if (e instanceof UpstreamHttpErrorException httpError) {
            return httpError.httpStatus() >= 500;
        }
        return true;
    }

    /** Boc loi goi Upstream khong xac dinh duoc nguyen nhan cu the (khac 2 loai duoi day) thanh 1 kieu chung de engine xu ly. */
    public static class UpstreamCallException extends SystemException {
        public UpstreamCallException(String upstreamName, Throwable cause) {
            super("Loi goi Upstream Service '" + upstreamName + "': " + cause.getMessage(), cause);
        }
    }

    /** Upstream tra ve HTTP loi that su (4xx/5xx) - giu status + body goc de GlobalExceptionHandler map ra ma loi phan biet duoc. */
    public static class UpstreamHttpErrorException extends SystemException {
        private final int httpStatus;
        private final String responseBody;

        public UpstreamHttpErrorException(String upstreamName, int httpStatus, String responseBody) {
            super("Upstream '" + upstreamName + "' tra ve HTTP " + httpStatus + ": " + responseBody);
            this.httpStatus = httpStatus;
            this.responseBody = responseBody;
        }

        public int httpStatus() {
            return httpStatus;
        }

        public String responseBody() {
            return responseBody;
        }
    }

    /** Timeout ket noi/doc du lieu hoac connection refused toi Upstream - ha tang that su co van de, khac loi nghiep vu 4xx/5xx. */
    public static class UpstreamTimeoutException extends SystemException {
        public UpstreamTimeoutException(String upstreamName, Throwable cause) {
            super("Upstream '" + upstreamName + "' khong phan hoi kip thoi (timeout/connection refused): " + cause.getMessage(), cause);
        }
    }
}
