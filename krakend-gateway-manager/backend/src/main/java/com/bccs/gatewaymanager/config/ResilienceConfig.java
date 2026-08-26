package com.bccs.gatewaymanager.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lang nghe moi chuyen trang thai CLOSED/OPEN/HALF_OPEN cua TAT CA circuit
 * breaker duoc tao dong theo ten Upstream (xem UpstreamHttpExecutor) - ghi log
 * WARN de van hanh phat hien som backend nao dang co van de, thay vi doi
 * nguoi dung phan anh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            CircuitBreaker cb = entryAddedEvent.getAddedEntry();
            cb.getEventPublisher().onStateTransition(event ->
                    log.warn("[CircuitBreaker:{}] chuyen trang thai {} -> {}",
                            cb.getName(), event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()));
        });
    }
}
