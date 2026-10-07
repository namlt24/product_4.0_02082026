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
        int idempotencyTtlSeconds,
        boolean parallelExecution,

        /** Cache TOAN BO response cho MOI client cung tham so - xem EndpointConfig.responseCacheEnabled. */
        boolean responseCacheEnabled,

        int responseCacheTtlSeconds,

        /** Mang RULE CỜ (V5) - "khop tổ hợp cờ -> bắt đầu từ step nào / chặn request". Rỗng = không dùng. */
        List<FlagRuleDto> flagRules,

        /** Cấu hình "ghép field từ bất kỳ step nào vào response cuối" - null = không dùng. Xem ResponseMappingSpecDto. */
        ResponseMappingSpecDto responseMapping
) {
}
