package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.exception.ErrorResponse;
import com.bccs.gatewaymanager.ratelimit.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Gioi han toc do goi cho Data Plane (bao ve Upstream Service that phia sau
 * khoi 1 client spam/loi vong lap) - xem RateLimitService cho thuat toan.
 *
 * Dang ky qua FilterRegistrationBean tren "/*" (xem RateLimitFilterConfig) roi
 * TU LOAI TRU /api/** va /actuator/** ngay trong doFilterInternal - dung y het
 * ly do EndpointService.rejectReservedPath() da giai thich: urlPattern cua
 * Servlet Filter khong ho tro "tat ca TRU mot vai tien to", nen phai dang ky
 * rong roi tu loc ben trong thay vi dang ky hep.
 *
 * Control Plane (/api/**) KHONG bi gioi han o day - da co ApiKeyAuthFilter
 * chan nguoi la roi, va luong luong goi (admin qua UI) von da thap, khong co
 * rui ro "spam vo tinh dam vao upstream that" nhu Data Plane.
 *
 * Filter chay TRUOC DispatcherServlet nen GlobalExceptionHandler khong bat
 * duoc loi tu day - phai tu viet JSON body 429 dung dinh dang ErrorResponse
 * hien co, dung y het ApiKeyAuthFilter.
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!rateLimitService.isEnabled() || isReservedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        RateLimitService.Decision decision = rateLimitService.checkAndIncrement(clientKey);
        if (!decision.allowed()) {
            log.warn("Tu choi request vi vuot rate limit: client={} {} {}", clientKey, request.getMethod(), path);
            writeTooManyRequests(response, decision.retryAfterSeconds());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isReservedPath(String path) {
        return path.equals("/api") || path.startsWith("/api/")
                || path.equals("/actuator") || path.startsWith("/actuator/");
    }

    /**
     * Uu tien header X-Forwarded-For (IP goc khi di qua reverse proxy) - LUU Y:
     * header nay co the bi gia mao neu request toi thang backend khong qua 1
     * proxy dang tin cay dung filter/strip header la, day la gioi han da biet
     * cua thiet ke V1 (chua co danh sach proxy tin cay/allowlist). Trong topology
     * docker-compose hien tai, Data Plane (port 8080) nhan request THANG (khong
     * qua nginx cua frontend - nginx chi proxy /api/**), nen getRemoteAddr() moi
     * la nguon dang tin cay chinh; X-Forwarded-For chi la fallback huu ich khi
     * gateway nay duoc dat sau 1 load balancer that trong trien khai khac.
     */
    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = ErrorResponse.of("GW-RATE-LIMITED",
                "Qua nhieu request trong " + retryAfterSeconds + " giay toi - vui long thu lai sau.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
