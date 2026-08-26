package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Chan Control Plane (/api/**) + phan nhay cam cua Actuator bang 1 API key don
 * gian - header "X-Gateway-Admin-Key" phai khop gia tri cau hinh qua
 * gatewaymanager.admin-api-key. KHONG dung Spring Security (them ca 1 framework
 * chi de check 1 header la qua muc voi V1) - chi la 1 OncePerRequestFilter thuan,
 * dang ky tuong minh qua FilterRegistrationBean (xem ApiKeyAuthFilterConfig) voi
 * urlPatterns gioi han, KHONG tu @Component (tranh Spring Boot tu dong ap dung
 * cho ca "/*" ke ca Data Plane).
 *
 * Data Plane (/** qua DynamicDispatcherController - traffic API that) CO CHU DICH
 * khong bi filter nay dung toi, giu nguyen khong auth theo dung pham vi finding
 * da chot voi nguoi dung.
 *
 * Filter chay TRUOC DispatcherServlet nen GlobalExceptionHandler khong bat duoc
 * loi tu day - phai tu viet JSON body 401 dung dinh dang ErrorResponse hien co.
 */
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Gateway-Admin-Key";

    private final String expectedApiKey;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(String expectedApiKey, ObjectMapper objectMapper) {
        this.expectedApiKey = expectedApiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER_NAME);
        if (provided == null || !constantTimeEquals(provided, expectedApiKey)) {
            log.warn("Tu choi request khong co/sai API key: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = ErrorResponse.of("GW-UNAUTHORIZED",
                "Thieu hoac sai header '" + HEADER_NAME + "'.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /** So sanh khong lo do dai key qua thoi gian xu ly (chong timing attack co ban). */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
