package com.bccs.gatewaymanager.dto;

import java.util.List;

/** Batch event canh bao DP push ve CP trong 1 request (flush dinh ky 5s). */
public record AlertEventBatchDto(
        List<AlertEventDto> events
) {
}
