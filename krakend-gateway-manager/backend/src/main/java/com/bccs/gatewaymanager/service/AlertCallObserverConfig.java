package com.bccs.gatewaymanager.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean no-op mac dinh cho AlertCallObserver khi KHONG co implementation nao
 * khac (Control Plane khong chay detector; test don le cung khong can fake).
 * Data Plane tu dinh nghia AlertDetector bean voi cung interface ->
 * ConditionalOnMissingBean khong tao no-op nua (bean detector thang).
 * Luu y: @ConditionalOnMissingBean chi dang tin cay trong @Configuration xuly
 * sau (registration order) - day la config bean "fallback" thong thuong, da la
 * pattern an toan voi ca 2 profile vi AlertDetector khai bao @Component truoc.
 */
@Configuration
public class AlertCallObserverConfig {

    @Bean
    @ConditionalOnMissingBean(AlertCallObserver.class)
    public AlertCallObserver noOpAlertCallObserver() {
        return new AlertCallObserver() {
            @Override
            public void recordUpstreamCall(String upstreamName, boolean success) {
            }

            @Override
            public void recordEndpointRequest(String endpointId, String endpointPath, boolean success) {
            }
        };
    }
}
