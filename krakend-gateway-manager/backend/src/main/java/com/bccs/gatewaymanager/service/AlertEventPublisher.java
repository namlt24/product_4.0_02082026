package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.AlertEventBatchDto;
import com.bccs.gatewaymanager.dto.AlertEventDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data-Plane-only - "duong day" day batch event canh bao ve Control Plane
 * (POST /api/internal/alerts). Mirror dung pattern fail-open cua AuditLogService:
 * - Queue ArrayBlockingQueue 500: day thi BO event moi nhat (log WARN) - canh
 *   bao khong bao gio lam hong traffic; DP restart mat queue (chap nhan - CP
 *   co staleness sweeper tu resolve alert khong duoc touch).
 * - Flush dinh ky 5s goi 1 request batch (it request hon nhieu event nho);
 *   GUI KHONG THU LAI - event mat thi mat (dung voi chu ky re-emit 30s cua
 *   AlertDetector: van de van con se duoc touch lai o chu ky ke, mat 1 event
 *   don le khong ton tai thong tin).
 * - Auth dung dung team key ma RemoteConfigSyncService dang dung
 *   (gatewaymanager.control-plane.sync-api-key) - ApiKeyAuthFilter tren CP tu
 *   resolve team_code tu key nay.
 */
@Slf4j
@Component
@Profile("data-plane")
public class AlertEventPublisher {

    private static final int QUEUE_CAPACITY = 500;
    private static final int MAX_BATCH_SIZE = 100;

    private final BlockingQueue<AlertEventDto> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final RestTemplate restTemplate;
    private final String controlPlaneBaseUrl;
    private final String syncApiKey;
    private final long flushIntervalSeconds;
    private final boolean enabled;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-alert-publisher");
        t.setDaemon(true);
        return t;
    });

    public AlertEventPublisher(RestTemplateBuilder restTemplateBuilder,
                               @Value("${gatewaymanager.control-plane.base-url}") String controlPlaneBaseUrl,
                               @Value("${gatewaymanager.control-plane.sync-api-key}") String syncApiKey,
                               @Value("${gatewaymanager.alert.publisher-flush-seconds:5}") long flushIntervalSeconds,
                               @Value("${gatewaymanager.alert.enabled:true}") boolean enabled) {
        // Timeout rieng giong RemoteConfigSyncService (5s) - KHONG dung RestTemplate
        // cua UpstreamHttpExecutor (timeout theo tung Upstream, khong lien quan).
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        this.controlPlaneBaseUrl = controlPlaneBaseUrl != null && controlPlaneBaseUrl.endsWith("/")
                ? controlPlaneBaseUrl.substring(0, controlPlaneBaseUrl.length() - 1)
                : controlPlaneBaseUrl;
        this.syncApiKey = syncApiKey;
        this.flushIntervalSeconds = flushIntervalSeconds;
        this.enabled = enabled;
    }

    /** Goi tu AlertDetector - KHONG BAO GIO throw/block (queue day thi bo). */
    void submit(AlertEventDto event) {
        if (!enabled || event == null) {
            return;
        }
        if (!queue.offer(event)) {
            log.warn("Queue canh bao day ({} events) - bo 1 event {}:{}", QUEUE_CAPACITY, event.alertType(), event.targetName());
        }
    }

    /** Goi tu AlertDetector - thanh 1 danh sach (empty = khong lam gi). */
    void submit(List<AlertEventDto> events) {
        if (!enabled || events == null || events.isEmpty()) {
            return;
        }
        for (AlertEventDto event : events) {
            submit(event);
        }
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            return;
        }
        scheduler.scheduleWithFixedDelay(this::flush, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
        log.info("Publisher canh bao da BAT - day batch ve Control Plane {} moi {}s (queue {}).",
                controlPlaneBaseUrl, flushIntervalSeconds, QUEUE_CAPACITY);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
    }

    /** Package-private de test goi truc tiep thay vi cho scheduler. */
    void flush() {
        try {
            List<AlertEventDto> batch = new ArrayList<>();
            // drainTo chi lay nhung gi co san - KHONG block thread (mirror AuditLogService).
            queue.drainTo(batch, MAX_BATCH_SIZE);
            if (batch.isEmpty()) {
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Gateway-Admin-Key", syncApiKey);
            ResponseEntity<String> response = restTemplate.exchange(
                    controlPlaneBaseUrl + "/api/internal/alerts", HttpMethod.POST,
                    new HttpEntity<>(new AlertEventBatchDto(batch), headers), String.class);
            log.debug("Da day {} event canh bao ve Control Plane (HTTP {}).", batch.size(),
                    response.getStatusCode().value());
        } catch (Exception e) {
            // FAIL-OPEN: mat batch nay (khong tra lai queue - van de van con se duoc
            // touch lai o chu ky detector ke tiep 30s, khong ton du lieu quan trong).
            log.warn("Loi day batch canh bao ve Control Plane (mat {} event, chu ky re-emit se phu hoi): {}: {}",
                    queue.size(), e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
