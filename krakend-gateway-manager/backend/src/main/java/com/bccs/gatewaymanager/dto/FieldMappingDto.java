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

        /**
         * Bat buoc khi sourceType=STEP_RESPONSE hoac STEP_RESPONSE_ARRAY_AGGREGATE, bo qua khi REQUEST_BODY.
         * @Min(1) chi ap dung khi co gia tri (Integer nullable) - khong pha case REQUEST_BODY (null).
         */
        @Min(1) Integer sourceStepOrder,

        /** Bat buoc khi sourceType=STEP_RESPONSE hoac REQUEST_BODY. */
        String sourceField,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE. */
        String sourceArrayField,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE. */
        String sourceElementField,

        @Min(1) int targetStepOrder,

        @NotNull MappingTargetType targetType,

        @NotBlank String targetParamName,

        /**
         * Vi tri hien thi khi sap xep (trang "Khai bao endpoint keo tha") - KHONG anh huong
         * hanh vi engine. Payload cu (chua biet field nay, vi du tu 1 client/test cu) thieu
         * field nay se duoc Jackson mac dinh 0 (int primitive, khong throw).
         */
        int mappingOrder
) {
    public FieldMappingDto {
        if (sourceType == null) {
            sourceType = FieldMappingSourceType.STEP_RESPONSE;
        }
    }
}
