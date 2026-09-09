package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.entity.Team;
import com.bccs.gatewaymanager.exception.ErrorResponse;
import com.bccs.gatewaymanager.repository.TeamRepository;
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
import java.util.Optional;

/**
 * Chan Control Plane (/api/**) + phan nhay cam cua Actuator bang API key -
 * header "X-Gateway-Admin-Key". 2 LOAI key tu 2026-09 (multi-tenant theo
 * doi):
 *
 * 1. **platform-admin key** (gatewaymanager.admin-api-key, nhu truoc day) -
 *    CHI dung duoc cho "/api/teams/**" (man hinh Quan ly doi) - doi nen tang
 *    (chung ta) dung de tao/xoa doi, KHONG dung duoc cho cau hinh
 *    Endpoint/Upstream/log cua bat ky doi cu the nao (thu hep pham vi rui ro
 *    neu lo key nay).
 * 2. **per-team key** (bang gwm_team.api_key, tra qua TeamRepository) - dung
 *    cho MOI "/api/**" con lai (endpoints/upstreams/config/logs) VA cho Data
 *    Plane cua chinh doi do tu dong bo config (RemoteConfigSyncService) -
 *    khop key nao se gan CurrentTeamContext dung theo team_code cua key do,
 *    MOI query/ghi phia sau tu dong loc/gan theo gia tri nay.
 *
 * KHONG dung Spring Security (them ca 1 framework chi de check header la qua
 * muc voi V1) - chi la 1 OncePerRequestFilter thuan, dang ky tuong minh qua
 * FilterRegistrationBean (xem ApiKeyAuthFilterConfig) voi urlPatterns gioi
 * han, KHONG tu @Component (tranh Spring Boot tu dong ap dung cho ca "/*" ke
 * ca Data Plane).
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
    private static final String TEAM_MANAGEMENT_PREFIX = "/api/teams";

    private final String platformAdminKey;
    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(String platformAdminKey, TeamRepository teamRepository, ObjectMapper objectMapper) {
        this.platformAdminKey = platformAdminKey;
        this.teamRepository = teamRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER_NAME);
        boolean isTeamManagement = request.getRequestURI().startsWith(TEAM_MANAGEMENT_PREFIX);

        if (provided == null) {
            log.warn("Tu choi request khong co API key: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response);
            return;
        }

        if (isTeamManagement) {
            // "/api/teams/**" CHI nhan platform-admin key - khong nhan ca key cua 1
            // doi cu the (1 doi khong duoc tu quan tri danh sach doi khac).
            if (constantTimeEquals(provided, platformAdminKey)) {
                filterChain.doFilter(request, response);
                return;
            }
            log.warn("Tu choi request /api/teams/** khong dung platform-admin key: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response);
            return;
        }

        Optional<Team> team = teamRepository.findByApiKey(provided);
        if (team.isEmpty()) {
            log.warn("Tu choi request khong co/sai API key (khong khop bat ky doi nao): {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response);
            return;
        }

        CurrentTeamContext.set(team.get().getTeamCode());
        try {
            filterChain.doFilter(request, response);
        } finally {
            // BAT BUOC clear - Tomcat tai su dung thread qua connection pool, khong
            // clear se ro ri team_code request nay sang request KHAC tren cung thread.
            CurrentTeamContext.clear();
        }
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
