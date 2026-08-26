package com.bccs.gatewaymanager.audit;

import java.time.Instant;

/**
 * 1 document trong index "gwm-hops-*" - dai dien 1 lan UpstreamHttpExecutor.call()
 * (= 1 Backend Step, tinh SAU KHI cache/retry/circuit-breaker da giai quyet xong,
 * KHONG nhan ban theo tung lan retry rieng le). requestId join sang RequestAuditEvent
 * de dung lai thanh "waterfall" tung hop cua 1 request.
 */
public record HopAuditEvent(
        String requestId,
        int stepOrder,
        String stepName,
        String upstreamName,
        String method,
        String resolvedUrl,
        String requestBody,
        boolean requestBodyTruncated,
        Integer responseStatus,
        String responseBody,
        boolean responseBodyTruncated,
        long durationMs,
        boolean cacheHit,
        boolean success,
        String errorMessage,
        Instant timestamp
) {
}
