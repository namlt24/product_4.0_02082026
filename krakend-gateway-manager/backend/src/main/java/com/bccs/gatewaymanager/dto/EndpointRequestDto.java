package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.GatewayMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/** Payload tao/sua 1 endpoint gateway - dung cho POST/PUT va cho endpoint preview khong luu DB. */
public record EndpointRequestDto(
        @NotBlank String name,

        String description,

        @NotBlank
        @Pattern(regexp = "^/.*", message = "path phai bat dau bang '/'")
        String path,

        @NotNull GatewayMethod method,

        boolean sequential,

        String outputEncoding,

        @NotEmpty @Valid List<BackendStepDto> steps,

        @Valid List<FieldMappingDto> mappings,

        /**
         * Bat idempotency-key (client gui header "Idempotency-Key") - mac dinh tat (false),
         * moi endpoint cu giu nguyen hanh vi hien tai. Xem EndpointConfig.idempotencyEnabled.
         */
        boolean idempotencyEnabled,

        Integer idempotencyTtlSeconds
) {
    public EndpointRequestDto {
        if (outputEncoding == null || outputEncoding.isBlank()) {
            outputEncoding = "json";
        }
        if (mappings == null) {
            mappings = List.of();
        }
        if (idempotencyTtlSeconds == null || idempotencyTtlSeconds <= 0) {
            idempotencyTtlSeconds = 86400;
        }
    }
}
