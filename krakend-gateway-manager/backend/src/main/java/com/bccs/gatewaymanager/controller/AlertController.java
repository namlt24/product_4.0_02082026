package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.AlertPageDto;
import com.bccs.gatewaymanager.dto.AlertResponseDto;
import com.bccs.gatewaymanager.dto.AlertSummaryDto;
import com.bccs.gatewaymanager.entity.AlertStatus;
import com.bccs.gatewaymanager.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Control-Plane-only - API trang "Canh bao" cho UI (/alerts). Nam duoi /api/**
 * nen tu dong duoc ApiKeyAuthFilter bao ve va scope theo team cua key (qua
 * CurrentTeamContext trong AlertService) - moi doi CHI thay canh bao cua minh.
 */
@RestController
@RequestMapping("/api/alerts")
@Profile("control-plane")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<AlertPageDto> search(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String targetName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertService.search(status, targetName, page, size));
    }

    @GetMapping("/summary")
    public ResponseEntity<AlertSummaryDto> summary() {
        return ResponseEntity.ok(alertService.summary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(alertService.get(id));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponseDto> acknowledge(@PathVariable String id) {
        return ResponseEntity.ok(alertService.acknowledge(id));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<AlertResponseDto> resolve(@PathVariable String id) {
        return ResponseEntity.ok(alertService.resolve(id));
    }
}
