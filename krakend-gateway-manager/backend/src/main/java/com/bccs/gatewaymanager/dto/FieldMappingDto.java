package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.MappingTargetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO khai bao chain field giua 2 step trong composite API. */
public record FieldMappingDto(
        String id,

        @Min(1) int sourceStepOrder,

        @NotBlank String sourceField,

        @Min(1) int targetStepOrder,

        @NotNull MappingTargetType targetType,

        @NotBlank String targetParamName
) {
}
