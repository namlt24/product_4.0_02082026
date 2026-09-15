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
        double cacheHitRate,
        /** Gioi han so luong lenh goi dong thoi (Bulkhead dung chung theo Upstream) - xem UpstreamService.maxConcurrentCalls. */
        int maxConcurrentCalls,
        /**
         * So "cho" Bulkhead con trong tai thoi diem xem man hinh - KHONG dung
         * bulkheadRegistry.bulkhead(name, supplier) (se tu tao 1 bulkhead moi chi vi dang
         * XEM man hinh health) - neu bulkhead CHUA TUNG duoc tao (chua co lenh goi nao qua
         * Upstream nay) thi toan bo permit con nguyen, tra thang maxConcurrentCalls (KHONG
         * dung sentinel -1 nhu failureRatePercent, vi "chua goi lan nao" o day co 1 gia tri
         * DUNG ro rang, khac han ty le loi chua co du lieu de tinh).
         */
        int availableConcurrentCalls,
        /** Dem dong tu luc app khoi dong - xem UpstreamHttpExecutor.bulkheadRejectionCount(). */
        long bulkheadRejectedCount,
        /**
         * true = maxConcurrentCalls van dang o gia tri mac dinh luc tao (chua tung duoc
         * chinh rieng) - CANH BAO khong co nghia "sai chac chan", chi la nhac nguoi van hanh
         * kiem tra lai theo cong thuc P99(giay) x rps thuc te x 1.2 (xem
         * HƯỚNG_DẪN_PRODUCTION.md muc 8) truoc khi nhan tai that, vi mac dinh 20 thuong qua
         * thap cho 1 Upstream dang phuc vu traffic san xuat.
         */
        boolean usingDefaultBulkheadSizing
) {
}
