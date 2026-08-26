package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.bccs.gatewaymanager.exception.SystemException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LogSearchService - KHAC voi AuditLogService (fail-open o phia ghi), o day
 * loi Elasticsearch phai duoc THROW ra (SystemException) de nguoi dung dang
 * chu dong bam "Tim kiem" biet ro co loi, khong bi hieu nham "khong co log nao".
 */
@ExtendWith(MockitoExtension.class)
class LogSearchServiceTest {

    @Mock
    private ElasticsearchClient client;

    private LogSearchService service;

    @Test
    void searchRequests_loiElasticsearch_bocThanhSystemException() throws IOException {
        service = new LogSearchService(client);
        when(client.search(any(Function.class), eq(RequestAuditEvent.class)))
                .thenThrow(new RuntimeException("ES khong ket noi duoc"));

        assertThatThrownBy(() -> service.searchRequests(null, null, null, null, null, 0, 20))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("ES khong ket noi duoc");
    }

    @Test
    void getHops_loiElasticsearch_bocThanhSystemException() throws IOException {
        service = new LogSearchService(client);
        when(client.search(any(Function.class), eq(HopAuditEvent.class)))
                .thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> service.getHops("req-1"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("req-1");
    }

    @Test
    void searchRequests_pageAmDuocKepVe0_sizeQuaLonBiGioiHan200() throws IOException {
        service = new LogSearchService(client);
        SearchResponse<RequestAuditEvent> emptyResponse = SearchResponse.of(b -> b
                .took(1)
                .timedOut(false)
                .shards(sh -> sh.total(1).successful(1).failed(0))
                .hits(h -> h.hits(List.of())));
        when(client.search(any(Function.class), eq(RequestAuditEvent.class))).thenReturn(emptyResponse);

        var result = service.searchRequests(null, null, null, null, null, -5, 9999);

        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(200);
        assertThat(result.items()).isEmpty();
    }
}
