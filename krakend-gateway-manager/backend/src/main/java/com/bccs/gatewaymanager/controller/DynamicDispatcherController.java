package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.audit.AuditLogService;
import com.bccs.gatewaymanager.audit.BodyTruncator;
import com.bccs.gatewaymanager.audit.RequestAuditEvent;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.EndpointRegistryCache;
import tools.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.BufferedReader;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
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
    private final AuditLogService auditLogService;

    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    @RequestMapping("/**")
    public ResponseEntity<?> dispatch(HttpServletRequest request) throws Exception {
        // requestId: dat vao MDC de UpstreamHttpExecutor (2-3 tang goi sau, CUNG
        // thread vi engine chay dong bo hoan toan - khong co handoff bat dong bo
        // nao) doc lai duoc ma khong can sua chu ky nhieu ham trung gian de truyen
        // tham so nay xuyen suot.
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        long startNanos = System.nanoTime();
        String requestPath = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            // Buoc 1: tra O(1) qua HashMap cho path "tinh" (khong co {param}) - da
            // so endpoint thuc te roi vao nhom nay (xem EndpointRegistryCache.findExact()).
            // Truoc day dispatch() luon quet TUYEN TINH toan bo danh sach endpoint
            // (kem PathPattern.matches() cho tung phan tu) du path khong he co
            // {param} - O(n)/request, thanh diem nghen that su khi so endpoint len
            // toi hang nghin. Khong can PathContainer.parsePath() o nhanh nay.
            EndpointResponseDto exact = registryCache.findExact(method.name(), requestPath);
            if (exact != null) {
                return execute(exact, Map.of(), request, requestId, method, requestPath, startNanos);
            }

            // Buoc 2: fallback quet tuan tu, nhung CHI tren nhom co {param} trong
            // path (registryCache.patternEndpoints() - nho hon han toan bo danh
            // sach endpoint trong thuc te).
            PathContainer pathContainer = PathContainer.parsePath(requestPath);
            for (EndpointResponseDto config : registryCache.patternEndpoints()) {
                if (!config.method().name().equals(method.name())) {
                    continue;
                }
                PathPattern pattern = patternCache.computeIfAbsent(config.path(), pathPatternParser::parse);
                if (!pattern.matches(pathContainer)) {
                    continue;
                }
                Map<String, String> pathVariables = pattern.matchAndExtract(pathContainer).getUriVariables();
                return execute(config, pathVariables, request, requestId, method, requestPath, startNanos);
            }

            log.debug("Khong tim thay EndpointConfig khop voi {} {}", method, requestPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "GW-NOT-FOUND", "message", "Khong co endpoint nao khop voi " + method + " " + requestPath));
        } finally {
            MDC.remove("requestId");
        }
    }

    /** Thuc thi 1 EndpointConfig da khop (qua O(1) hoac fallback pattern) + ghi audit ca 2 nhanh thanh cong/loi. */
    private ResponseEntity<?> execute(EndpointResponseDto config, Map<String, String> pathVariables, HttpServletRequest request,
                                       String requestId, HttpMethod method, String requestPath, long startNanos) throws Exception {
        String rawBody = readBody(request);
        try {
            JsonNode result = engine.handle(config, pathVariables, request.getParameterMap(), rawBody);
            recordAudit(requestId, config, method, requestPath, rawBody, startNanos, "SUCCESS", 200, null, null);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            recordAudit(requestId, config, method, requestPath, rawBody, startNanos, "ERROR",
                    resolveHttpStatus(e), resolveErrorCode(e), e.getMessage());
            throw e;
        }
    }

    private String readBody(HttpServletRequest request) throws Exception {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    /**
     * Ghi 1 RequestAuditEvent (xem AuditLogService - fail-open, KHONG BAO GIO
     * duoc throw/chan response that cho client). httpStatus/errorCode o nhanh
     * loi la "best effort" (khong lap lai toan bo bang map ma loi cua
     * GlobalExceptionHandler - do van la nguon su that duy nhat cho response
     * THAT tra ve client, audit log chi la 1 duong doc lap phuc vu tra cuu).
     */
    private void recordAudit(String requestId, EndpointResponseDto config, HttpMethod method, String requestPath,
                              String rawBody, long startNanos, String status, Integer httpStatus,
                              String errorCode, String errorMessage) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        BodyTruncator.Result bodyResult = BodyTruncator.truncate(rawBody);
        RequestAuditEvent event = new RequestAuditEvent(
                requestId, Instant.now(), config.id(), config.name(), method.name(), requestPath,
                status, httpStatus, errorCode, errorMessage, durationMs,
                bodyResult.body(), bodyResult.truncated(), currentApmTraceIdOrNull());
        auditLogService.recordRequest(event);
    }

    private Integer resolveHttpStatus(RuntimeException e) {
        if (e instanceof com.bccs.gatewaymanager.engine.UpstreamHttpExecutor.UpstreamHttpErrorException httpError) {
            return httpError.httpStatus();
        }
        return null;
    }

    private String resolveErrorCode(RuntimeException e) {
        if (e instanceof BusinessException be) {
            return be.getErrorCode();
        }
        return e.getClass().getSimpleName();
    }

    /**
     * trace.id cua Elastic APM (neu Java agent co gan vao JVM qua -javaagent, xem
     * Dockerfile) de link audit log sang APM trace tuong ung. co.elastic.apm:apm-agent-api
     * (khai bao trong pom.xml) duoc Elastic thiet ke AN TOAN khi KHONG co agent
     * that su gan vao - ElasticApm.currentTransaction() tra ve mot no-op object,
     * getTraceId() tra ve chuoi rong, khong throw.
     */
    private String currentApmTraceIdOrNull() {
        String traceId = co.elastic.apm.api.ElasticApm.currentTransaction().getTraceId();
        return (traceId == null || traceId.isBlank()) ? null : traceId;
    }
}
