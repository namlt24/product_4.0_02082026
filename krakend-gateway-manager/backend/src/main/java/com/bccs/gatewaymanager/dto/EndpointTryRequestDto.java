package com.bccs.gatewaymanager.dto;

import java.util.Map;

/** Payload cho "Thu ngay" - nguoi dung tu nhap gia tri gia lap 1 request that cua client. */
public record EndpointTryRequestDto(
        Map<String, String> pathVariables,
        Map<String, String> queryParams,
        String body,
        /**
         * V5 - gia tri cờ GHÉ ĐÈ cho CHINH lan chay thu nay (mo phong "neu cờ
         * VIP=true thi luong di nhu the nao") - KHONG anh huong cache cờ that
         * tren Data Plane. Key = flagCode, value = gia tri mo phong.
         */
        Map<String, Boolean> flagOverrides
) {
    public EndpointTryRequestDto {
        if (pathVariables == null) pathVariables = Map.of();
        if (queryParams == null) queryParams = Map.of();
        if (flagOverrides == null) flagOverrides = Map.of();
    }
}
