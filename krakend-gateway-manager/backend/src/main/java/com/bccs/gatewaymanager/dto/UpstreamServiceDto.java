package com.bccs.gatewaymanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/** Payload dang ky/hien thi 1 backend that (dung chung cho request va response). */
public record UpstreamServiceDto(
        String id,

        @NotBlank String name,

        String description,

        @NotBlank
        @jakarta.validation.constraints.Pattern(regexp = "^https?://.*", message = "baseHost phai bat dau bang http:// hoac https://")
        String baseHost,

        @Min(100) @Max(60000) int connectTimeoutMs,

        @Min(100) @Max(60000) int readTimeoutMs,

        boolean circuitBreakerEnabled,

        @Min(1) @Max(100) int failureRateThreshold,

        boolean retryEnabled,

        /** So luong lenh goi dong thoi toi da qua Bulkhead - xem UpstreamService.maxConcurrentCalls. */
        @Min(1) @Max(1000) int maxConcurrentCalls,

        /** Thoi gian (ms) cho "cho" trong Bulkhead truoc khi bi tu choi - 0 la gia tri hop le (khong cho, tu choi ngay neu day). */
        @Min(0) @Max(60000) int maxWaitDurationMs,

        Instant createdAt,

        Instant updatedAt
) {
    public UpstreamServiceDto {
        if (connectTimeoutMs <= 0) connectTimeoutMs = 1000;
        if (readTimeoutMs <= 0) readTimeoutMs = 3000;
        if (failureRateThreshold <= 0) failureRateThreshold = 50;
        if (maxConcurrentCalls <= 0) maxConcurrentCalls = 20;
    }
}
