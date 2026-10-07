package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.entity.GwmFlag;
import com.bccs.gatewaymanager.service.FlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Control-Plane-only - API quan ly CỜ NGHIỆP VỶ cua doi (bang GWM_FLAG -
 * DDL V5). Nam duoi /api/** nen tu duoc ApiKeyAuthFilter bao ve va scope theo
 * team key - doi chi thao tac tren cờ cua chinh minh. Gia tri cờ duoc DP sync
 * qua /api/config/export (ConfigExportDto.flags).
 */
@RestController
@RequestMapping("/api/flags")
@Profile("control-plane")
@RequiredArgsConstructor
public class FlagController {

    private final FlagService flagService;

    @GetMapping
    public ResponseEntity<List<GwmFlag>> list() {
        return ResponseEntity.ok(flagService.list());
    }

    @PostMapping
    public ResponseEntity<GwmFlag> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(flagService.create(
                (String) body.get("flagCode"),
                (String) body.get("displayName"),
                (String) body.get("description"),
                Boolean.TRUE.equals(body.get("enabled"))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GwmFlag> update(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(flagService.update(id, body.get("displayName"), body.get("description")));
    }

    /** Toggle gia tri cờ (BAT/TAT) - thao tac chinh khi chay that. */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<GwmFlag> toggle(@PathVariable String id) {
        return ResponseEntity.ok(flagService.toggle(id));
    }

    /** Lich su doi gia tri cờ (moi nhat truoc). */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<Map<String, Object>>> history(@PathVariable String id) {
        return ResponseEntity.ok(flagService.history(id).stream()
                .<Map<String, Object>>map(h -> Map.of(
                        "id", h.getId(),
                        "oldValue", h.getOldValue() == null ? null : h.getOldValue(),
                        "newValue", h.getNewValue(),
                        "source", h.getSource(),
                        "changedAt", h.getChangedAt() == null ? "" : h.getChangedAt().toString()))
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        flagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
