package com.bccs.gatewaymanager.dto;

/**
 * Snapshot suc khoe 1 Upstream Service tai thoi diem goi API - trang thai
 * circuit breaker (tu Resilience4j, KHONG luu DB) + hit-rate cache Redis
 * (dem trong-process tu luc app khoi dong, xem UpstreamHttpExecutor).
 *
 * failureRate/bufferedCalls = -1/0 khi circuit breaker CHUA TUNG duoc goi
 * lan nao (chua du so lan goi toi thieu de Resilience4j tinh ty le - xem
 * minimumNumberOfCalls trong UpstreamHttpExecutor.circuitBreakerFor()).
 * cacheHitRate = -1 khi upstream nay chua co step nao cacheEnabled=true
 * tung duoc goi (chua co du lieu de tinh ty le, KHAC voi 0% that su).
 */
public record UpstreamHealthDto(
        String id,
        String name,
        String baseHost,
        boolean circuitBreakerEnabled,
        /** CLOSED / OPEN / HALF_OPEN / DISABLED / FORCED_OPEN - xem io.github.resilience4j.circuitbreaker.CircuitBreaker.State. */
        String circuitState,
        float failureRatePercent,
        int bufferedCalls,
        long cacheHits,
        long cacheMisses,
        double cacheHitRate
) {
}
