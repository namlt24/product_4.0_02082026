package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.entity.GwmAlert;
import com.bccs.gatewaymanager.entity.GwmAlertEmail;
import com.bccs.gatewaymanager.repository.GwmAlertEmailRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Control-Plane-only - gui EMAIL thong bao khi 1 canh bao MOI duoc tao (xem
 * AlertService.ingestOne - diem "Canh bao MOI"). Mirror dung pattern fail-open
 * cua AuditLogService: hang doi trong bo nho (offer() khong block - day thi bo
 * + log.warn) + 1 thread nen gui dinh ky; moi loi SMTP chi log canh bao roi bo
 * - mail KHONG BAO GIO lam hong/cham luong ingest alert (goi tu HTTP thread
 * cua POST /api/internal/alerts).
 *
 * CHONG SPAM (2 lop):
 * - Dedup san co cua alert (1 van de = 1 row ACTIVE = 1 mail luc tao; touch/
 *   re-emit/resolve KHONG gui lai - diem goi duy nhat la tao row moi).
 * - Rate gate in-memory moi team: toi da rate-limit-max mail trong rate-limit-
 *   window-minutes phut (chan burst: 1 upstream chet keo nhieu endpoint bao
 *   cung luc). Vuot nguong -> log.warn + bo mail.
 *
 * NGUOI NHAN: bang GWM_ALERT_EMAIL cua doi (doi tu quan ly qua API
 * /api/alerts/recipients); doi chua khai -> fallback config
 * recipients-fallback (env GATEWAY_ALERT_EMAIL_FALLBACK); ca 2 rong -> bo.
 *
 * SMTP: tu build JavaMailSenderImpl (lazy lan gui dau tien) chu KHONG dung
 * autoconfig cua Spring Boot 4 - autoconfig mail cua Boot 4 nam o module rieng
 * moi, tu build chi can jakarta.mail (pull boi spring-boot-starter-mail) la
 * chay chac o moi bo phien ban. Ca phien gui deu co timeout (5s/10s) de
 * thread khong bi block lau khi SMTP khong phan hoi.
 *
 * BẮT BUỘC @Profile("control-plane"): bean inject GwmAlertEmailRepository (JPA)
 * — Data Plane loai autoconfiguration JPA/DataSource khoi context (xem
 * application-data-plane.yml) nen bean nay phai khong ton tai o DP, khong thi
 * context CRASH ngay khi khoi dong (bug that: build DP #21 CrashLoopBackOff
 * 2026-09-29, ung dung khong start duoc vi "required a bean of type
 * GwmAlertEmailRepository that could not be found").
 */
@Slf4j
@Service
@Profile("control-plane")
public class AlertEmailService {

    private static final int MAX_QUEUE_SIZE = 100;
    private static final int MAX_DETAIL_LENGTH = 500;

    private final GwmAlertEmailRepository recipientRepository;
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;
    private final boolean sslEnable;
    private final boolean starttlsEnable;
    private final String recipientsFallback;
    private final String uiBaseUrl;
    private final int rateLimitMax;
    private final long rateLimitWindowMinutes;

    /** Hang doi GwmAlert cho gui - day thi bo event moi nhat (mirror AlertEventPublisher). */
    private final BlockingQueue<GwmAlert> queue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    /** Rate gate in-memory: teamCode -> danh sach timestamp (millis) mail da gui con trong cua so. */
    private final Map<String, List<Long>> sentTimestampsByTeam = new ConcurrentHashMap<>();

    /** Lazy: tao lan dau tien khi can gui that (khong tao khi enabled=false hoac chua co mail nao). */
    private volatile JavaMailSenderImpl sender;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-alert-email-sender");
        t.setDaemon(true);
        return t;
    });

    public AlertEmailService(GwmAlertEmailRepository recipientRepository,
                             @Value("${gatewaymanager.alert.email.enabled:false}") boolean enabled,
                             @Value("${gatewaymanager.alert.email.host:smtp.vmail.viettel.vn}") String host,
                             @Value("${gatewaymanager.alert.email.port:465}") int port,
                             @Value("${gatewaymanager.alert.email.username:}") String username,
                             @Value("${gatewaymanager.alert.email.password:}") String password,
                             @Value("${gatewaymanager.alert.email.from:}") String from,
                             @Value("${gatewaymanager.alert.email.ssl-enable:true}") boolean sslEnable,
                             @Value("${gatewaymanager.alert.email.starttls-enable:false}") boolean starttlsEnable,
                             @Value("${gatewaymanager.alert.email.recipients-fallback:}") String recipientsFallback,
                             @Value("${gatewaymanager.alert.email.ui-base-url:http://localhost:4200}") String uiBaseUrl,
                             @Value("${gatewaymanager.alert.email.rate-limit-max:10}") int rateLimitMax,
                             @Value("${gatewaymanager.alert.email.rate-limit-window-minutes:5}") long rateLimitWindowMinutes) {
        this.recipientRepository = recipientRepository;
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.from = from;
        this.sslEnable = sslEnable;
        this.starttlsEnable = starttlsEnable;
        this.recipientsFallback = recipientsFallback == null ? "" : recipientsFallback;
        this.uiBaseUrl = uiBaseUrl;
        this.rateLimitMax = rateLimitMax;
        this.rateLimitWindowMinutes = rateLimitWindowMinutes;
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Gui email canh bao dang TAT (gatewaymanager.alert.email.enabled=false).");
            return;
        }
        scheduler.scheduleWithFixedDelay(this::pollAndSend, 2, 2, java.util.concurrent.TimeUnit.SECONDS);
        log.info("Gui email canh bao da BAT - SMTP {}:{} (SSL={}, STARTTLS={}), gui moi 2s, rate limit {}/{} phut/team, hang doi toi da {}.",
                host, port, sslEnable, starttlsEnable, rateLimitMax, rateLimitWindowMinutes, MAX_QUEUE_SIZE);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
        // Co gui not cuoi cung truoc khi tat - "best effort", khong chan shutdown lau (mirror AuditLogService).
        pollAndSend();
    }

    /** Goi tu AlertService - KHONG BAO GIO throw/block (queue day thi bo). Caller cung van boc try/catch (2 lop an toan). */
    public void notifyNewAlert(GwmAlert alert) {
        if (!enabled || alert == null) {
            return;
        }
        if (!queue.offer(alert)) {
            log.warn("Hang doi email canh bao day ({} phan tu) - bo 1 mail: {} {}", MAX_QUEUE_SIZE, alert.getAlertType(), alert.getTargetName());
        }
    }

    /** Package-private de test goi truc tiep thay vi cho scheduler 2s (mirror AuditLogService.flush()). */
    void pollAndSend() {
        // Danh sach mail co the gui lan nay - de cap nhat rate gate SAU KHI gui ok
        // (khong chot truoc, tranh SMTP chet ma van tru timestamp).
        List<GwmAlert> batch = new ArrayList<>();
        queue.drainTo(batch);
        for (GwmAlert alert : batch) {
            try {
                sendOne(alert);
            } catch (Exception e) {
                // FAIL-OPEN: 1 mail loi KHONG chen lang mail ke tiep (mirror AuditLogService per-doc).
                log.warn("Loi gui email canh bao (bo qua mail nay): type={} target={} - {}: {}",
                        alert.getAlertType(), alert.getTargetName(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private void sendOne(GwmAlert alert) {
        if (!tryAcquireRateGate(alert.getTeamCode())) {
            log.warn("Vuot gioi han rate email canh bao cua doi '{}' ({} mail/{} phut) - bo 1 mail: type={} target={}",
                    alert.getTeamCode(), rateLimitMax, rateLimitWindowMinutes, alert.getAlertType(), alert.getTargetName());
            return;
        }
        List<String> recipients = resolveRecipients(alert.getTeamCode());
        if (recipients.isEmpty()) {
            log.warn("Doi '{}' chua khai nguoi nhan email canh bao va khong co fallback - bo mail: type={} target={}",
                    alert.getTeamCode(), alert.getAlertType(), alert.getTargetName());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipients.toArray(new String[0]));
        message.setSubject(buildSubject(alert));
        message.setText(buildBody(alert));
        sender().send(message);
        log.info("Da gui email canh bao: doi='{}' type={} target={} -> {} nguoi nhan",
                alert.getTeamCode(), alert.getAlertType(), alert.getTargetName(), recipients.size());
    }

    /** GWM_ALERT_EMAIL cua doi -> rong thi fallback env. Trim + loai gia tri sai dinh dang + dedup (giu thu tu). */
    List<String> resolveRecipients(String teamCode) {
        Set<String> result = new LinkedHashSet<>();
        try {
            for (GwmAlertEmail row : recipientRepository.findByTeamCode(teamCode)) {
                if (isValidEmail(row.getEmail())) {
                    result.add(row.getEmail().trim());
                }
            }
        } catch (Exception e) {
            // DB loi van thu tiep fallback - mail canh bao khong duoc chet vi 1 lan doc DB hong.
            log.warn("Loi doc nguoi nhan email cua doi '{}' tu DB (dung fallback): {}", teamCode, e.getMessage());
        }
        if (result.isEmpty()) {
            for (String raw : recipientsFallback.split(",")) {
                String candidate = raw.trim();
                if (isValidEmail(candidate)) {
                    result.add(candidate);
                }
            }
        }
        return new ArrayList<>(result);
    }

    static boolean isValidEmail(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        // Don gian du dung cho dia chi noi bo: co 1 @ duy nhat, 2 phan khac rong, phan hien sau co ".".
        int at = value.indexOf('@');
        int lastAt = value.lastIndexOf('@');
        if (at <= 0 || at != lastAt || at == value.length() - 1) {
            return false;
        }
        String domain = value.substring(at + 1);
        return domain.indexOf('.') > 0 && !value.contains(" ");
    }

    /**
     * Rate gate: true = duoc gui. Dua tren danh sach timestamp con trong cua so
     * truoc; khong xoa timestamp ngay khi bi tu choi (khong mo cua so moi gian lan).
     */
    boolean tryAcquireRateGate(String teamCode) {
        long now = System.currentTimeMillis();
        long windowMillis = rateLimitWindowMinutes * 60_000L;
        List<Long> timestamps = sentTimestampsByTeam.computeIfAbsent(teamCode, k -> java.util.Collections.synchronizedList(new ArrayList<>()));
        synchronized (timestamps) {
            timestamps.removeIf(t -> t <= now - windowMillis);
            if (timestamps.size() >= rateLimitMax) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }

    static String buildSubject(GwmAlert alert) {
        return String.format("[GWM] [%s] %s - %s (doi %s)",
                alert.getSeverity(), alert.getAlertType(), alert.getTargetName(), alert.getTeamCode());
    }

    /** Instance (khong static) vi can uiBaseUrl cau hinh cho link cuoi mail. */
    String buildBody(GwmAlert alert) {
        String detail = alert.getDetail();
        if (detail != null && detail.length() > MAX_DETAIL_LENGTH) {
            detail = detail.substring(0, MAX_DETAIL_LENGTH) + "... (cat that)";
        }
        return String.format(
                "Canh bao MOI tren Gateway Manager:%n%n"
                        + "  Muc do:   %s%n"
                        + "  Loai:     %s%n"
                        + "  Muc tieu: %s (%s)%n"
                        + "  Doi:      %s%n"
                        + "  Lan dau:  %s%n"
                        + "  Chi tiet: %s%n%n"
                        + "Xem chi tiet: %s/#/alerts%n"
                        + "(Gateway Manager - thong bao tu dong, khong tra loi mail nay)",
                alert.getSeverity(), alert.getAlertType(), alert.getTargetName(), alert.getTargetType(),
                alert.getTeamCode(), alert.getFirstSeenAt(), detail == null ? "" : detail, uiBaseUrl);
    }

    /**
     * JavaMailSenderImpl tao LAZY - khong ket noi SMTP luc khoi dong (app len
     * khong can SMTP song ngay), va chi build 1 lan roi tai su dung (volatile
     * double-check).
     */
    private JavaMailSenderImpl sender() {
        JavaMailSenderImpl local = sender;
        if (local == null) {
            synchronized (this) {
                local = sender;
                if (local == null) {
                    local = buildSender();
                    sender = local;
                }
            }
        }
        return local;
    }

    /** Package-private de test override (mock JavaMailSender thay vi ket noi SMTP that). */
    JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl local = new JavaMailSenderImpl();
        local.setHost(host);
        local.setPort(port);
        if (username != null && !username.isBlank()) {
            local.setUsername(username);
            local.setPassword(password);
        }
        Properties props = local.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(username != null && !username.isBlank()));
        props.put("mail.smtp.ssl.enable", String.valueOf(sslEnable));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return local;
    }
}
