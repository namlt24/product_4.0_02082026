package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Thu ngay" (P1) - goi THANG CompositeOrchestratorEngine trong-process voi
 * du lieu gia lap do admin nhap tren UI, dung Y HET engine ma Data Plane that
 * dung (DynamicDispatcherController) - khong phai 1 ban gia lap rieng co the
 * lech hanh vi so voi luc chay that.
 *
 * Chay qua Control Plane (/api/**) nen KHONG bi RateLimitFilter gioi han (chi
 * ap cho Data Plane) va KHONG can CORS rieng (frontend da goi /api/** qua
 * nginx proxy san co). Loi (BusinessException/UpstreamHttpErrorException/
 * UpstreamTimeoutException/CallNotPermittedException...) duoc de nguyen
 * throw ra - GlobalExceptionHandler format dung y het response 1 client that
 * se thay, giup "Thu ngay" phan anh dung 100% hanh vi that.
 */
@Service
@RequiredArgsConstructor
public class EndpointTryService {

    private final EndpointService endpointService;
    private final CompositeOrchestratorEngine engine;

    public JsonNode tryCall(String endpointId, Map<String, String> pathVariables,
                             Map<String, String> queryParams, String rawBody) {
        EndpointResponseDto config = endpointService.get(endpointId);
        Map<String, String[]> queryParamsArr = queryParams.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new String[]{e.getValue()}));
        return engine.handle(config, pathVariables, queryParamsArr, rawBody);
    }
}
