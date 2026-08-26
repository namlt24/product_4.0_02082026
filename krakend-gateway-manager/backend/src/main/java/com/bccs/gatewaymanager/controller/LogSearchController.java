package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.audit.HopAuditEvent;
import com.bccs.gatewaymanager.audit.LogSearchResultDto;
import com.bccs.gatewaymanager.audit.LogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * "Tra cuu Log" - doc RequestAuditEvent/HopAuditEvent tu Elasticsearch (xem
 * LogSearchService). Nam duoi /api/** nen tu dong duoc ApiKeyAuthFilter bao ve
 * y het cac API Control Plane khac - khong can filter/auth rieng.
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogSearchController {

    private final LogSearchService service;

    @GetMapping("/requests")
    public ResponseEntity<LogSearchResultDto> searchRequests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String endpointPath,
            @RequestParam(required = false) String bodyContains,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchRequests(from, to, status, endpointPath, bodyContains, page, size));
    }

    @GetMapping("/requests/{requestId}/hops")
    public ResponseEntity<List<HopAuditEvent>> getHops(@PathVariable String requestId) {
        return ResponseEntity.ok(service.getHops(requestId));
    }
}
