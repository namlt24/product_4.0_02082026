package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.UpstreamHealthDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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

    private UpstreamService upstream(String name) {
        return UpstreamService.builder().id("u-1").name(name).baseHost("http://x").circuitBreakerEnabled(true).build();
    }

    @Test
    void chuaTungGoiLan_nao_traVeTrangThaiRoRang_khongPhaiCLOSED_gia() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(upstream("svc")));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.empty());

        UpstreamHealthService svc = new UpstreamHealthService(repository, circuitBreakerRegistry, upstreamHttpExecutor);
        List<UpstreamHealthDto> result = svc.healthSnapshot();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).circuitState()).isEqualTo("CHUA_GOI_LAN_NAO");
        assertThat(result.get(0).failureRatePercent()).isEqualTo(-1f);
        assertThat(result.get(0).cacheHitRate()).isEqualTo(-1d);
    }

    @Test
    void daCoBreaker_anhXaDungTrangThaiVaFailureRate() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(upstream("svc")));
        when(circuitBreakerRegistry.find("svc")).thenReturn(Optional.of(circuitBreaker));
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        when(circuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getFailureRate()).thenReturn(75.5f);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(20);
        when(upstreamHttpExecutor.cacheHitCount("svc")).thenReturn(8L);
        when(upstreamHttpExecutor.cacheMissCount("svc")).thenReturn(2L);

        UpstreamHealthService svc = new UpstreamHealthService(repository, circuitBreakerRegistry, upstreamHttpExecutor);
        UpstreamHealthDto result = svc.healthSnapshot().get(0);

        assertThat(result.circuitState()).isEqualTo("OPEN");
        assertThat(result.failureRatePercent()).isEqualTo(75.5f);
        assertThat(result.bufferedCalls()).isEqualTo(20);
        assertThat(result.cacheHits()).isEqualTo(8L);
        assertThat(result.cacheMisses()).isEqualTo(2L);
        assertThat(result.cacheHitRate()).isEqualTo(0.8d);
    }
}
