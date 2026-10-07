package com.bccs.gatewaymanager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Body loi chuan tra ve cho FE.
 *
 * Voi loi "Upstream tra ve HTTP 4xx/5xx" (GW-UPSTREAM-4XX/5XX), neu body loi
 * cua backend la JSON dang StandardResponse BCCS (co code/message/requestId/
 * shortTraceId/traceId/data) thi cac truong do duoc TACH ra top-level de
 * client khong phai parse message lenh - con cac loi GW khac thi cac truong
 * nay null va khong xuat hien trong JSON (NON_NULL).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errorCode,
        String message,
        Instant timestamp,
        String code,
        Object data,
        String requestId,
        String shortTraceId,
        String traceId
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, Instant.now(), null, null, null, null, null);
    }

    public static ErrorResponse ofUpstream(String errorCode, String message, String code, Object data,
                                           String requestId, String shortTraceId, String traceId) {
        return new ErrorResponse(errorCode, message, Instant.now(), code, data, requestId, shortTraceId, traceId);
    }
}
