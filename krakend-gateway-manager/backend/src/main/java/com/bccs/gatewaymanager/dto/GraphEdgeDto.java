package com.bccs.gatewaymanager.dto;

/** 1 canh phu thuoc: endpoint "fromEndpointId" co 1 step (thu tu viaStepOrder) goi nguoc vao endpoint "toEndpointId". */
public record GraphEdgeDto(
        String fromEndpointId,
        String toEndpointId,
        int viaStepOrder
) {
}
