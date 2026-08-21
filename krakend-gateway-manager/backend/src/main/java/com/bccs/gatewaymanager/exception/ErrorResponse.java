package com.bccs.gatewaymanager.exception;

import java.time.Instant;

/** Body loi chuan tra ve cho FE. */
public record ErrorResponse(
        String errorCode,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, Instant.now());
    }
}
