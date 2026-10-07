package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.CurrentTeamDto;
import com.bccs.gatewaymanager.entity.Team;
import com.bccs.gatewaymanager.repository.TeamRepository;
import com.bccs.gatewaymanager.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Control-Plane-only (@Profile) - "Toi la doi nao": tra thong tin doi tuong
 * ung voi API key trong header cua request hien tai. ApiKeyAuthFilter da tra
 * cuu key -> teamCode va set CurrentTeamContext truoc khi controller nay chay;
 * doi ten team doc them 1 lan tu gwm_team de lay teamName.
 *
 * KHONG nam duoi "/api/teams" (do chi nhan platform-admin key) - day la
 * endpoint cho TEAM KEY thuong, muc dich chinh de UI hien badge team.
 */
@RestController
@RequestMapping("/api/me")
@Profile("control-plane")
@RequiredArgsConstructor
public class MeController {

    private final TeamRepository teamRepository;

    @GetMapping
    public ResponseEntity<CurrentTeamDto> me() {
        String teamCode = CurrentTeamContext.require();
        Team team = teamRepository.findById(teamCode)
                .orElseThrow(() -> new BusinessException("GW-TEAM-404", "Khong tim thay doi ma=" + teamCode));
        return ResponseEntity.ok(new CurrentTeamDto(team.getTeamCode(), team.getTeamName()));
    }
}
