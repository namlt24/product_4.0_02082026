package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.service.EndpointRegistryCache;
import tools.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data Plane: "catch-all" nhan MOI request traffic that (khong phai /api/**,
 * Spring MVC tu uu tien mapping cu the hon truoc mapping "/**" nay), tra cuu
 * trong EndpointRegistryCache theo (method, path), roi giao cho
 * CompositeOrchestratorEngine thuc thi. Day la thay the truc tiep cho viec
 * KrakenD/Gravitee doc krakend.json/policy JSON - o day engine tu route,
 * khong co file config trung gian nao ca.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DynamicDispatcherController {

    private final EndpointRegistryCache registryCache;
    private final CompositeOrchestratorEngine engine;

    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    @RequestMapping("/**")
    public ResponseEntity<?> dispatch(HttpServletRequest request) throws Exception {
        String requestPath = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        PathContainer pathContainer = PathContainer.parsePath(requestPath);

        for (EndpointResponseDto config : registryCache.all()) {
            if (!config.method().name().equals(method.name())) {
                continue;
            }
            PathPattern pattern = patternCache.computeIfAbsent(config.path(), pathPatternParser::parse);
            if (!pattern.matches(pathContainer)) {
                continue;
            }
            Map<String, String> pathVariables = pattern.matchAndExtract(pathContainer).getUriVariables();
            String rawBody = readBody(request);
            JsonNode result = engine.handle(config, pathVariables, request.getParameterMap(), rawBody);
            return ResponseEntity.ok(result);
        }

        log.debug("Khong tim thay EndpointConfig khop voi {} {}", method, requestPath);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "GW-NOT-FOUND", "message", "Khong co endpoint nao khop voi " + method + " " + requestPath));
    }

    private String readBody(HttpServletRequest request) throws Exception {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
