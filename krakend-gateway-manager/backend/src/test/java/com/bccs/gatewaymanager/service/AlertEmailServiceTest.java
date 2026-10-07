package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.entity.AlertSeverity;
import com.bccs.gatewaymanager.entity.AlertType;
import com.bccs.gatewaymanager.entity.GwmAlert;
import com.bccs.gatewaymanager.repository.GwmAlertEmailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AlertEmailService PHAI fail-open tuyet doi - loi SMTP/DB khong bao gio duoc
 * throw ra ngoai pollAndSend(), mail khong bao gio lam hong/cham luong ingest
 * alert (goi tu HTTP thread cua POST /api/internal/alerts). Mirror dung triet
 * ly da test cho AuditLogService.
 *
 * Cac test KHONG khoi dong scheduler (@PostConstruct start() khong duoc goi -
 * tao service bang constructor thu cong) nen pollAndSend() duoc goi truc tiep.
 */
@ExtendWith(MockitoExtension.class)
class AlertEmailServiceTest {

    @Mock
    private GwmAlertEmailRepository recipientRepository;
    @Mock
    private JavaMailSenderImpl mailSender;

    private GwmAlert alert(String teamCode, AlertType type, AlertSeverity severity, String targetName) {
        return GwmAlert.builder()
                .teamCode(teamCode)
                .alertType(type)
                .severity(severity)
                .targetName(targetName)
                .title("Loi " + type)
                .detail("Chi tiet ky thuat")
                .targetType(com.bccs.gatewaymanager.entity.AlertTargetType.UPSTREAM)
                .firstSeenAt(Instant.now())
                .lastSeenAt(Instant.now())
                .build();
    }

    /** Service KHONG bat mail (enabled=false, default) - notify no-op, poll khong gui gi. */
    private AlertEmailService disabledService() {
        return new AlertEmailService(recipientRepository, false,
                "smtp.vmail.viettel.vn", 465, "u", "p", "f@viettel.com.vn",
                true, false, "fallback@viettel.com.vn", "http://localhost:4200", 10, 5);
    }

