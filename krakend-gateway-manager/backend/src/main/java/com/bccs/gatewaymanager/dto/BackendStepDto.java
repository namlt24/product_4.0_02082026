package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.GatewayMethod;
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

        String group,

        /** Ten field can "boc vo" response truoc khi mapping/allow/deny/group, vi du "data" (StandardResponse). */
        String target,

        List<String> allowFields,

        List<String> denyFields,

        Map<String, String> fieldRenameMapping
) {
}
