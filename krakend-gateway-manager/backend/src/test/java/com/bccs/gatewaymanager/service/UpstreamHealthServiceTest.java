package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.UpstreamHealthDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpstreamHealthServiceTest {

    @Mock
    private UpstreamServiceRepository repository;
    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Mock
    private UpstreamHttpExecutor upstreamHttpExecutor;
    @Mock
    private CircuitBreaker circuitBreaker;
    @Mock
    private CircuitBreaker.Metrics metrics;
    @Mock
    private BulkheadRegistry bulkheadRegistry;
    @Mock
    private Bulkhead bulkhead;
    @Mock
    private Bulkhead.Metrics bulkheadMetrics;

    @BeforeEach
    void setUp() {
        CurrentTeamContext.set("default");
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private UpstreamService upstream(String name) {
        return upstream(name, 20);
    }

    private UpstreamService upstream(String name, int maxConcurrentCalls) {
        return UpstreamService.builder().id("u-1").name(name).baseHost("http://x")
                .circuitBreakerEnabled(true).maxConcurrentCalls(maxConcurrentCalls).build();
    }

    private UpstreamHealthService service() {
        return new UpstreamHealthService(repository, circuitBreakerRegistry, bulkheadRegistry, upstreamHttpExecutor);
    }

    @Test
    void chuaTungGoiLan_nao_traVeTrangThaiRoRang_khongPhaiCLOSED_gia() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default")).thenReturn(List.of(upstream("svc")));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.empty());

        List<UpstreamHealthDto> result = service().healthSnapshot();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).circuitState()).isEqualTo("CHUA_GOI_LAN_NAO");
        assertThat(result.get(0).failureRatePercent()).isEqualTo(-1f);
        assertThat(result.get(0).cacheHitRate()).isEqualTo(-1d);
    }

    @Test
    void daCoBreaker_anhXaDungTrangThaiVaFailureRate() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default")).thenReturn(List.of(upstream("svc")));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.of(circuitBreaker));
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        when(circuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getFailureRate()).thenReturn(75.5f);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(20);
        when(upstreamHttpExecutor.cacheHitCount("svc")).thenReturn(8L);
        when(upstreamHttpExecutor.cacheMissCount("svc")).thenReturn(2L);
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.empty());

        UpstreamHealthDto result = service().healthSnapshot().get(0);

        assertThat(result.circuitState()).isEqualTo("OPEN");
        assertThat(result.failureRatePercent()).isEqualTo(75.5f);
        assertThat(result.bufferedCalls()).isEqualTo(20);
        assertThat(result.cacheHits()).isEqualTo(8L);
        assertThat(result.cacheMisses()).isEqualTo(2L);
        assertThat(result.cacheHitRate()).isEqualTo(0.8d);
    }

    // ---- Bulkhead (xem docs/IMPROVEMENT-Bulkhead-Visibility-Gateway-Manager.md) ----

    @Test
    void bulkheadChuaTungDuocTao_availableConcurrentCallsBangDungMaxConcurrentCalls() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default")).thenReturn(List.of(upstream("svc", 20)));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.empty());

        UpstreamHealthDto result = service().healthSnapshot().get(0);

        // find() (KHONG bulkhead(name, supplier)) - chua tung goi lan nao = toan bo permit
        // con nguyen, KHONG phai -1 (khac han failureRatePercent).
        assertThat(result.maxConcurrentCalls()).isEqualTo(20);
        assertThat(result.availableConcurrentCalls()).isEqualTo(20);
        assertThat(result.bulkheadRejectedCount()).isZero();
    }

    @Test
    void bulkheadDaTungGoi_layDungSoChoConLaiVaSoLanTuChoi() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default")).thenReturn(List.of(upstream("svc", 20)));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.of(bulkhead));
        when(bulkhead.getMetrics()).thenReturn(bulkheadMetrics);
        when(bulkheadMetrics.getAvailableConcurrentCalls()).thenReturn(15);
        when(upstreamHttpExecutor.bulkheadRejectionCount("svc")).thenReturn(3L);

        UpstreamHealthDto result = service().healthSnapshot().get(0);

        assertThat(result.availableConcurrentCalls()).isEqualTo(15);
        assertThat(result.bulkheadRejectedCount()).isEqualTo(3L);
    }

    @Test
    void maxConcurrentCallsBangGiaTriMacDinh_usingDefaultBulkheadSizingTrue() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default"))
                .thenReturn(List.of(upstream("svc", UpstreamService.DEFAULT_MAX_CONCURRENT_CALLS)));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.empty());

        UpstreamHealthDto result = service().healthSnapshot().get(0);

        assertThat(result.usingDefaultBulkheadSizing()).isTrue();
    }

    @Test
    void maxConcurrentCallsDaChinhKhacDefault_usingDefaultBulkheadSizingFalse() {
        when(repository.findAllByTeamCodeOrderByNameAsc("default")).thenReturn(List.of(upstream("svc", 500)));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());
        when(bulkheadRegistry.find("svc")).thenReturn(Optional.empty());

        UpstreamHealthDto result = service().healthSnapshot().get(0);

        assertThat(result.usingDefaultBulkheadSizing()).isFalse();
    }
}
