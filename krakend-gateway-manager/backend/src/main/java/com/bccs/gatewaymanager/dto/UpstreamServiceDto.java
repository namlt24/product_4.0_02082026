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

        boolean cacheEnabled,

        @Min(1) @Max(86400) int cacheTtlSeconds,

        Instant createdAt,

        Instant updatedAt
) {
    public UpstreamServiceDto {
        if (connectTimeoutMs <= 0) connectTimeoutMs = 1000;
        if (readTimeoutMs <= 0) readTimeoutMs = 3000;
        if (failureRateThreshold <= 0) failureRateThreshold = 50;
        if (cacheTtlSeconds <= 0) cacheTtlSeconds = 300;
    }
}
