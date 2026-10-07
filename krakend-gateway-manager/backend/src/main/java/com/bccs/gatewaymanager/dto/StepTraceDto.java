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
        String errorMessage,

        /**
         * V5 - THONG TIN RULE CỜ khop (khi co): "Rule 'Luồng VIP' khop -> bat
         * dau tu step 3" hoac "Rule 'Bao tri' khop -> CHAN (GW-FEATURE-DISABLED)".
         * null cho hop goi upstream thuong - chi co 1 dong dac biet o DAU
         * waterfall (hoac 1 dong thong bao BLOCK) de UI hien luong cờ da di.
         * stepOrder=-1 (khong phai step that), stepName="RULE", success=true
         * khi START_STEP; success=false + errorMessage khi BLOCK.
         */
        String matchedFlagRuleInfo,

        /**
         * Chi tiet 1 dieu kien re nhanh da danh gia (field da doc, gia tri thay duoc,
         * toan tu, ket qua). null ngoai dong "dieu kien". Dung de tim ra vi sao nhanh
         * bi roi sai (vd field khong co trong response) - xem TraceCollector.recordCondition().
         */
        String conditionInfo
) {
    /** Back-compat cho call site cu (UpstreamHttpExecutor - hop goi upstream thuong, khong co thong tin rule). */
    public StepTraceDto(int stepOrder, String stepName, String upstreamName, String method, String resolvedUrl,
                        String requestBody, boolean requestBodyTruncated, Integer responseStatus,
                        String responseBody, boolean responseBodyTruncated, long durationMs,
                        boolean cacheHit, boolean success, String errorMessage) {
        this(stepOrder, stepName, upstreamName, method, resolvedUrl, requestBody, requestBodyTruncated,
                responseStatus, responseBody, responseBodyTruncated, durationMs, cacheHit, success, errorMessage, null, null);
    }
}
