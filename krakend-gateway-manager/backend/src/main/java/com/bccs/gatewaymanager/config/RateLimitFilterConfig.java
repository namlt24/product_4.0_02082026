package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.ratelimit.RateLimitService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * Dang ky RateLimitFilter tuong minh qua FilterRegistrationBean tren "/*" (bat
 * buoc dang ky rong vi Servlet urlPattern khong ho tro "tru mot vai tien to" -
 * filter tu loai tru /api/**va /actuator/** ben trong, xem RateLimitFilter) -
 * KHONG dung @Component de tranh Spring Boot tu ap dung theo cach khac voi y
 * dinh (dung y het ly do ApiKeyAuthFilterConfig da giai thich).
 *
 * Order = 2 (sau ApiKeyAuthFilter order = 1) - thu tu khong thuc su quan trong
 * o day vi 2 filter loai tru lan nhau ve pham vi (1 chi xet /api+/actuator, 1
 * chi xet phan con lai), nhung van khai bao ro rang de tranh phu thuoc vao thu
 * tu dang ky mac dinh khong xac dinh.
 */
@Configuration
public class RateLimitFilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitService rateLimitService, ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(rateLimitService, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("rateLimitFilter");
        registration.setOrder(2);
        return registration;
    }
}
