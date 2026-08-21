package com.bccs.gatewaymanager.dto;

import java.util.List;

/**
 * Thong tin ve chinh KrakenD gateway - dung cho FE de tu dong dien host khi
 * nguoi dung chon "Goi mot endpoint KrakenD khac" trong Endpoint Picker,
 * thay vi phai tu go tay "http://localhost:8080".
 */
public record GatewayInfoDto(
        int port,
        String selfBaseUrl,
        List<String> selfHostAliases
) {
}
