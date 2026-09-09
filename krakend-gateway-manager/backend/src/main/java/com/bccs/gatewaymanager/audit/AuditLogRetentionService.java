package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tu dong XOA index "gwm-requests-*"/"gwm-hops-*" qua han giu log (mac dinh 3
 * ngay - xem gatewaymanager.audit.retention-days). Elasticsearch khong co co
 * che TTL cho tung document rieng le - cach chuan la xoa CA INDEX (moi ngay 1
 * index rieng, xem AuditLogService.indexSuffix), re hon nhieu so voi xoa tung
 * document qua Delete By Query.
 *
 * Chay job dinh ky NGAY TRONG backend nay (khong dung ILM policy phia
 * Elasticsearch) - theo lua chon cua nguoi quan tri he thong: tu chua het
 * trong app, khong can doi tac quan tri Elasticsearch cua tung doi thao tac
 * gi them (khac voi mo hinh ban giao DDL cho DBA Oracle - Elasticsearch chi
 * la ha tang TUY CHON/fail-open nen chap nhan app tu quan ly vong doi index
 * cua chinh no).
 *
 * FAIL-OPEN giong AuditLogService/GatewayCacheService: Elasticsearch loi/mat
 * ket noi chi log canh bao roi bo qua LAN CHAY NAY, KHONG anh huong traffic
 * that, se tu thu lai o lan chay dinh ky ke tiep (khong co gi bi mat vinh
 * vien - index qua han se duoc xoa cham hon, khong bao gio bi bo sot han).
 */
@Slf4j
@Service
public class AuditLogRetentionService {

    private static final List<String> INDEX_PREFIXES = List.of("gwm-requests-", "gwm-hops-");
    // Khop dung hau to ngay "yyyy.MM.dd" o CUOI ten index (xem
    // AuditLogService.INDEX_DATE_SUFFIX) - vi du "gwm-requests-2026.09.06".
    private static final Pattern INDEX_DATE_SUFFIX_PATTERN = Pattern.compile("(\\d{4}\\.\\d{2}\\.\\d{2})$");
    private static final DateTimeFormatter INDEX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final ElasticsearchClient client;
    private final boolean enabled;
    private final int retentionDays;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-audit-retention");
        t.setDaemon(true);
        return t;
    });

    public AuditLogRetentionService(ElasticsearchClient client,
                                     @Value("${gatewaymanager.audit.enabled:true}") boolean enabled,
                                     @Value("${gatewaymanager.audit.retention-days:3}") int retentionDays) {
        this.client = client;
        this.enabled = enabled;
        this.retentionDays = retentionDays;
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Don dep log qua han (retention) dang TAT cung voi audit log (gatewaymanager.audit.enabled=false).");
            return;
        }
        if (retentionDays <= 0) {
            log.info("gatewaymanager.audit.retention-days={} (<=0) - KHONG tu dong xoa index audit log nao, giu vinh vien.", retentionDays);
            return;
        }
        // Xoa index la thao tac re va khong khan cap - kiem tra 1 lan/ngay la du, tri hoan
        // 1 phut sau khoi dong de ES/app kip on dinh truoc lan kiem tra dau tien.
        scheduler.scheduleWithFixedDelay(this::purgeExpiredIndices, 1, 24 * 60, TimeUnit.MINUTES);
        log.info("Don dep log qua han da BAT - giu lai {} ngay gan nhat cho gwm-requests-*/gwm-hops-*, kiem tra moi 24h.", retentionDays);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
    }

    /** Package-private (khong phai private) de test goi truc tiep thay vi phai cho scheduler 24h. */
    void purgeExpiredIndices() {
        // Tu bao ve doc lap voi start() (khong chi dua vao viec start() co lich job hay
        // khong) - phong truong hop ham nay bi goi truc tiep (test, hoac code sau nay).
        if (!enabled || retentionDays <= 0) {
            return;
        }
        LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(retentionDays);
        for (String prefix : INDEX_PREFIXES) {
            purgePrefix(prefix, cutoff);
        }
    }

    /** Moi prefix xu ly doc lap - 1 prefix loi (vi du ES tam gian doan giua chung) khong duoc lam bo sot prefix con lai. */
    private void purgePrefix(String prefix, LocalDate cutoff) {
        try {
            GetIndexRequest request = new GetIndexRequest.Builder()
                    .index(prefix + "*")
                    .ignoreUnavailable(true)
                    .allowNoIndices(true)
                    .build();
            Set<String> indexNames = client.indices().get(request).result().keySet();
            for (String indexName : indexNames) {
                LocalDate indexDate = parseIndexDate(indexName);
                if (indexDate != null && indexDate.isBefore(cutoff)) {
                    deleteIndex(indexName);
                }
            }
        } catch (Exception e) {
            log.warn("Loi liet ke index audit log '{}*' tren Elasticsearch de don dep (bo qua lan nay, KHONG anh huong traffic that, se thu lai lan sau): {}: {}",
                    prefix, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void deleteIndex(String indexName) {
        try {
            client.indices().delete(new DeleteIndexRequest.Builder().index(indexName).build());
            log.info("Da xoa index audit log qua han giu {} ngay: {}", retentionDays, indexName);
        } catch (Exception e) {
            log.warn("Loi xoa index audit log qua han '{}' (bo qua, se thu lai lan sau): {}: {}",
                    indexName, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private LocalDate parseIndexDate(String indexName) {
        Matcher matcher = INDEX_DATE_SUFFIX_PATTERN.matcher(indexName);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.parse(matcher.group(1), INDEX_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
