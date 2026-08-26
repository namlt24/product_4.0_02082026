package com.bccs.gatewaymanager.engine;

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
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Thuc su thuc hien 1 lan goi HTTP ra Upstream, boc trong Redis cache-aside
 * (chi cho GET + upstream.cacheEnabled) va Resilience4j CircuitBreaker/Retry/
 * Bulkhead - tat ca dat TEN THEO upstream.getName(), tao dong tai lan goi dau
 * (khong the dung @CircuitBreaker/@Cacheable tinh vi backend duoc chon dong
 * theo cau hinh DB, khong phai theo chu ky method co dinh).
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

    private final Map<String, RestTemplate> restTemplateCache = new ConcurrentHashMap<>();

    public JsonNode call(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                          HttpHeaders headers, JsonNode body) {
        boolean cacheable = upstream.isCacheEnabled() && method == HttpMethod.GET;
        String cacheKey = cacheable ? GatewayCacheService.buildKey(upstream.getName(), method.name(), resolvedUrl) : null;

        if (cacheable) {
            Optional<String> cached = cacheService.get(cacheKey);
            if (cached.isPresent()) {
                try {
                    return objectMapper.readTree(cached.get());
                } catch (Exception e) {
                    log.warn("Cache gia tri hong cho key={}, bo qua cache: {}", cacheKey, e.getMessage());
                }
            }
        }

        Supplier<JsonNode> call = () -> doHttpCall(upstream, method, resolvedUrl, headers, body);

        Supplier<JsonNode> decorated = Bulkhead.decorateSupplier(bulkheadFor(upstream), call);
        if (upstream.isCircuitBreakerEnabled()) {
            decorated = CircuitBreaker.decorateSupplier(circuitBreakerFor(upstream), decorated);
        }
        if (upstream.isRetryEnabled()) {
            decorated = Retry.decorateSupplier(retryFor(upstream), decorated);
        }

        JsonNode result = decorated.get();

        if (cacheable) {
            cacheService.put(cacheKey, result.toString(), upstream.getCacheTtlSeconds());
        }
        return result;
    }

    private JsonNode doHttpCall(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                                 HttpHeaders headers, JsonNode body) {
        RestTemplate restTemplate = restTemplateFor(upstream);
        try {
            String bodyString = body == null ? null : body.toString();
            ResponseEntity<String> response = restTemplate.exchange(
                    resolvedUrl, method, new HttpEntity<>(bodyString, headers), String.class);
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            log.warn("Loi goi Upstream '{}' [{} {}]: {}", upstream.getName(), method, resolvedUrl, e.getMessage());
            throw new UpstreamCallException(upstream.getName(), e);
        }
    }

    private RestTemplate restTemplateFor(UpstreamService upstream) {
        // Spring Boot 4 (spring-boot-restclient): setConnectTimeout/setReadTimeout doi ten
        // thanh connectTimeout/readTimeout (bo tien to "set", theo quy uoc builder moi).
        return restTemplateCache.computeIfAbsent(upstream.getName(), name -> new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(upstream.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(upstream.getReadTimeoutMs()))
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
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofMillis(500))
                .build());
    }

    private Retry retryFor(UpstreamService upstream) {
        return retryRegistry.retry(upstream.getName(), () -> RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(200), 2.0, 0.5))
                // Khong retry khi circuit breaker da mo (fail fast that su, khong doi backoff vo ich).
                .ignoreExceptions(CallNotPermittedException.class)
                .build());
    }

    /** Boc loi goi Upstream (timeout/connection refused/HTTP error) thanh 1 kieu chung de engine xu ly. */
    public static class UpstreamCallException extends SystemException {
        public UpstreamCallException(String upstreamName, Throwable cause) {
            super("Loi goi Upstream Service '" + upstreamName + "': " + cause.getMessage(), cause);
        }
    }
}
