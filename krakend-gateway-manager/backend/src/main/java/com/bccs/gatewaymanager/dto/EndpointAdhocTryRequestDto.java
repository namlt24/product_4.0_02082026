package com.bccs.gatewaymanager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Payload cho "Thu nhanh" 1 draft CHUA LUU (xem EndpointTryService.tryAdhoc()) -
 * dung khi khai bao endpoint qua Canvas, cho phep xem truoc request/response
 * TRUOC khi bam Luu. `endpoint` la CHINH payload se gui khi Luu that (dung lai
 * EndpointRequestDto.toPayload() ben frontend, khong xay dung rieng) - validate
 * qua @Valid/@NotNull o day chi bat loi hinh thuc (field @NotBlank/@Pattern...),
 * validate nghiep vu sau (stepOrder/branching/compensation...) do
 * EndpointService.validate() dam nhiem ben trong EndpointTryService.
 */
public record EndpointAdhocTryRequestDto(
        @NotNull @Valid EndpointRequestDto endpoint,
        Map<String, String> pathVariables,
        Map<String, String> queryParams,
        String body
) {
    public EndpointAdhocTryRequestDto {
        if (pathVariables == null) pathVariables = Map.of();
        if (queryParams == null) queryParams = Map.of();
    }
}
