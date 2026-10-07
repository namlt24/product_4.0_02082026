package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.AlertEventBatchDto;
import com.bccs.gatewaymanager.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint NOI BO nhan batch event canh bao tu Data Plane (AlertEventPublisher
 * push moi 5s). Khong co trong plan API cong khai cho UI - la "duong day" DP->CP.
 *
 * AUTH: nam duoi "/api/*" nen tu dong duoc ApiKeyAuthFilter bao ve - DP dung
 * dung team key no da dung cho RemoteConfigSyncService (header
 * X-Gateway-Admin-Key), filter tu set CurrentTeamContext theo team cua key do,
 * AlertService lay team_code tu day. DP KHONG tu do khai bao team_code trong
 * body - nhan key cua doi nao thi alert thuoc doi do.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/alerts")
@Profile("control-plane")
@RequiredArgsConstructor
public class InternalAlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> ingest(@RequestBody AlertEventBatchDto batch) {
        int count = (batch == null || batch.events() == null) ? 0 : alertService.ingest(batch.events());
        return ResponseEntity.ok(Map.of("accepted", count));
    }
}
