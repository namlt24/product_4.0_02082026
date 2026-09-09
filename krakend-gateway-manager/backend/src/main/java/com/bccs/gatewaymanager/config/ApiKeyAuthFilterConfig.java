package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * Dang ky ApiKeyAuthFilter tuong minh voi urlPatterns gioi han - chi Control
 * Plane (/api/**) va phan Actuator nhay cam (info/circuitbreakers), KHONG dung
 * toi /actuator/health (can mo cho Docker/k8s healthcheck) va KHONG dung toi
 * Data Plane (/** qua DynamicDispatcherController).
 */
@Configuration
public class ApiKeyAuthFilterConfig {

    /** Gio la platform-admin key - CHI dung cho "/api/teams/**" (xem javadoc ApiKeyAuthFilter). */
    @Value("${gatewaymanager.admin-api-key}")
    private String adminApiKey;

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(ObjectMapper objectMapper, TeamRepository teamRepository) {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiKeyAuthFilter(adminApiKey, teamRepository, objectMapper));
        registration.addUrlPatterns(
                "/api/*",
                "/actuator/info", "/actuator/info/*",
                "/actuator/circuitbreakers", "/actuator/circuitbreakers/*",
                "/actuator/circuitbreakerevents", "/actuator/circuitbreakerevents/*"
        );
        registration.setName("apiKeyAuthFilter");
        registration.setOrder(1);
        return registration;
    }
}
