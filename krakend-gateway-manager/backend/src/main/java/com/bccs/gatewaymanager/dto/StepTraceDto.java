package com.bccs.gatewaymanager.dto;

/**
 * 1 dong trong waterfall tung step tra ve boi tinh nang "Thu nhanh" (xem
 * EndpointTryService/TraceCollector) - mirror dung HopAuditEvent (audit
 * Elasticsearch), bo cac field chi phuc vu ES (requestId - "Thu nhanh"
 * khong join sang request nao khac; timestamp - khong can, ket qua chi
 * dung 1 lan ngay luc goi).
 */
public record StepTraceDto(
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
        String errorMessage
) {
}
