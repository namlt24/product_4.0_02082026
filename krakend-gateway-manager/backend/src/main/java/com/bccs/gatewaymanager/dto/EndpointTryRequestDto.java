package com.bccs.gatewaymanager.dto;

import java.util.Map;

/** Payload cho "Thu ngay" - nguoi dung tu nhap gia tri gia lap 1 request that cua client. */
public record EndpointTryRequestDto(
        Map<String, String> pathVariables,
        Map<String, String> queryParams,
        String body
) {
    public EndpointTryRequestDto {
        if (pathVariables == null) pathVariables = Map.of();
        if (queryParams == null) queryParams = Map.of();
    }
}
