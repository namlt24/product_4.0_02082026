package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.AlertEventDto;
import com.bccs.gatewaymanager.dto.AlertPageDto;
import com.bccs.gatewaymanager.dto.AlertResponseDto;
import com.bccs.gatewaymanager.dto.AlertSummaryDto;
import com.bccs.gatewaymanager.entity.AlertStatus;
import com.bccs.gatewaymanager.entity.GwmAlert;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.GwmAlertRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Control-Plane-only - "so do" canh bao: NHAN event tu Data Plane
 * (AlertEventPublisher push qua POST /api/internal/alerts), LUU Oracle bang
 * GWM_ALERT voi dedup (1 van de = 1 row ACTIVE - xem javadoc GwmAlert), phuc
 * vu FE trang "Canh bao" (/api/alerts).
 *
 * DAO DONG GIAO THUC VOI DP:
 * - event resolved=false moi chu ky (30s) khi van de van con -> "touch" (update
 *   lastSeenAt + occurrenceCount++, KHONG tao row moi, KHONG thong bao lai).
 * - event resolved=true khi van de het -> resolve row ACTIVE tuong ung
 *   (resolvedReason=DP_RESOLVED).
 * - Neu DP chet/mat ket noi -> khong con touch -> StalenessSweeper tu resolve
 *   sau staleness-minutes (30p) voi resolvedReason=STALENESS - dung ca trong
 *   truong hop upstream da het van de nhung DP khong kip bao resolved.
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class AlertService {

    private final GwmAlertRepository alertRepository;
    private final TransactionTemplate transactionTemplate;
    private final AlertEmailService alertEmailService;

    @Value("${gatewaymanager.alert.staleness-minutes:30}")
    private int stalenessMinutes;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-alert-staleness-sweeper");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    void start() {
        // Job re (1 query tra ve it row) - 60s/lan la du; tri hoan 60s sau khoi dong
        // de khong cham DB ngay luc CP dang load registry (PostConstruct khac).
        scheduler.scheduleWithFixedDelay(this::sweepStaleAlerts, 60, 60, TimeUnit.SECONDS);
        log.info("Staleness sweeper canh bao da BAT - alert ACTIVE khong duoc touch trong {} phut se tu dong RESOLVED.", stalenessMinutes);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
    }

    /**
     * Nhuan 1 batch event tu DP. Team code LA TIN CAY TUYET DOI tu
     * CurrentTeamContext (ApiKeyAuthFilter da xac thuc team key) - KHONG lay tu
     * body vi DP khong biet team_code, va lay tu body se cho phep 1 doi gia
     * ma doi khac (noi cua ky tuan duoi). Tra ve so event da nhuan thanh cong
     * (event loi rieng le chi log + bo qua, khong lam hong ca batch - fail-open
     * giong tinh thần audit log).
     */
    public int ingest(List<AlertEventDto> events) {
        String teamCode = CurrentTeamContext.require();
        int accepted = 0;
        for (AlertEventDto event : events) {
            try {
                // Moi event 1 transaction rieng (transactionTemplate tuc thoi - cung
                // pattern reload RegistryCache): 1 event loi dedup/constraint khong
                // rollback ca batch.
                transactionTemplate.executeWithoutResult(tx -> ingestOne(teamCode, event));
                accepted++;
            } catch (Exception e) {
                log.warn("Bo qua 1 event canh bao loi (khong anh huong cac event khac trong batch): type={} target={} - {}: {}",
                        event.alertType(), event.targetName(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
        return accepted;
    }

    private void ingestOne(String teamCode, AlertEventDto event) {
        if (event.resolved()) {
            alertRepository.findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                            teamCode, event.alertType(), event.targetType(), event.targetName(),
                            List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                    .ifPresent(alert -> {
                        // DP bao het van de - resolve dung row, giu firstSeenAt lam lich su.
                        alert.setStatus(AlertStatus.RESOLVED);
                        alert.setResolvedAt(Instant.now());
                        alert.setResolvedReason("DP_RESOLVED");
                        alertRepository.save(alert);
                        log.info("Canh bao RESOLVED boi DP: team={} type={} target={}", teamCode, event.alertType(), event.targetName());
                    });
            // Neu khong con row dang mo (da resolve truoc do tu staleness/manual) - bo qua im lang.
            return;
        }

        // Dedup lookup phai gom CA ACKNOWLEDGED (khong chi ACTIVE) - neu chi tim
        // ACTIVE thi sau khi nguoi van hanh ACK, chu ky touch ke tiep se khong tim
        // thay row -> tao row ACTIVE moi song song row ACK (2 row cho cung 1 van de,
        // spam UI). Row ACK van la "van de DANG xay ra" - chi khac cach hien thi.
        var existing = alertRepository
                .findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
                        teamCode, event.alertType(), event.targetType(), event.targetName(),
                        List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED));
        if (existing.isPresent()) {
            // "Touch" - van de van con: chi cap nhat lan thay gan nhat + dem chu ky.
            // KHONG doi trang thai (row ACK giu nguyen ACK).
            GwmAlert alert = existing.get();
            alert.setLastSeenAt(event.occurredAt() != null ? event.occurredAt() : Instant.now());
            alert.setOccurrenceCount(alert.getOccurrenceCount() + 1);
            alert.setDetail(event.detail()); // chi tiet moi nhat co gia tri dieu tra hon chi tiet lan dau
            alertRepository.save(alert);
            return;
        }

        Instant now = Instant.now();
        GwmAlert alert = GwmAlert.builder()
                .teamCode(teamCode)
                .alertType(event.alertType())
                .severity(event.severity())
                .status(AlertStatus.ACTIVE)
                .targetType(event.targetType())
                .targetName(event.targetName())
                .targetId(event.targetId())
                .title(event.title())
                .detail(event.detail())
                .firstSeenAt(event.occurredAt() != null ? event.occurredAt() : now)
                .lastSeenAt(event.occurredAt() != null ? event.occurredAt() : now)
                .occurrenceCount(1)
                .build();
        try {
            alertRepository.save(alert);
            log.warn("Canh bao MOI: team={} [{}] type={} target={} - {}",
                    teamCode, event.severity(), event.alertType(), event.targetName(), event.title());
            notifyNewAlertSafely(alert);
        } catch (DataIntegrityViolationException e) {
            // Race: 2 pod DP (scale >1) push cung van de gan nhu dong thoi - unique
            // functional index V3 chi cho phep 1 row ACTIVE -> create cua pod thu 2
            // vo, coi nhu touch la du (ky tiep se vao nhanh existing.isPresent()).
            log.info("Canh bao bi trung voi row ACTIVE da co (race 2 DP) - coi nhu touch: team={} type={} target={}",
                    teamCode, event.alertType(), event.targetName());
        }
    }

    /**
     * Gui EMAIL cho canh bao MOI (AlertEmailService - chi CP, chi luc tao row
     * moi; touch/resolve KHONG mail - chong spam flapping). Du boc try/catch
     * o day VA trong AlertEmailService (2 lop an toan): loi mail KHONG BAO GIO
     * lam hong/rollback luong ingest (queue.offer non-blocking, SMTP chay
     * thread rieng - goi tu trong transaction cua ingestOne nen bat buoc
     * fail-open tuyet doi).
     */
    private void notifyNewAlertSafely(GwmAlert alert) {
        try {
            alertEmailService.notifyNewAlert(alert);
        } catch (Exception e) {
            log.warn("Loi dua canh bao vao hang doi email (bo qua): type={} target={} - {}: {}",
                    alert.getAlertType(), alert.getTargetName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /** Job 60s: alert ACTIVE/ACKNOWLEDGED lau khong duoc touch -> tu resolve (resolvedReason=STALENESS). */
    void sweepStaleAlerts() {
        try {
            Instant threshold = Instant.now().minusSeconds(stalenessMinutes * 60L);
            // Quet TAT CA cac team (khong co CurrentTeamContext trong thread scheduler) -
            // 1 query toan cuc loc ngay o DB, khong quet bang khong loc (alert co the
            // len hang nghin sau nhieu thang chay).
            List<GwmAlert> stale = alertRepository.findByStatusInAndLastSeenAtBefore(
                    List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED), threshold);
            for (GwmAlert alert : stale) {
                try {
                    transactionTemplate.executeWithoutResult(tx -> {
                        alert.setStatus(AlertStatus.RESOLVED);
                        alert.setResolvedAt(Instant.now());
                        alert.setResolvedReason("STALENESS");
                        alertRepository.save(alert);
                    });
                    log.info("Canh bao tu RESOLVED do qua lau khong co tin moi (staleness {} phut): team={} type={} target={}",
                            stalenessMinutes, alert.getTeamCode(), alert.getAlertType(), alert.getTargetName());
                } catch (Exception e) {
                    log.warn("Loi resolve 1 alert staleness (bo qua, se thu lai lan sau): id={} - {}: {}",
                            alert.getId(), e.getClass().getSimpleName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            // FAIL-SAFE: job loi khong bao gio anh huong luong chinh cua CP (pattern AuditLogRetentionService).
            log.warn("Loi staleness sweeper canh bao (bo qua lan nay, se thu lai lan sau): {}: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /** Trang danh sach (LUON scope theo team cua request - multi-tenant V2). */
    public AlertPageDto search(AlertStatus status, String targetName, int page, int size) {
        String teamCode = CurrentTeamContext.require();
        String normalizedTarget = (targetName == null || targetName.isBlank()) ? null : targetName.trim();
        // Phan trang the cong (derivable query the repository khong ho tro Pageable
        // cho ket hop filter tuy chon status/targetName nen subList sau khi lay ca
        // bo cua team - so luong alert 1 team nho (hang chuc), khong can Pageable).
        List<GwmAlert> all = (normalizedTarget == null)
                ? alertRepository.findByTeamCodeOrderByLastSeenAtDesc(teamCode)
                : alertRepository.findByTeamCodeOrderByLastSeenAtDesc(teamCode).stream()
                        .filter(a -> a.getTargetName().contains(normalizedTarget))
                        .toList();
        List<GwmAlert> filtered = (status == null)
                ? all
                : all.stream().filter(a -> a.getStatus() == status).toList();
        int total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<AlertResponseDto> items = filtered.subList(fromIndex, toIndex).stream()
                .map(AlertResponseDto::from)
                .toList();
        return new AlertPageDto(items, total, safePage, safeSize);
    }

    public AlertSummaryDto summary() {
        String teamCode = CurrentTeamContext.require();
        // Dem chi tiet theo loai de FE hien "CB OPEN x2, Error rate x1..." - tong
        // ACTIVE = tong cac loai (countByTeamCodeAndStatus bi trung lap voi tong
        // byType nen khong goi rieng). CRITICAL hien tai la duy nhat CIRCUIT_OPEN;
        // neu sau nay them loai CRITICAL moi, group theo severity thay vi theo type.
        List<Object[]> byType = alertRepository.countByTypeForTeamAndStatus(teamCode, AlertStatus.ACTIVE);
        Map<String, Long> countsByType = new LinkedHashMap<>();
        long total = 0;
        for (Object[] row : byType) {
            String typeName = ((com.bccs.gatewaymanager.entity.AlertType) row[0]).name();
            long count = (Long) row[1];
            countsByType.put(typeName, count);
            total += count;
        }
        Long openCircuits = countsByType.get(com.bccs.gatewaymanager.entity.AlertType.CIRCUIT_OPEN.name());
        long criticalCount = openCircuits != null ? openCircuits : 0;
        return AlertSummaryDto.of(criticalCount, total - criticalCount, countsByType);
    }

    public AlertResponseDto get(String id) {
        String teamCode = CurrentTeamContext.require();
        return alertRepository.findById(id)
                .filter(a -> a.getTeamCode().equals(teamCode)) // KHONG cho doi nay xem alert cua doi khac
                .map(AlertResponseDto::from)
                .orElseThrow(() -> new BusinessException("GW-ALERT-NOT-FOUND", "Khong tim thay canh bao id=" + id));
    }

    public AlertResponseDto acknowledge(String id) {
        String teamCode = CurrentTeamContext.require();
        GwmAlert alert = alertRepository.findById(id)
                .filter(a -> a.getTeamCode().equals(teamCode))
                .orElseThrow(() -> new BusinessException("GW-ALERT-NOT-FOUND", "Khong tim thay canh bao id=" + id));
        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new BusinessException("GW-ALERT-STATE", "Chi canh bao trang thai ACTIVE moi duoc acknowledge (hien tai: " + alert.getStatus() + ").");
        }
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(Instant.now());
        return AlertResponseDto.from(alertRepository.save(alert));
    }

    public AlertResponseDto resolve(String id) {
        String teamCode = CurrentTeamContext.require();
        GwmAlert alert = alertRepository.findById(id)
                .filter(a -> a.getTeamCode().equals(teamCode))
                .orElseThrow(() -> new BusinessException("GW-ALERT-NOT-FOUND", "Khong tim thay canh bao id=" + id));
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BusinessException("GW-ALERT-STATE", "Canh bao da RESOLVED tu truoc.");
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        alert.setResolvedReason("MANUAL");
        return AlertResponseDto.from(alertRepository.save(alert));
    }
}
