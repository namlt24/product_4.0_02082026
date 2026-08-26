package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.UpstreamHealthDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Doc snapshot suc khoe cua tat ca Upstream Service da dang ky - man hinh
 * "Dashboard suc khoe Upstream" (P1). Chi DOC, khong ghi gi ca.
 */
@Service
@RequiredArgsConstructor
public class UpstreamHealthService {

    private final UpstreamServiceRepository repository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final UpstreamHttpExecutor upstreamHttpExecutor;

    @Transactional(readOnly = true)
    public List<UpstreamHealthDto> healthSnapshot() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toHealth).toList();
    }

    private UpstreamHealthDto toHealth(UpstreamService u) {
        // find() (KHONG phai circuitBreaker(name, supplier)) - tra ve Optional rong neu
        // upstream nay CHUA TUNG duoc goi lan nao, khong tu tao 1 breaker moi nhu 1 tac
        // dung phu ngoai y muon chi vi dang XEM man hinh health.
        CircuitBreaker breaker = circuitBreakerRegistry.find(u.getName()).orElse(null);
        String state = breaker == null ? "CHUA_GOI_LAN_NAO" : breaker.getState().name();
        float failureRate = breaker == null ? -1f : breaker.getMetrics().getFailureRate();
        int bufferedCalls = breaker == null ? 0 : breaker.getMetrics().getNumberOfBufferedCalls();

        long hits = upstreamHttpExecutor.cacheHitCount(u.getName());
        long misses = upstreamHttpExecutor.cacheMissCount(u.getName());
        long total = hits + misses;
        double hitRate = total == 0 ? -1d : (double) hits / total;

        return new UpstreamHealthDto(u.getId(), u.getName(), u.getBaseHost(), u.isCircuitBreakerEnabled(),
                state, failureRate, bufferedCalls, hits, misses, hitRate);
    }
}
