package com.bccs.gatewaymanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Data-Plane-only - day su kien CB state transition (dang ky trong
 * ResilienceConfig) vao AlertDetector de CIRCUIT_OPEN duoc danh dau NGAY luc
 * CB mo (thay vi doi toi da 1 chu ky detector 30s). DP chi dung; CP khong co
 * bean nay (ResilienceConfig van log WARN y nhu cu - hanh vi cu khong doi).
 *
 * @Lazy de tranh chu ky khoi tao: ResilienceConfig -> AlertDetector ->
 * AlertEventPublisher -> RestTemplateBuilder... khong vong lai nhung an toan
 * hon khi resolver cham (AlertDetector implement AlertCallObserver cung duoc
 * UpstreamHttpExecutor inject).
 */
@Slf4j
@Component
@Profile("data-plane")
@RequiredArgsConstructor
public class ResilienceAlertListener {

    private final @Lazy AlertDetector alertDetector;

    /** Goi tu ResilienceConfig (sau log.warn) khi CB chuyen trang thai. */
    public void onStateTransition(String circuitBreakerName, String fromState, String toState) {
        if ("OPEN".equals(toState)) {
            try {
                alertDetector.onCircuitBreakerOpened(circuitBreakerName);
            } catch (Exception e) {
                log.warn("Loi danh dau CB OPEN cho canh bao (bo qua): {} - {}: {}",
                        circuitBreakerName, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
