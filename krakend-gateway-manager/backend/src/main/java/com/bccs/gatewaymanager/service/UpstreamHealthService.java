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
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Doc snapshot suc khoe cua tat ca Upstream Service da dang ky - man hinh
 * "Dashboard suc khoe Upstream" (P1). Chi DOC, khong ghi gi ca.
 */
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class UpstreamHealthService {

    private final UpstreamServiceRepository repository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final UpstreamHttpExecutor upstreamHttpExecutor;

    @Transactional(readOnly = true)
    public List<UpstreamHealthDto> healthSnapshot() {
        return repository.findAllByTeamCodeOrderByNameAsc(CurrentTeamContext.require()).stream().map(this::toHealth).toList();
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

        // find() (KHONG phai bulkhead(name, supplier)) - dung y het ly do lam voi CircuitBreaker
        // o tren: tra ve Optional rong neu Upstream nay CHUA TUNG co lenh goi nao, khong tu tao
        // 1 bulkhead moi chi vi dang XEM man hinh health.
        Bulkhead bulkhead = bulkheadRegistry.find(u.getName()).orElse(null);
        // Chua tung goi lan nao = toan bo permit con nguyen, KHAC voi failureRate/bufferedCalls
        // o tren (khong co sentinel -1 can thiet o day, "chua dung toi" co 1 gia tri DUNG ro rang).
        int availableConcurrentCalls = bulkhead == null
                ? u.getMaxConcurrentCalls() : bulkhead.getMetrics().getAvailableConcurrentCalls();
        long bulkheadRejectedCount = upstreamHttpExecutor.bulkheadRejectionCount(u.getName());
        boolean usingDefaultBulkheadSizing = u.getMaxConcurrentCalls() == UpstreamService.DEFAULT_MAX_CONCURRENT_CALLS;

        return new UpstreamHealthDto(u.getId(), u.getName(), u.getBaseHost(), u.isCircuitBreakerEnabled(),
                state, failureRate, bufferedCalls, hits, misses, hitRate,
                u.getMaxConcurrentCalls(), availableConcurrentCalls, bulkheadRejectedCount, usingDefaultBulkheadSizing);
    }
}
