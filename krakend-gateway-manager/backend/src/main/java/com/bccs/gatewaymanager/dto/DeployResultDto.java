package com.bccs.gatewaymanager.dto;

import java.util.List;

/** Ket qua goi POST /api/config/deploy. */
public record DeployResultDto(
        boolean success,
        String message,
        List<String> warnings
) {
}
