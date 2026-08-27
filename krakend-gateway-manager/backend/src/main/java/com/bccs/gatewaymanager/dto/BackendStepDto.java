package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/** DTO cho 1 backend step (request va response deu dung chung shape nay). */
public record BackendStepDto(
        String id,

        @Min(1) int stepOrder,

        @NotBlank String name,

        @NotNull GatewayMethod method,

        @NotBlank String urlPattern,

        /** Id cua UpstreamService da dang ky - thay the danh sach "hosts" go tay truoc day. */
        @NotBlank String upstreamServiceId,

        /** Ten UpstreamService, chi de hien thi tren UI - engine khong dung field nay. */
        String upstreamServiceName,

        /** true = lay nguyen body goc cua client lam nen truoc khi ap BODY_FIELD mapping. */
        boolean forwardOriginalBody,

        /** Cache Redis rieng cho step nay (chi GET) - xem BackendStep.cacheEnabled. */
        boolean cacheEnabled,

        @Min(1) @Max(86400) int cacheTtlSeconds,

        String group,

        /** Ten field can "boc vo" response truoc khi mapping/allow/deny/group, vi du "data" (StandardResponse). */
        String target,

        List<String> allowFields,

        List<String> denyFields,

        Map<String, String> fieldRenameMapping,

        /** Vi tri tren canvas "khai bao endpoint keo tha" - null = chua duoc keo tha, FE tu suy auto-layout theo stepOrder. */
        Integer canvasX,

        Integer canvasY,

        /** Override rieng cho step nay - null = dung mac dinh cua UpstreamService (xem BackendStep.connectTimeoutMs). */
        @Min(100) @Max(60000) Integer connectTimeoutMs,

        @Min(100) @Max(60000) Integer readTimeoutMs,

        /**
         * Re nhanh (P1-5) - tat ca nullable, khong khai bao (conditionOperator=null)
         * = step chay binh thuong theo dung stepOrder ke tiep, khong doi hanh vi cu.
         * Chi dung STEP_RESPONSE/REQUEST_BODY (khong dung STEP_RESPONSE_ARRAY_AGGREGATE -
         * so sanh 1 mang khong co y nghia cho dieu kien dung/sai).
         */
        FieldMappingSourceType conditionSourceType,

        Integer conditionSourceStepOrder,

        String conditionSourceField,

        ConditionOperator conditionOperator,

        /** Chi dung khi conditionOperator=EQUALS/NOT_EQUALS. */
        String conditionExpectedValue,

        /** null = neu dieu kien DUNG thi ket thuc chuoi tai day (ket qua step nay la response cuoi cung). */
        Integer nextStepOrderIfTrue,

        /** null = neu dieu kien SAI thi ket thuc chuoi tai day. */
        Integer nextStepOrderIfFalse
) {
    public BackendStepDto {
        if (cacheTtlSeconds <= 0) {
            cacheTtlSeconds = 300;
        }
    }
}
