package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ghi RequestAuditEvent/HopAuditEvent vao Elasticsearch (index "gwm-requests-*"/
 * "gwm-hops-*") - dung cho trang "Tra cuu Log" (P2). KHONG BAO GIO duoc chan
 * traffic that: moi loi (ES down, mat ket noi, bulk loi 1 phan...) chi log
 * canh bao roi bo qua - dung triet ly fail-open da ap dung xuyen suot session
 * nay (GatewayCacheService, RateLimitService).
 *
 * Kien truc: hang doi trong bo nho (bounded, khong chan thread ghi - offer()
 * tra ve false ngay neu day thay vi cho) + 1 thread nen flush dinh ky bang
 * Bulk API (giam so round-trip toi ES so voi 1 request/document).
 */
@Slf4j
@Service
public class AuditLogService {

    private static final int MAX_QUEUE_SIZE = 5000;
    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter INDEX_DATE_SUFFIX =
            DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.UTC);

    private final ElasticsearchClient client;
    private final boolean enabled;

    private final BlockingQueue<QueuedDoc> queue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-audit-flush");
        t.setDaemon(true);
        return t;
    });

    public AuditLogService(ElasticsearchClient client,
                            @Value("${gatewaymanager.audit.enabled:true}") boolean enabled) {
        this.client = client;
        this.enabled = enabled;
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Audit log toi Elasticsearch dang TAT (gatewaymanager.audit.enabled=false).");
            return;
        }
        scheduler.scheduleWithFixedDelay(this::flush, 1, 1, TimeUnit.SECONDS);
        log.info("Audit log toi Elasticsearch da BAT - flush moi 1 giay, hang doi toi da {} phan tu.", MAX_QUEUE_SIZE);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
        // Co gang flush not cuoi cung truoc khi tat han - "best effort", khong chan shutdown lau.
        flush();
    }

    public void recordRequest(RequestAuditEvent event) {
        enqueue("gwm-requests-" + indexSuffix(event.timestamp()), event);
    }

    public void recordHop(HopAuditEvent event) {
        enqueue("gwm-hops-" + indexSuffix(event.timestamp()), event);
    }

    private void enqueue(String index, Object doc) {
        if (!enabled) {
            return;
        }
        if (!queue.offer(new QueuedDoc(index, doc))) {
            log.warn("Hang doi audit log da day (>{} phan tu) - bo qua 1 su kien de KHONG chan traffic that.", MAX_QUEUE_SIZE);
        }
    }

    /** Package-private (khong phai private) de test goi truc tiep thay vi phai cho scheduler 1 giay. */
    void flush() {
        List<QueuedDoc> batch = new ArrayList<>();
        queue.drainTo(batch, BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }
        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (QueuedDoc d : batch) {
                bulkBuilder.operations(op -> op.index(idx -> idx.index(d.index()).document(d.doc())));
            }
            BulkResponse response = client.bulk(bulkBuilder.build());
            if (response.errors()) {
                long errorCount = response.items().stream().filter(item -> item.error() != null).count();
                log.warn("Bulk index audit log: {}/{} document loi (xem chi tiet tu Elasticsearch neu can).",
                        errorCount, batch.size());
            }
        } catch (Exception e) {
            // Kem ca ten class exception (khong chi getMessage()) - nhieu loi (vi du
            // thieu Jackson module cho java.time) chi lo ro nguyen nhan qua class,
            // getMessage() rieng le nhieu khi qua chung chung ("Jackson exception").
            log.warn("Loi day audit log toi Elasticsearch (bo qua {} su kien lan nay, KHONG anh huong traffic that): {}: {}",
                    batch.size(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private String indexSuffix(Instant timestamp) {
        return INDEX_DATE_SUFFIX.format(timestamp != null ? timestamp : Instant.now());
    }

    private record QueuedDoc(String index, Object doc) {
    }
}
