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

        Integer idempotencyTtlSeconds,

        /**
         * Chi co y nghia khi sequential=false - bat song song hoa THAT SU cac step doc
         * lap qua thread pool rieng. Mac dinh tat (false). Xem EndpointConfig.parallelExecution.
         */
        boolean parallelExecution,

        /**
         * Cache TOAN BO response cho MOI client cung tham so - CHAN CUNG khi validate neu
         * endpoint hoac bat ky step nao khong phai GET (xem EndpointService.validateResponseCache()).
         * Mac dinh tat (false). Xem EndpointConfig.responseCacheEnabled.
         */
        boolean responseCacheEnabled,

        Integer responseCacheTtlSeconds
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
        if (responseCacheTtlSeconds == null || responseCacheTtlSeconds <= 0) {
            responseCacheTtlSeconds = 300;
        }
    }
}
