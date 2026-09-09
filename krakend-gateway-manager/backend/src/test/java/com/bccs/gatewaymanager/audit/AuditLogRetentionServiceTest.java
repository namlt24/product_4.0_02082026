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

    /**
     * purgeExpiredIndices() goi indices().get(...) mot lan RIENG cho moi prefix
     * ("gwm-requests-*" roi "gwm-hops-*") - stub phai phan biet theo pattern duoc
     * hoi, neu khong 1 ket qua gia se bi tinh trung lap cho ca 2 prefix.
     */
    private void stubGetForPrefix(String prefix, Map<String, IndexState> result) throws Exception {
        when(indicesClient.get(any(GetIndexRequest.class))).thenAnswer(invocation -> {
            GetIndexRequest req = invocation.getArgument(0);
            String pattern = req.index().get(0);
            GetIndexResponse response = org.mockito.Mockito.mock(GetIndexResponse.class);
            when(response.result()).thenReturn(pattern.equals(prefix + "*") ? result : Map.of());
            return response;
        });
    }

    @Test
    void bienHanGiuLog_dungRetentionDaysNgay_khongThuaKhongThieu() throws Exception {
        // retentionDays=3 tinh tu hom nay: phai giu DUNG age 0,1,2 (3 ngay), xoa tu age 3
        // tro di. Day chinh la bien da tung bi sai (off-by-one: cutoff tinh
        // "today.minusDays(retentionDays)" roi xoa "truoc cutoff" se giu du 4 ngay
        // thay vi 3) - test nay phai FAIL neu loi do quay lai.
        when(client.indices()).thenReturn(indicesClient);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String age2 = indexName("gwm-requests-", today.minusDays(2)); // PHAI giu
        String age3 = indexName("gwm-requests-", today.minusDays(3)); // PHAI xoa

        Map<String, IndexState> result = new LinkedHashMap<>();
        result.put(age2, indexState);
        result.put(age3, indexState);
        stubGetForPrefix("gwm-requests-", result);

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);
        service.purgeExpiredIndices();

        ArgumentCaptor<DeleteIndexRequest> captor = ArgumentCaptor.forClass(DeleteIndexRequest.class);
        verify(indicesClient).delete(captor.capture());
        assertThat(captor.getValue().index().get(0)).isEqualTo(age3);
    }

    @Test
    void loiXoa1Index_khongLamBoSotIndexQuaHanKhacTrongCungPrefix() throws Exception {
        // deleteIndex() tu bat loi rieng cho tung index (khong de 1 lan xoa that bai
        // lam dut ca vong lap) - xac nhan index thu 2 van duoc thu xoa du index thu
        // nhat bi loi.
        when(client.indices()).thenReturn(indicesClient);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String oldIndex1 = indexName("gwm-requests-", today.minusDays(5));
        String oldIndex2 = indexName("gwm-requests-", today.minusDays(6));

        Map<String, IndexState> result = new LinkedHashMap<>();
        result.put(oldIndex1, indexState);
        result.put(oldIndex2, indexState);
        stubGetForPrefix("gwm-requests-", result);
        when(indicesClient.delete(any(DeleteIndexRequest.class)))
                .thenThrow(new RuntimeException("ES down luc xoa index 1"))
                .thenReturn(null);

        AuditLogRetentionService service = new AuditLogRetentionService(client, true, 3);
        service.purgeExpiredIndices();

        verify(indicesClient, org.mockito.Mockito.times(2)).delete(any(DeleteIndexRequest.class));
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
