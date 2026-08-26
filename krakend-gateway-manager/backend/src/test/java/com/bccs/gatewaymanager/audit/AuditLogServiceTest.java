package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuditLogService PHAI fail-open tuyet doi - loi Elasticsearch (mat ket noi,
 * bulk loi...) khong bao gio duoc throw ra ngoai flush()/recordRequest()/
 * recordHop(), dung triet ly da ap dung cho GatewayCacheService/RateLimitService.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private ElasticsearchClient client;
    @Mock
    private BulkResponse bulkResponse;

    private RequestAuditEvent requestEvent() {
        return new RequestAuditEvent("req-1", Instant.now(), "ep-1", "n", "GET", "/x",
                "SUCCESS", 200, null, null, 10L, null, false, null);
    }

    private HopAuditEvent hopEvent() {
        return new HopAuditEvent("req-1", 1, "step1", "up1", "GET", "http://x", null, false,
                200, null, false, 5L, false, true, null, Instant.now());
    }

    @Test
    void disabled_khongBaoGioGoiClient() throws Exception {
        AuditLogService service = new AuditLogService(client, false);
        service.recordRequest(requestEvent());
        service.recordHop(hopEvent());

        service.flush();

        verify(client, never()).bulk(any(BulkRequest.class));
    }

    @Test
    void enabled_flush_goiBulkDungSoDocument() throws Exception {
        when(client.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);
        when(bulkResponse.errors()).thenReturn(false);
        AuditLogService service = new AuditLogService(client, true);

        service.recordRequest(requestEvent());
        service.recordHop(hopEvent());
        service.flush();

        ArgumentCaptor<BulkRequest> captor = ArgumentCaptor.forClass(BulkRequest.class);
        verify(client).bulk(captor.capture());
        assertThat(captor.getValue().operations()).hasSize(2);
    }

    @Test
    void khongCoGiTrongHangDoi_flush_khongGoiClient() throws Exception {
        AuditLogService service = new AuditLogService(client, true);

        service.flush();

        verify(client, never()).bulk(any(BulkRequest.class));
    }

    @Test
    void loiElasticsearch_flush_khongThrow_failOpen() throws Exception {
        when(client.bulk(any(BulkRequest.class))).thenThrow(new RuntimeException("ES down"));
        AuditLogService service = new AuditLogService(client, true);
        service.recordRequest(requestEvent());

        // KHONG duoc throw - day la yeu cau quan trong nhat cua class nay.
        service.flush();
    }

    @Test
    void bulkTraVeLoiMotPhan_khongThrow_chiLogCanhBao() throws Exception {
        when(client.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);
        when(bulkResponse.errors()).thenReturn(true);
        when(bulkResponse.items()).thenReturn(java.util.List.of());
        AuditLogService service = new AuditLogService(client, true);
        service.recordRequest(requestEvent());

        service.flush();
    }
}
