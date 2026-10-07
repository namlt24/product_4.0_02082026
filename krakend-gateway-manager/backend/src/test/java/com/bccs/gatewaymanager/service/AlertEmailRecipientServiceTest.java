package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.entity.GwmAlertEmail;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.GwmAlertEmailRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test quan ly nguoi nhan email theo doi: validate, duplicate, max 10, va doc
 * lap (doi khong the thay/xoa dia chi cua doi khac du co id).
 */
@ExtendWith(MockitoExtension.class)
class AlertEmailRecipientServiceTest {

    @Mock
    private GwmAlertEmailRepository repository;

    private AlertEmailRecipientService service;

    @BeforeEach
    void setUp() {
        CurrentTeamContext.set("TEAM1");
        service = new AlertEmailRecipientService(repository);
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private GwmAlertEmail row(String id, String teamCode, String email) {
        return GwmAlertEmail.builder().id(id).teamCode(teamCode).email(email).build();
    }

    @Test
    void add_emailHopLe_luuVoiTeamCuaNguoiGoi() {
        when(repository.existsByTeamCodeAndEmailIgnoreCase("TEAM1", "lead@viettel.com.vn")).thenReturn(false);
        when(repository.findByTeamCode("TEAM1")).thenReturn(List.of());
        when(repository.save(any(GwmAlertEmail.class))).thenAnswer(inv -> inv.getArgument(0));

        GwmAlertEmail result = service.add(" lead@viettel.com.vn ");

        ArgumentCaptor<GwmAlertEmail> captor = ArgumentCaptor.forClass(GwmAlertEmail.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTeamCode()).isEqualTo("TEAM1"); // tu context, KHONG tu input
        assertThat(captor.getValue().getEmail()).isEqualTo("lead@viettel.com.vn"); // da trim
        assertThat(result).isSameAs(captor.getValue());
    }

    @Test
    void add_emailSaiDinhDang_bi400() {
        assertThatThrownBy(() -> service.add("sai-dinh-dang"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-INVALID"));
        verify(repository, never()).save(any());
    }

    @Test
    void add_emailNull_bi400() {
        assertThatThrownBy(() -> service.add(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-INVALID"));
    }

    @Test
    void add_emailDaTonTai_biTrung() {
        when(repository.existsByTeamCodeAndEmailIgnoreCase("TEAM1", "lead@viettel.com.vn")).thenReturn(true);

        assertThatThrownBy(() -> service.add("lead@viettel.com.vn"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-DUPLICATE"));
    }

    @Test
    void add_du10NguoiNhan_biTuChoi() {
        List<GwmAlertEmail> dayBoi = java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(i -> row("id" + i, "TEAM1", "user" + i + "@viettel.com.vn"))
                .toList();
        when(repository.findByTeamCode("TEAM1")).thenReturn(dayBoi);

        assertThatThrownBy(() -> service.add("them@viettel.com.vn"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-MAX"));
        verify(repository, never()).save(any(GwmAlertEmail.class));
    }

    @Test
    void list_chiTraVeCuaDoiHienTai() {
        when(repository.findByTeamCode("TEAM1")).thenReturn(
                List.of(row("r1", "TEAM1", "a@viettel.com.vn")));

        List<GwmAlertEmail> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("a@viettel.com.vn");
        verify(repository).findByTeamCode("TEAM1"); // luon theo team key
    }

    @Test
    void delete_cuaDoiKhac_biTuChoi() {
        when(repository.findById("r1")).thenReturn(Optional.of(row("r1", "TEAM2", "b@viettel.com.vn")));

        assertThatThrownBy(() -> service.delete("r1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-NOT-FOUND"));
        verify(repository, never()).delete(any(GwmAlertEmail.class));
    }

    @Test
    void delete_cuaDoiMinh_xoaThanhCong() {
        GwmAlertEmail own = row("r1", "TEAM1", "a@viettel.com.vn");
        when(repository.findById("r1")).thenReturn(Optional.of(own));

        service.delete("r1");

        verify(repository).delete(own);
    }

    @Test
    void delete_idKhongTonTai_biNotFound() {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("nope"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-EMAIL-NOT-FOUND"));
    }
}
