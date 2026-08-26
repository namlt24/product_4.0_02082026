package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.bccs.gatewaymanager.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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

    public LogSearchResultDto searchRequests(Instant from, Instant to, String status, String endpointPath,
                                              String bodyContains, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);

        List<Query> filters = new ArrayList<>();
        if (from != null || to != null) {
            filters.add(Query.of(q -> q.range(r -> r.date(d -> {
                d.field("timestamp");
                if (from != null) d.gte(from.toString());
                if (to != null) d.lte(to.toString());
                return d;
            }))));
        }
        if (status != null && !status.isBlank()) {
            filters.add(Query.of(q -> q.term(t -> t.field("status.keyword").value(status))));
        }
        if (endpointPath != null && !endpointPath.isBlank()) {
            // "match" (full-text, tokenized) tren "clientPath" tung gay sai: 2 path khac
            // han nhau nhung chung 1 tu (vi du ca "/v1/branch-demo/..." lan "/v1/canvas-demo/
            // staff-full-info" cung co token "demo"/"staff") van bi coi la khop - da xac nhan
            // qua test that. Dung wildcard "chua chuoi con" tren "clientPath.keyword" (khong
            // phan tich/tokenize) de dung y nghia nguoi dung mong doi: loc theo DUNG path/doan
            // path go vao, khong phai "co tu nao do trung".
            String escaped = endpointPath.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
            filters.add(Query.of(q -> q.wildcard(w -> w.field("clientPath.keyword").value("*" + escaped + "*"))));
        }
        if (bodyContains != null && !bodyContains.isBlank()) {
            filters.add(Query.of(q -> q.match(m -> m.field("requestBody").query(bodyContains))));
        }

        Query finalQuery = filters.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(BoolQuery.of(b -> b.filter(filters))));

        try {
            SearchResponse<RequestAuditEvent> response = client.search(s -> s
                            .index(REQUESTS_INDEX)
                            .query(finalQuery)
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
