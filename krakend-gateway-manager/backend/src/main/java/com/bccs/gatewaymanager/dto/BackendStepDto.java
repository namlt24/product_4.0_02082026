package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.fasterxml.jackson.annotation.JsonCreator;
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
         * Override Bulkhead RIENG cho step nay - null = dung chung Bulkhead cua
         * UpstreamService (hanh vi cu, khong doi). Xem BackendStep.maxConcurrentCallsOverride
         * ve y nghia/danh doi day du - khong tu dong kiem tra tong override co vuot suc
         * chiu that cua Upstream hay khong.
         */
        @Min(1) @Max(1000) Integer maxConcurrentCallsOverride,

        /** Chi co y nghia khi maxConcurrentCallsOverride khac null - null = dung UpstreamService.maxWaitDurationMs. */
        @Min(0) @Max(60000) Integer maxWaitDurationMsOverride,

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

        /**
         * V5b - Dieu kien cờ NHIEU NHOM khi conditionSourceType=SYSTEM_FLAG:
         * AND trong nhom, OR giua nhom (mirror Rule). Khac null/rong -> engine
         * danh gia nhom, bo qua dieu kien 1 co don le. Null = khong dung
         * (endpoint cu - backward-compat).
         */
        List<List<FlagRuleDto.FlagConditionDto>> conditionFlagGroups,

        /**
         * V6 - Dieu kien NHIEU NGUON (REQUEST_BODY/STEP_RESPONSE) NHIEU NHOM - tong
         * quat hoa conditionFlagGroups sang cho du lieu request/response thuc. Khac
         * null/rong -> engine danh gia NHOM nay, bo qua HOAN TOAN dieu kien 1 co don
         * le VA nhanh SYSTEM_FLAG/conditionFlagGroups. Xem BackendStep.conditionGroupsJson.
         */
        List<List<ConditionRuleDto>> conditionGroups,

        /** null = neu dieu kien DUNG thi ket thuc chuoi tai day (ket qua step nay la response cuoi cung). */
        Integer nextStepOrderIfTrue,

        /** null = neu dieu kien SAI thi ket thuc chuoi tai day. */
        Integer nextStepOrderIfFalse,

        /**
         * Fallback khi step nay LOI (upstream timeout/4xx/5xx) - null = throw ngay, ket thuc
         * ca chuoi (hanh vi cu). DOC LAP voi conditionOperator - dung duoc ca step tuan tu
         * thuong. Xem BackendStep.onErrorStepOrder.
         */
        Integer onErrorStepOrder,

        /**
         * "Wave" song song trong 1 chuoi sequential - xem BackendStep.parallelGroup.
         * Chi co y nghia khi EndpointConfig.sequential=true.
         */
        Integer parallelGroup,

        /**
         * Bu tru/rollback nghiep vu (saga best-effort) - xem
         * BackendStep.compensationUpstreamService. Ca 3 field deu tuy chon, PHAI
         * cung co hoac cung khong co (xem EndpointService.validateCompensationConfig()).
         */
        String compensationUpstreamServiceId,

        /** Ten Upstream bu tru, chi de hien thi - engine khong dung field nay (mirror upstreamServiceName). */
        String compensationUpstreamServiceName,

        GatewayMethod compensationMethod,

        String compensationUrlPattern,

        /**
         * Rẽ NHIỀU nhánh (switch) - xem {@link SwitchBranchDto}. null/rong = dung 2 nhanh
         * (nextStepOrderIfTrue/False) nhu cu. Dat CUOI record de constructor cu (36 tham so)
         * van dung duoc qua constructor phu ben duoi.
         */
        List<SwitchBranchDto> switchBranches,

        /**
         * Nhanh SAI (2 nhanh) hoac KHONG nhanh nao khop (switch): thay vi ket thuc/chuyen step, BAO LOI
         * voi ma + thong diep nay. null = dung nextStepOrderIfFalse nhu cu. Khi co, nextStepOrderIfFalse phai null.
         */
        String falseErrorCode,
        String falseErrorMessage
) {
    @JsonCreator
    public BackendStepDto {
        if (cacheTtlSeconds <= 0) {
            cacheTtlSeconds = 300;
        }
    }

    /** Giu tuong thich: cac cho goi/khoi tao cu (36 tham so, khong co switchBranches). */
    public BackendStepDto(String id, int stepOrder, String name, GatewayMethod method, String urlPattern,
                          String upstreamServiceId, String upstreamServiceName, boolean forwardOriginalBody,
                          boolean cacheEnabled, int cacheTtlSeconds, String group, String target,
                          List<String> allowFields, List<String> denyFields, Map<String, String> fieldRenameMapping,
                          Integer canvasX, Integer canvasY, Integer connectTimeoutMs, Integer readTimeoutMs,
                          Integer maxConcurrentCallsOverride, Integer maxWaitDurationMsOverride,
                          FieldMappingSourceType conditionSourceType, Integer conditionSourceStepOrder,
                          String conditionSourceField, ConditionOperator conditionOperator,
                          String conditionExpectedValue, List<List<FlagRuleDto.FlagConditionDto>> conditionFlagGroups,
                          List<List<ConditionRuleDto>> conditionGroups, Integer nextStepOrderIfTrue,
                          Integer nextStepOrderIfFalse, Integer onErrorStepOrder, Integer parallelGroup,
                          String compensationUpstreamServiceId, String compensationUpstreamServiceName,
                          GatewayMethod compensationMethod, String compensationUrlPattern) {
        this(id, stepOrder, name, method, urlPattern, upstreamServiceId, upstreamServiceName, forwardOriginalBody,
                cacheEnabled, cacheTtlSeconds, group, target, allowFields, denyFields, fieldRenameMapping, canvasX,
                canvasY, connectTimeoutMs, readTimeoutMs, maxConcurrentCallsOverride, maxWaitDurationMsOverride,
                conditionSourceType, conditionSourceStepOrder, conditionSourceField, conditionOperator,
                conditionExpectedValue, conditionFlagGroups, conditionGroups, nextStepOrderIfTrue, nextStepOrderIfFalse,
                onErrorStepOrder, parallelGroup, compensationUpstreamServiceId, compensationUpstreamServiceName,
                compensationMethod, compensationUrlPattern, null, null, null);
    }

    /** Giu tuong thich: cac cho goi 37 tham so (co switchBranches, chua co thong bao loi). */
    public BackendStepDto(String id, int stepOrder, String name, GatewayMethod method, String urlPattern,
                          String upstreamServiceId, String upstreamServiceName, boolean forwardOriginalBody,
                          boolean cacheEnabled, int cacheTtlSeconds, String group, String target,
                          List<String> allowFields, List<String> denyFields, Map<String, String> fieldRenameMapping,
                          Integer canvasX, Integer canvasY, Integer connectTimeoutMs, Integer readTimeoutMs,
                          Integer maxConcurrentCallsOverride, Integer maxWaitDurationMsOverride,
                          FieldMappingSourceType conditionSourceType, Integer conditionSourceStepOrder,
                          String conditionSourceField, ConditionOperator conditionOperator,
                          String conditionExpectedValue, List<List<FlagRuleDto.FlagConditionDto>> conditionFlagGroups,
                          List<List<ConditionRuleDto>> conditionGroups, Integer nextStepOrderIfTrue,
                          Integer nextStepOrderIfFalse, Integer onErrorStepOrder, Integer parallelGroup,
                          String compensationUpstreamServiceId, String compensationUpstreamServiceName,
                          GatewayMethod compensationMethod, String compensationUrlPattern,
                          List<SwitchBranchDto> switchBranches) {
        this(id, stepOrder, name, method, urlPattern, upstreamServiceId, upstreamServiceName, forwardOriginalBody,
                cacheEnabled, cacheTtlSeconds, group, target, allowFields, denyFields, fieldRenameMapping, canvasX,
                canvasY, connectTimeoutMs, readTimeoutMs, maxConcurrentCallsOverride, maxWaitDurationMsOverride,
                conditionSourceType, conditionSourceStepOrder, conditionSourceField, conditionOperator,
                conditionExpectedValue, conditionFlagGroups, conditionGroups, nextStepOrderIfTrue, nextStepOrderIfFalse,
                onErrorStepOrder, parallelGroup, compensationUpstreamServiceId, compensationUpstreamServiceName,
                compensationMethod, compensationUrlPattern, switchBranches, null, null);
    }
}
