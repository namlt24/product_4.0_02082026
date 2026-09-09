package com.bccs.gatewaymanager.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuditLogRetentionService PHAI fail-open tuyet doi (giong AuditLogService) -
 * loi Elasticsearch khi liet ke/xoa index khong bao gio duoc throw ra ngoai
 * purgeExpiredIndices(), va chi duoc xoa DUNG index qua han giu log.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogRetentionServiceTest {

    private static final DateTimeFormatter SUFFIX = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Mock
    private ElasticsearchClient client;
    @Mock
    private co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient indicesClient;
    @Mock
    private GetIndexResponse getIndexResponse;
    @Mock
    private IndexState indexState;

    private String indexName(String prefix, LocalDate date) {
        return prefix + SUFFIX.format(date);
    }

    @Test
    void quaHanGiuLog_bDelete() throws Exception {
        when(client.indices()).thenReturn(indicesClient);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String oldRequests = indexName("gwm-requests-", today.minusDays(5));
        String freshRequests = indexName("gwm-requests-", today);
        String oldHops = indexName("gwm-hops-", today.minusDays(10));

        Map<String, IndexState> requestsResult = new LinkedHashMap<>();
        requestsResult.put(oldRequests, indexState);
        requestsResult.put(freshRequests, indexState);
        Map<String, IndexState> hopsResult = new LinkedHashMap<>();
        hopsResult.put(oldHops, indexState);

        when(indicesClient.get(any(GetIndexRequest.class)))
                .thenAnswer(invocation -> {
                    GetIndexRequest req = invocation.getArgument(0);
                    String pattern = req.index().get(0);
                    when(getIndexResponse.result()).thenReturn(pattern.startsWith("gwm-requests") ? requestsResult : hopsResult);
                    return getIndexResponse;
                });

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);
        service.purgeExpiredIndices();

        ArgumentCaptor<DeleteIndexRequest> captor = ArgumentCaptor.forClass(DeleteIndexRequest.class);
        verify(indicesClient, org.mockito.Mockito.times(2)).delete(captor.capture());
        var deletedIndices = captor.getAllValues().stream().map(r -> r.index().get(0)).toList();
        assertThat(deletedIndices).containsExactlyInAnyOrder(oldRequests, oldHops);
    }

    @Test
    void disabled_khongBaoGioGoiClient() {
        AuditLogRetentionService service = new AuditLogRetentionService(client, false, 3);

        service.purgeExpiredIndices();

        // purgeExpiredIndices() tu kiem tra "enabled" o dau ham (khong chi dua vao
        // start() khong lich job) - goi truc tiep van khong duoc dung client.indices().
        verify(client, never()).indices();
    }

    @Test
    void retentionDaysKhongDuong_khongXoaGiNhungKhongThrow() {
        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 0);

        // purgeExpiredIndices() tu kiem tra retentionDays<=0 o dau ham - goi truc tiep
        // (khong qua scheduler cua start()) van khong duoc dung client.indices().
        service.purgeExpiredIndices();

        verify(client, never()).indices();
    }

    @Test
    void loiLietKeIndex_khongThrow_boQuaPrefixDoTiepTucPrefixKhac() throws Exception {
        when(client.indices()).thenReturn(indicesClient);
        when(indicesClient.get(any(GetIndexRequest.class))).thenThrow(new RuntimeException("ES down"));

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);

        // KHONG duoc throw - day la yeu cau quan trong nhat cua class nay.
        service.purgeExpiredIndices();

        verify(indicesClient, never()).delete(any(DeleteIndexRequest.class));
    }

    @Test
    void loiXoaIndex_khongThrow() throws Exception {
        when(client.indices()).thenReturn(indicesClient);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String oldIndex = indexName("gwm-requests-", today.minusDays(5));
        Map<String, IndexState> result = Map.of(oldIndex, indexState);
        when(getIndexResponse.result()).thenReturn(result);
        when(indicesClient.get(any(GetIndexRequest.class))).thenReturn(getIndexResponse);
        when(indicesClient.delete(any(DeleteIndexRequest.class))).thenThrow(new RuntimeException("ES down luc xoa"));

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);

        service.purgeExpiredIndices();
    }

    @Test
    void khongCoIndexNaoQuaHan_khongGoiDelete() throws Exception {
        when(client.indices()).thenReturn(indicesClient);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String freshIndex = indexName("gwm-requests-", today);
        Map<String, IndexState> result = Map.of(freshIndex, indexState);
        when(getIndexResponse.result()).thenReturn(result);
        when(indicesClient.get(any(GetIndexRequest.class))).thenReturn(getIndexResponse);

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);
        service.purgeExpiredIndices();

        verify(indicesClient, never()).delete(any(DeleteIndexRequest.class));
    }
}
