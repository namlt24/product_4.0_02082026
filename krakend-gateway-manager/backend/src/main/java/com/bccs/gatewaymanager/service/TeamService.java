package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.TeamCreateRequestDto;
import com.bccs.gatewaymanager.dto.TeamCreatedDto;
import com.bccs.gatewaymanager.dto.TeamDto;
import com.bccs.gatewaymanager.entity.Team;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * CRUD bang gwm_team (danh sach doi dung chung Control Plane nay) - chi goi
 * duoc qua platform-admin key (xem ApiKeyAuthFilter/TeamController), KHONG
 * nam duoi CurrentTeamContext (khong thuoc pham vi 1 doi cu the nao).
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class TeamService {

    /** 32 byte = 256 bit ngau nhien, encode Base64 URL-safe khong dau "=" - du manh, khong ky tu can escape khi dat trong header HTTP. */
    private static final int API_KEY_RANDOM_BYTES = 32;

    private final TeamRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(readOnly = true)
    public List<TeamDto> list() {
        return repository.findAllByOrderByTeamNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public TeamCreatedDto create(TeamCreateRequestDto dto) {
        if (repository.existsById(dto.teamCode())) {
            throw new BusinessException("GW-TEAM-001", "Ma doi '" + dto.teamCode() + "' da ton tai.");
        }
        String apiKey = generateApiKey();
        Team saved = repository.save(Team.builder()
                .teamCode(dto.teamCode())
                .teamName(dto.teamName())
                .apiKey(apiKey)
                .build());
        log.info("Da tao doi moi: {} ({})", saved.getTeamCode(), saved.getTeamName());
        return new TeamCreatedDto(saved.getTeamCode(), saved.getTeamName(), saved.getApiKey(), saved.getCreatedAt());
    }

    @Transactional
    public void delete(String teamCode) {
        Team entity = repository.findById(teamCode)
                .orElseThrow(() -> new BusinessException("GW-TEAM-404", "Khong tim thay doi ma=" + teamCode));
        repository.delete(entity);
        log.info("Da xoa doi: {}", teamCode);
        // Co y KHONG xoa/anh huong EndpointConfig/UpstreamService da co cua doi nay -
        // du bi xoa khoi gwm_team, du lieu cau hinh cu van con trong DB (chi khong
        // con ai dang nhap duoc qua key cu de sua/xoa no nua) - xoa cau hinh la
        // hanh dong rieng, co chu dich, khong nen la tac dung phu am tham cua xoa 1
        // dong trong bang doi.
    }

    private String generateApiKey() {
        byte[] randomBytes = new byte[API_KEY_RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private TeamDto toDto(Team e) {
        return new TeamDto(e.getTeamCode(), e.getTeamName(), e.getCreatedAt());
    }
}
