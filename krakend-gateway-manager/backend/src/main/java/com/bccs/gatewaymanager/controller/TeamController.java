package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.TeamCreateRequestDto;
import com.bccs.gatewaymanager.dto.TeamCreatedDto;
import com.bccs.gatewaymanager.dto.TeamDto;
import com.bccs.gatewaymanager.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Control-Plane-only (@Profile) - "Quan ly doi", CHI goi duoc bang
 * platform-admin key (xem ApiKeyAuthFilter, so khop rieng cho tien to
 * "/api/teams"), khong nam duoi CurrentTeamContext.
 */
@RestController
@RequestMapping("/api/teams")
@Profile("control-plane")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    @GetMapping
    public ResponseEntity<List<TeamDto>> list() {
        return ResponseEntity.ok(service.list());
    }

    /** apiKey plaintext chi xuat hien trong response nay - hien 1 lan duy nhat, khong luu lai/hien lai o dau khac (xem TeamCreatedDto). */
    @PostMapping
    public ResponseEntity<TeamCreatedDto> create(@Valid @RequestBody TeamCreateRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @DeleteMapping("/{teamCode}")
    public ResponseEntity<Void> delete(@PathVariable String teamCode) {
        service.delete(teamCode);
        return ResponseEntity.noContent().build();
    }
}
