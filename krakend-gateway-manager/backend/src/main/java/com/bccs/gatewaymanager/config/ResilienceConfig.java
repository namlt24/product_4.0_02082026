package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.service.ResilienceAlertListener;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Lang nghe cac su kien resilience "dang chu y" cua TAT CA CircuitBreaker/Bulkhead duoc
 * tao dong theo ten Upstream (hoac theo step, xem UpstreamHttpExecutor.bulkheadFor()) - ghi
 * log WARN de van hanh phat hien som van de, thay vi doi nguoi dung phan anh:
 * - CircuitBreaker: moi lan chuyen trang thai CLOSED/OPEN/HALF_OPEN.
 * - Bulkhead: moi lan 1 lenh goi bi TU CHOI (day cho, het maxWaitDurationMs) - day CHINH LA
 *   dau hieu ro nhat cua "bay Bulkhead mac dinh qua thap" (xem
 *   UpstreamService.DEFAULT_MAX_CONCURRENT_CALLS) da tung bi bo sot khi sizing 1000 RPS: sizing
 *   Tomcat/HTTP pool dung nhung Bulkhead con qua thap thi request van bi 429/503 ma khong ai
 *   nhin thay tai sao - ghi WARN o day de grep/gan alert duoc, VA dem dong lai qua
 *   UpstreamHttpExecutor.recordBulkheadRejection() de hien tren "Dashboard suc khoe Upstream".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final UpstreamHttpExecutor upstreamHttpExecutor;
    // List rong o Control Plane (khong co bean) - DP co ResilienceAlertListener.
    // Optional/List injection: khong co bean thi Spring inject list rong, khong loi.
    private final List<ResilienceAlertListener> resilienceAlertListeners;

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            CircuitBreaker cb = entryAddedEvent.getAddedEntry();
            cb.getEventPublisher().onStateTransition(event -> {
                log.warn("[CircuitBreaker:{}] chuyen trang thai {} -> {}",
                        cb.getName(), event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState());
                for (ResilienceAlertListener listener : resilienceAlertListeners) {
                    listener.onStateTransition(cb.getName(),
                            event.getStateTransition().getFromState().name(),
                            event.getStateTransition().getToState().name());
                }
            });
        });
        bulkheadRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            Bulkhead bh = entryAddedEvent.getAddedEntry();
            bh.getEventPublisher().onCallRejected(event -> {
                log.warn("[Bulkhead:{}] tu choi 1 lenh goi (day cho, het maxWaitDurationMs) - " +
                                "neu backend van con khoe (xem circuit breaker/latency), can xem lai " +
                                "maxConcurrentCalls co dang qua thap so voi tai thuc te khong.",
                        bh.getName());
                upstreamHttpExecutor.recordBulkheadRejection(bh.getName());
            });
        });
    }
}
