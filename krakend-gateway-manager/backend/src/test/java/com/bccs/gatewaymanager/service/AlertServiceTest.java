package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.AlertEventDto;
import com.bccs.gatewaymanager.entity.AlertSeverity;
import com.bccs.gatewaymanager.entity.AlertStatus;
import com.bccs.gatewaymanager.entity.AlertTargetType;
import com.bccs.gatewaymanager.entity.AlertType;
import com.bccs.gatewaymanager.entity.GwmAlert;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.GwmAlertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test luong ingest alert cua CP: dedup (moi/touch/resolve), race
 * DataIntegrityViolationException, va HOOK EMAIL (chi mail khi MOI - khong
 * mail khi touch/resolve). TransactionTemplate duoc mock de thuc thi ngay
 * callback (khong co Spring context).
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private GwmAlertRepository alertRepository;
    @Mock
    private AlertEmailService alertEmailService;
    @Mock
    private TransactionTemplate transactionTemplate;

    private AlertService service;

    @BeforeEach
    void setUp() {
        // executeWithoutResult (default method tren TransactionOperations) goi
        // execute(callback) - stub de chay thang callback ngay lap tuc (khong co Spring context).
        lenient().doAnswer(inv -> {
            inv.getArgument(0, java.util.function.Consumer.class).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        service = new AlertService(alertRepository, transactionTemplate, alertEmailService);
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private AlertEventDto event(AlertType type, String targetName, boolean resolved) {
        return new AlertEventDto(type, AlertTargetType.UPSTREAM, targetName, "up-1", resolved,
                AlertSeverity.CRITICAL, "Loi " + type, "chi tiet", Instant.now());
    }

    private GwmAlert activeAlert(String teamCode, AlertType type, String targetName) {
        return GwmAlert.builder()
                .teamCode(teamCode)
                .alertType(type)
                .severity(AlertSeverity.CRITICAL)
                .status(AlertStatus.ACTIVE)
                .targetType(AlertTargetType.UPSTREAM)
                .targetName(targetName)
                .title("Loi " + type)
                .detail("chi tiet")
                .firstSeenAt(Instant.now())
                .lastSeenAt(Instant.now())
                .occurrenceCount(1)
                .build();
    }

    private void team(String teamCode) {
        CurrentTeamContext.set(teamCode);
    }

    @Test
    void ingest_vanDeMoi_luuRowMoi_vaGuiEmail() {
        team("TEAM1");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.empty());
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        int accepted = service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", false)));

        assertThat(accepted).isEqualTo(1);
        ArgumentCaptor<GwmAlert> captor = ArgumentCaptor.forClass(GwmAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AlertStatus.ACTIVE);
        assertThat(captor.getValue().getOccurrenceCount()).isEqualTo(1);
        // EMAIL CHI GUI O DAY - diem "Canh bao MOI".
        verify(alertEmailService).notifyNewAlert(any(GwmAlert.class));
    }

    @Test
    void ingest_vanDeDaCo_touch_khongGuiEmail() {
        team("TEAM1");
        GwmAlert existing = activeAlert("TEAM1", AlertType.CIRCUIT_OPEN, "up-1");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.of(existing));
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", false)));

        ArgumentCaptor<GwmAlert> captor = ArgumentCaptor.forClass(GwmAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getOccurrenceCount()).isEqualTo(2);
        assertThat(captor.getValue().getStatus()).isEqualTo(AlertStatus.ACTIVE); // khong doi trang thai
        // TOUCH KHONG MAIL - chong spam re-emit moi chu ky 30s.
        verify(alertEmailService, never()).notifyNewAlert(any());
    }

    @Test
    void ingest_eventResolved_resolveRow_khongGuiEmail() {
        team("TEAM1");
        GwmAlert existing = activeAlert("TEAM1", AlertType.CIRCUIT_OPEN, "up-1");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.of(existing));
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", true)));

        ArgumentCaptor<GwmAlert> captor = ArgumentCaptor.forClass(GwmAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(captor.getValue().getResolvedReason()).isEqualTo("DP_RESOLVED");
        verify(alertEmailService, never()).notifyNewAlert(any());
    }

    @Test
    void ingest_raceDataIntegrityViolation_coiNhuTouch_khongGuiEmail() {
        team("TEAM1");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.empty());
        when(alertRepository.save(any(GwmAlert.class)))
                .thenThrow(new DataIntegrityViolationException("unique index V3"));

        int accepted = service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", false)));

        assertThat(accepted).isEqualTo(1); // khong fail batch - coi nhu touch
        verify(alertEmailService, never()).notifyNewAlert(any());
    }

    @Test
    void ingest_emailServiceThrow_vanAcceptedTiepDien() {
        team("TEAM1");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.empty());
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().doThrow(new RuntimeException("queue gone")).when(alertEmailService).notifyNewAlert(any());

        int accepted = service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", false)));

        // Lop 2 an toan: luong ingest KHONG BAO GIO chet vi email.
        assertThat(accepted).isEqualTo(1);
    }

    @Test
    void ingest_dungTeamKeyKhongLayTuBody() {
        team("TEAM9");
        when(alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                any(), any(), any(), any(), anyList())).thenReturn(Optional.empty());
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(List.of(event(AlertType.CIRCUIT_OPEN, "up-1", false)));

        ArgumentCaptor<GwmAlert> captor = ArgumentCaptor.forClass(GwmAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getTeamCode()).isEqualTo("TEAM9");
    }

    @Test
    void get_cuaDoiKhac_khongXemDuoc() {
        team("TEAM1");
        when(alertRepository.findById("a1")).thenReturn(Optional.of(activeAlert("TEAM2", AlertType.CIRCUIT_OPEN, "up-1")));

        assertThatThrownBy(() -> service.get("a1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-NOT-FOUND"));
    }

    @Test
    void acknowledge_rowResolved_biTuChoi() {
        team("TEAM1");
        GwmAlert resolved = activeAlert("TEAM1", AlertType.CIRCUIT_OPEN, "up-1");
        resolved.setStatus(AlertStatus.RESOLVED);
        when(alertRepository.findById("a1")).thenReturn(Optional.of(resolved));

        assertThatThrownBy(() -> service.acknowledge("a1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("GW-ALERT-STATE"));
    }

    @Test
    void resolve_manual_datResolvedReasonMANUAL() {
        team("TEAM1");
        GwmAlert active = activeAlert("TEAM1", AlertType.CIRCUIT_OPEN, "up-1");
        when(alertRepository.findById("a1")).thenReturn(Optional.of(active));
        when(alertRepository.save(any(GwmAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.resolve("a1");

        assertThat(result.status()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(result.resolvedReason()).isEqualTo("MANUAL");
        verify(alertEmailService, never()).notifyNewAlert(any());
    }
}