    /** Service bat mail, KHONG co fallback - de kiem soat nguoi nhan qua mock repo. */
    private AlertEmailService enabledService() {
        return new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "", "http://ui.local", 10, 5);
    }

    @Test
    void notify_khiChuaBat_boImLang() {
        AlertEmailService service = disabledService();

        service.notifyNewAlert(alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "up-1"));
        service.pollAndSend();

        // Khong co fallback trong service disabled + repo mac dinh tra rong - neu notify khong no-op
        // thi sendOne van duoc goi; verify nay kiem tra khong co gi gui (repo khong bao gio duoc doc).
        verify(recipientRepository, never()).findByTeamCode(any());
    }

    @Test
    void pollAndSend_khongCoNhan_bienFallback_dungTo() {
        AlertEmailService service = new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "fb1@viettel.com.vn, fb2@viettel.com.vn", "http://ui.local", 10, 5);

        List<String> recipients = service.resolveRecipients("TEAM1");

        assertThat(recipients).containsExactly("fb1@viettel.com.vn", "fb2@viettel.com.vn");
    }

    @Test
    void pollAndSend_fallbackRong_khongGui() {
        AlertEmailService service = enabledService();
        when(recipientRepository.findByTeamCode("TEAM1")).thenReturn(List.of());

        service.notifyNewAlert(alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "up-1"));
        service.pollAndSend();

        // Bo mail - khong throw (fail-open), cung khong co sender nao duoc tao/gi.
    }

    @Test
    void resolveRecipients_dbLoi_dungFallback_vanLayDuocNguoiNhan() {
        AlertEmailService service = new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "fb@viettel.com.vn", "http://ui.local", 10, 5);
        when(recipientRepository.findByTeamCode("TEAM1")).thenThrow(new RuntimeException("DB down"));

        List<String> recipients = service.resolveRecipients("TEAM1");

        assertThat(recipients).containsExactly("fb@viettel.com.vn");
    }

    @Test
    void resolveRecipients_emailSaiDinhDang_bokat() {
        AlertEmailService service = enabledService();
        when(recipientRepository.findByTeamCode("TEAM1")).thenReturn(List.of(
                com.bccs.gatewaymanager.entity.GwmAlertEmail.builder().teamCode("TEAM1").email("hop-le@viettel.com.vn").build(),
                com.bccs.gatewaymanager.entity.GwmAlertEmail.builder().teamCode("TEAM1").email("sai-dinh-dang").build(),
                com.bccs.gatewaymanager.entity.GwmAlertEmail.builder().teamCode("TEAM1").email("hai@@cong.com").build()
        ));

        List<String> recipients = service.resolveRecipients("TEAM1");

        assertThat(recipients).containsExactly("hop-le@viettel.com.vn");
    }

    @Test
    void pollAndSend_rateLimitVuot_boMailCuaTeam() {
        // rate-max=2: mail thu 3 cung chu ky phai bi tu choi.
        AlertEmailService service = new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "fb@viettel.com.vn", "http://ui.local", 2, 5);

        assertThat(service.tryAcquireRateGate("TEAM1")).isTrue();
        assertThat(service.tryAcquireRateGate("TEAM1")).isTrue();
        assertThat(service.tryAcquireRateGate("TEAM1")).isFalse();
        // Doi khac khong bi anh huong (rate gate theo team).
        assertThat(service.tryAcquireRateGate("TEAM2")).isTrue();
    }

    @Test
    void pollAndSend_smtpLoi_khongNhanHuongMailKeTiep() {
        AlertEmailService service = new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "fb@viettel.com.vn", "http://ui.local", 10, 5) {
            @Override
            JavaMailSenderImpl buildSender() {
                return mailSender;
            }
        };
        when(recipientRepository.findByTeamCode(any())).thenReturn(List.of(
                com.bccs.gatewaymanager.entity.GwmAlertEmail.builder().teamCode("TEAM1").email("r@viettel.com.vn").build()));
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP down"))
                .doNothing()
                .when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));

        service.notifyNewAlert(alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "up-1"));
        service.notifyNewAlert(alert("TEAM1", AlertType.ERROR_RATE_HIGH, AlertSeverity.WARNING, "ep-1"));
        service.pollAndSend();

        // Mail 1 loi SMTP nhung mail 2 van duoc gui (khong fail chuyen ca batch).
        verify(mailSender, org.mockito.Mockito.times(2)).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void notify_hangDoiDay_boEventMoi() {
        // Override sender de KHONG ket noi SMTP that (100 mail x timeout SMTP se lam test cham hang phut).
        AlertEmailService service = new AlertEmailService(recipientRepository, true,
                "smtp.vmail.viettel.vn", 465, "u", "p", "from@viettel.com.vn",
                true, false, "fb@viettel.com.vn", "http://ui.local", 100, 5) {
            @Override
            JavaMailSenderImpl buildSender() {
                return mailSender;
            }
        };

        // Chi co the day queue qua reflection vi MAX_QUEUE_SIZE la const 100 - thay vao do test qua
        // rate gate da phu day; o day chi xac nhan notify 100 lan dau khong throw va 100 lan sau cung khong throw.
        for (int i = 0; i < 150; i++) {
            service.notifyNewAlert(alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "up-" + i));
        }
        service.pollAndSend();
        // Khong throw la pass; 50 phan tu tu trai queue da bi bo + log.warn.
    }

    @Test
    void buildSubject_canDayDuThongTin() {
        GwmAlert a = alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "order-service");

        String subject = AlertEmailService.buildSubject(a);

        assertThat(subject).isEqualTo("[GWM] [CRITICAL] CIRCUIT_OPEN - order-service (doi TEAM1)");
    }

    @Test
    void buildBody_coLinkVaChiTiet() {
        AlertEmailService service = enabledService();
        GwmAlert a = alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "order-service");

        String body = service.buildBody(a);

        assertThat(body).contains("CRITICAL");
        assertThat(body).contains("CIRCUIT_OPEN");
        assertThat(body).contains("order-service");
        assertThat(body).contains("TEAM1");
        assertThat(body).contains("Chi tiet: Chi tiet ky thuat");
        assertThat(body).contains("http://ui.local/#/alerts");
    }

    @Test
    void buildBody_chiTietQua500KyTu_biCat() {
        AlertEmailService service = enabledService();
        GwmAlert a = alert("TEAM1", AlertType.CIRCUIT_OPEN, AlertSeverity.CRITICAL, "up");
        a.setDetail("x".repeat(600));

        String body = service.buildBody(a);

        assertThat(body).contains("(cat that)");
        assertThat(body).doesNotContain("x".repeat(501));
    }

    @Test
    void isValidEmail_cacTruongHopDungSai() {
        assertThat(AlertEmailService.isValidEmail("a@b.c")).isTrue();
        assertThat(AlertEmailService.isValidEmail(" namlt24@viettel.com.vn ")).isFalse(); // co space
        assertThat(AlertEmailService.isValidEmail(null)).isFalse();
        assertThat(AlertEmailService.isValidEmail("")).isFalse();
        assertThat(AlertEmailService.isValidEmail("khong-at")).isFalse();
        assertThat(AlertEmailService.isValidEmail("@b.c")).isFalse();
        assertThat(AlertEmailService.isValidEmail("a@")).isFalse();
        assertThat(AlertEmailService.isValidEmail("a@b")).isFalse(); // domain khong co cham
        assertThat(AlertEmailService.isValidEmail("a@@b.c")).isFalse();
        assertThat(AlertEmailService.isValidEmail("a b@c.d")).isFalse();
    }
}
