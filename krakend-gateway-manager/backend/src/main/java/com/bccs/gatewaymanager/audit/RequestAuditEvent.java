package com.bccs.gatewaymanager.audit;

import java.time.Instant;

/**
 * 1 document trong index "gwm-requests-*" - dai dien 1 lan client THAT goi vao
 * 1 Endpoint composite (qua DynamicDispatcherController). Xem HopAuditEvent
 * cho chi tiet tung Backend Step ben trong (join qua requestId).
 */
public record RequestAuditEvent(
        String requestId,
        Instant timestamp,
        String endpointId,
        String endpointName,
        String clientMethod,
        String clientPath,
        /** SUCCESS | ERROR */
        String status,
        Integer httpStatus,
        String errorCode,
        String errorMessage,
        long durationMs,
        String requestBody,
        boolean requestBodyTruncated,
        /** trace.id cua Elastic APM (neu Java agent co gan) - link sang APM trace tuong ung. */
        String traceId
) {
}
