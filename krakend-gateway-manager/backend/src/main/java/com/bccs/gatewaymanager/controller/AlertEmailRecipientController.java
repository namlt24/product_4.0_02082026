package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.entity.GwmAlertEmail;
import com.bccs.gatewaymanager.service.AlertEmailRecipientService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Control-Plane-only - API quan ly nguoi nhan EMAIL canh bao cua doi (bang
 * GWM_ALERT_EMAIL - DDL V4). Nam duoi /api/** nen tu duoc ApiKeyAuthFilter bao
 * ve va scope theo team key - doi chi thao tac tren danh sach cua chinh minh.
 * AlertEmailService doc bang nay khi gui mail canh bao MOI.
 */
@RestController
@RequestMapping("/api/alerts/recipients")
@Profile("control-plane")
@RequiredArgsConstructor
public class AlertEmailRecipientController {

    private final AlertEmailRecipientService recipientService;

    @GetMapping
    public ResponseEntity<List<GwmAlertEmail>> list() {
        return ResponseEntity.ok(recipientService.list());
    }

    @PostMapping
    public ResponseEntity<GwmAlertEmail> add(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(recipientService.add(body.get("email")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        recipientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
