package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.TeamCreateRequestDto;
import com.bccs.gatewaymanager.dto.TeamCreatedDto;
import com.bccs.gatewaymanager.entity.Team;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository repository;

    private TeamService service;

    @BeforeEach
    void setUp() {
        service = new TeamService(repository);
    }

    @Test
    void create_teamCodeDaTonTai_bi400() {
        when(repository.existsById("VCOM")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new TeamCreateRequestDto("VCOM", "Doi VCOM")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-TEAM-001");

        verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void create_thanhCong_sinhApiKeyNgauNhien_traVeDungTeamCode() {
        when(repository.existsById("VCOM")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TeamCreatedDto result = service.create(new TeamCreateRequestDto("VCOM", "Doi VCOM"));

        assertThat(result.teamCode()).isEqualTo("VCOM");
        assertThat(result.teamName()).isEqualTo("Doi VCOM");
        assertThat(result.apiKey()).isNotBlank();

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getApiKey()).isEqualTo(result.apiKey());
        assertThat(captor.getValue().getTeamCode()).isEqualTo("VCOM");
    }

    @Test
    void create_apiKeySinhRa_giaiMaDuocBase64UrlSafeVaDuDai() {
        // 32 byte ngau nhien encode Base64 URL-safe khong dau "=" -> 43 ky tu (ceil(32*4/3)
        // roi bo padding) - xac nhan dung DO DAI/BANG MA, khong phai 1 chuoi ngan/yeu.
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TeamCreatedDto result = service.create(new TeamCreateRequestDto("X", "Doi X"));

        assertThat(result.apiKey()).doesNotContain("=").hasSizeGreaterThanOrEqualTo(40);
        // Khong throw = giai ma Base64 URL-safe hop le.
        Base64.getUrlDecoder().decode(result.apiKey());
    }

    @Test
    void create_goiNhieuLan_apiKeyKhongTrungNhau() {
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            keys.add(service.create(new TeamCreateRequestDto("T" + i, "Doi " + i)).apiKey());
        }

        assertThat(keys).hasSize(20);
    }

    @Test
    void delete_khongTonTai_bi404() {
        when(repository.findById("MISSING")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.delete("MISSING"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-TEAM-404");
    }

    @Test
    void delete_thanhCong() {
        Team entity = Team.builder().teamCode("VCOM").teamName("Doi VCOM").apiKey("k").build();
        when(repository.findById("VCOM")).thenReturn(java.util.Optional.of(entity));

        service.delete("VCOM");

        verify(repository).delete(entity);
    }
}
