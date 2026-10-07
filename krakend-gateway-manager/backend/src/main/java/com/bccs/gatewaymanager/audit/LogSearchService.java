package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.bccs.gatewaymanager.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Doc lai RequestAuditEvent/HopAuditEvent tu Elasticsearch cho trang "Tra cuu
 * Log" - CHI DOC, khong ghi gi ca (xem AuditLogService cho phia ghi). Khac
 * voi AuditLogService, loi o day duoc THROW ra (nguoi dung dang chu dong bam
 * "Tim kiem", can biet ro neu ES khong tra loi duoc thay vi im lang tra ve
 * danh sach rong gay hieu nham "khong co log nao").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogSearchService {

    private static final String REQUESTS_INDEX = "gwm-requests-*";
    private static final String HOPS_INDEX = "gwm-hops-*";
    private static final int MAX_HOPS_PER_REQUEST = 200;

    private final ElasticsearchClient client;

    /**
     * Tra cuu DUY NHAT theo traceId (bat buoc, do UI chi con truyen tieu chi nay):
     * DUNG-NGUYEN-CHUOI tren .keyword (khong tokenize) de 1 traceId chi khop dung
     * request cua no. Khong loc them khoang thoi gian - 1 traceId la duy nhat,
     * chi qua it request thi truy van full-range van re (term query O(1)-ish).
     */
    public LogSearchResultDto searchRequests(String traceId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);

        if (traceId == null || traceId.isBlank()) {
            return new LogSearchResultDto(List.of(), 0, safePage, safeSize);
        }

        try {
            SearchResponse<RequestAuditEvent> response = client.search(s -> s
                            .index(REQUESTS_INDEX)
                            .query(Query.of(q -> q.term(t -> t.field("traceId.keyword").value(traceId.trim()))))
                            .sort(so -> so.field(f -> f.field("timestamp").order(SortOrder.Desc)))
                            .from(safePage * safeSize)
                            .size(safeSize)
                            .ignoreUnavailable(true),
                    RequestAuditEvent.class);

            List<RequestAuditEvent> items = response.hits().hits().stream().map(Hit::source).toList();
            long total = response.hits().total() != null ? response.hits().total().value() : items.size();
            return new LogSearchResultDto(items, total, safePage, safeSize);
        } catch (Exception e) {
            throw new SystemException("Loi tim kiem log tren Elasticsearch: " + e.getMessage(), e);
        }
    }

    /** Danh sach hop cua 1 request, sap theo stepOrder tang dan - "waterfall" hien thi tren UI. */
    public List<HopAuditEvent> getHops(String requestId) {
        try {
            SearchResponse<HopAuditEvent> response = client.search(s -> s
                            .index(HOPS_INDEX)
                            .query(q -> q.term(t -> t.field("requestId.keyword").value(requestId)))
                            .sort(so -> so.field(f -> f.field("stepOrder").order(SortOrder.Asc)))
                            .size(MAX_HOPS_PER_REQUEST)
                            .ignoreUnavailable(true),
                    HopAuditEvent.class);
            return response.hits().hits().stream().map(Hit::source).toList();
        } catch (Exception e) {
            throw new SystemException("Loi tim hop cua request id=" + requestId + " tren Elasticsearch: " + e.getMessage(), e);
        }
    }
}
