package com.bccs.gatewaymanager.dto;

import java.util.List;

/** Toan bo so do phu thuoc giua cac endpoint (endpoint nao goi nguoc vao endpoint nao qua chinh KrakenD). */
public record DependencyGraphDto(
        List<GraphNodeDto> nodes,
        List<GraphEdgeDto> edges,
        /** Canh bao vong lap (cycle) - se gay goi vo han khi chay that, PHAI sua truoc khi Deploy. */
        List<String> cycleWarnings
) {
}
