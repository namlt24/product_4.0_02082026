package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.GatewayMethod;

import java.time.Instant;
import java.util.List;

/** DTO tra ve cho FE - bao gom id va timestamp, khac voi request DTO. */
public record EndpointResponseDto(
        String id,
        String name,
        String description,
        String path,
        GatewayMethod method,
        boolean sequential,
        String outputEncoding,
        List<BackendStepDto> steps,
        List<FieldMappingDto> mappings,
        Instant createdAt,
        Instant updatedAt,
        boolean idempotencyEnabled,
        int idempotencyTtlSeconds
) {
}
