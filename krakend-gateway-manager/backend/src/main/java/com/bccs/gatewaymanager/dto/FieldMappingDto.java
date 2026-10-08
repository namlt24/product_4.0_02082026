package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.MappingTargetContext;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.fasterxml.jackson.annotation.JsonCreator;
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

        /** Bat buoc khi sourceType=CONSTANT - gia tri hang so co dinh, khong doc tu request/response. */
        String constantValue,

        @Min(1) int targetStepOrder,

        @NotNull MappingTargetType targetType,

        @NotBlank String targetParamName,

        /**
         * Vi tri hien thi khi sap xep (trang "Khai bao endpoint keo tha") - KHONG anh huong
         * hanh vi engine. Payload cu (chua biet field nay, vi du tu 1 client/test cu) thieu
         * field nay se duoc Jackson mac dinh 0 (int primitive, khong throw).
         */
        int mappingOrder,

        /**
         * Mapping nay xay dung loi goi CHINH (MAIN, mac dinh) hay loi goi BU TRU/
         * rollback (COMPENSATION) cua targetStepOrder - xem MappingTargetContext.
         * Payload cu (chua biet field nay) tu nhan MAIN qua compact constructor,
         * dung y het cach sourceType mac dinh STEP_RESPONSE.
         */
        MappingTargetContext targetContext,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_FIND - xem FieldMapping.filterField. */
        String filterField,

        /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_FIND - xem FieldMapping.filterValue. */
        String filterValue
) {
    /** Canonical constructor (14 field) la constructor duy nhat Jackson duoc dung de doc JSON. */
    @JsonCreator
    public FieldMappingDto {
        if (sourceType == null) {
            sourceType = FieldMappingSourceType.STEP_RESPONSE;
        }
        if (targetContext == null) {
            targetContext = MappingTargetContext.MAIN;
        }
    }

    /** Dang 12 field (truoc khi co STEP_RESPONSE_ARRAY_FIND) - giu tuong thich cac cho goi cu. */
    public FieldMappingDto(String id, FieldMappingSourceType sourceType, Integer sourceStepOrder, String sourceField,
                            String sourceArrayField, String sourceElementField, String constantValue,
                            int targetStepOrder, MappingTargetType targetType, String targetParamName,
                            int mappingOrder, MappingTargetContext targetContext) {
        this(id, sourceType, sourceStepOrder, sourceField, sourceArrayField, sourceElementField, constantValue,
                targetStepOrder, targetType, targetParamName, mappingOrder, targetContext, null, null);
    }
}
