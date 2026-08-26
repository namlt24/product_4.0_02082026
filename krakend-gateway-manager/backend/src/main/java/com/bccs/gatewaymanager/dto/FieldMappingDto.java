package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO khai bao chain field giua 2 step trong composite API (hoac tu chinh body
 * cua client). Xem FieldMappingSourceType cho y nghia cua tung to hop field.
 */
public record FieldMappingDto(
        String id,

        @NotNull FieldMappingSourceType sourceType,

        /** Bat buoc khi sourceType=STEP_RESPONSE hoac STEP_RESPONSE_ARRAY_AGGREGATE, bo qua khi REQUEST_BODY. */
        Integer sourceStepOrder,

        /** Bat buoc khi sourceType=STEP_RESPONSE hoac REQUEST_BODY. */
        String sourceField,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE. */
        String sourceArrayField,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE. */
        String sourceElementField,

        @Min(1) int targetStepOrder,

        @NotNull MappingTargetType targetType,

        @NotBlank String targetParamName
) {
    public FieldMappingDto {
        if (sourceType == null) {
            sourceType = FieldMappingSourceType.STEP_RESPONSE;
        }
    }
}
