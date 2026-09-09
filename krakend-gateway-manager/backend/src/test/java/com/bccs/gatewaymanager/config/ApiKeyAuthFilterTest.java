package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.entity.Team;
import com.bccs.gatewaymanager.repository.TeamRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ApiKeyAuthFilter la diem cach ly duy nhat giua cac doi (moi query/ghi phia
 * sau chi dua vao CurrentTeamContext do filter nay thiet lap) - test nay xac
 * nhan 3 nhanh (platform-admin key cho "/api/teams/**", per-team key cho moi
 * "/api/**" con lai, tu choi 401) dung nhu javadoc class da mo ta, VA xac
 * nhan CurrentTeamContext luon duoc clear (kem ca khi filterChain throw) -
 * ro ri ThreadLocal nay qua request khac la loi bao mat nghiem trong (dau du
 * du lieu giua cac doi).
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthFilterTest {

    private static final String PLATFORM_ADMIN_KEY = "platform-admin-key";
    private static final String HEADER = "X-Gateway-Admin-Key";

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private ApiKeyAuthFilter filter;

    @BeforeEach
    void setUp() {
        // KHONG duoc khoi tao filter bang field initializer ("= new ApiKeyAuthFilter(...)")
        // - field initializer chay TRUOC khi MockitoExtension gan cac @Mock, se nhan
        // teamRepository=null. Phai khoi tao trong @BeforeEach (chay SAU khi @Mock san sang).
        filter = new ApiKeyAuthFilter(PLATFORM_ADMIN_KEY, teamRepository, JsonMapper.builder().build());
    }

    @AfterEach
    void tearDown() {
        // Phong truong hop 1 test lo dung throw truoc khi filter kip clear - khong de
        // ro ri ThreadLocal sang test/thread khac.
        CurrentTeamContext.clear();
    }

    private void stubUnauthorizedResponseWriter() throws Exception {
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));
    }

    @Test
    void khongCoHeader_bi401_khongGoiChain() throws Exception {
        when(request.getHeader(HEADER)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        stubUnauthorizedResponseWriter();

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void apiTeams_dungPlatformAdminKey_choQua_khongSetCurrentTeamContext() throws Exception {
        when(request.getHeader(HEADER)).thenReturn(PLATFORM_ADMIN_KEY);
        when(request.getRequestURI()).thenReturn("/api/teams");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // "/api/teams/**" khong thuoc pham vi 1 doi cu the nao - CurrentTeamContext
        // KHONG duoc set trong nhanh nay.
        assertThat(currentTeamOrNull()).isNull();
    }

    @Test
    void apiTeams_dungKeyCuaMotDoiCuThe_bi401() throws Exception {
        // "/api/teams/**" CHI nhan platform-admin key - key hop le cua 1 doi khong
        // duoc dung de tu quan tri danh sach doi.
        when(request.getHeader(HEADER)).thenReturn("team-a-key");
        when(request.getRequestURI()).thenReturn("/api/teams");
        stubUnauthorizedResponseWriter();

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        // Khong duoc tra cuu gwm_team cho nhanh nay - platform-admin key so khop truoc,
        // sai thi tu choi thang, khong can hoi DB.
        verify(teamRepository, never()).findByApiKey(anyString());
    }

    @Test
    void apiEndpoints_dungKeyCuaDoiA_choQuaVaSetDungTeamCode() throws Exception {
        when(request.getHeader(HEADER)).thenReturn("team-a-key");
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        when(teamRepository.findByApiKey("team-a-key"))
                .thenReturn(Optional.of(Team.builder().teamCode("TEAM_A").teamName("Doi A").apiKey("team-a-key").build()));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Sau doFilterInternal(), filterChain(mock) da "chay" xong (khong lam gi that
        // su) nen filter da toi finally{} va clear() - kiem tra GIAN TIEP bang cach xac
        // nhan filterChain DUOC GOI trong luc CurrentTeamContext dang set dung, qua 1
        // FilterChain gia lap tu doc gia tri ngay trong doFilter() (xem test duoi).
        assertThat(currentTeamOrNull()).isNull(); // da duoc clear() sau khi tra ve
    }

    @Test
    void apiEndpoints_dungKeyCuaDoiA_TrongLucChain_CurrentTeamContextDungTeamA() throws Exception {
        when(request.getHeader(HEADER)).thenReturn("team-a-key");
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        when(teamRepository.findByApiKey("team-a-key"))
                .thenReturn(Optional.of(Team.builder().teamCode("TEAM_A").teamName("Doi A").apiKey("team-a-key").build()));
        String[] observedDuringChain = new String[1];
        org.mockito.Mockito.doAnswer(inv -> {
            observedDuringChain[0] = CurrentTeamContext.require();
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(observedDuringChain[0]).isEqualTo("TEAM_A");
    }

    @Test
    void apiEndpoints_keyKhongKhopDoiNao_bi401() throws Exception {
        when(request.getHeader(HEADER)).thenReturn("key-la");
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        when(teamRepository.findByApiKey("key-la")).thenReturn(Optional.empty());
        stubUnauthorizedResponseWriter();

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void apiEndpoints_dungPlatformAdminKey_bi401() throws Exception {
        // platform-admin key CHI dung cho "/api/teams/**" (xem javadoc
        // ApiKeyAuthFilter) - ngoai pham vi do, no chi la 1 chuoi khong khop bat ky
        // doi nao trong gwm_team, nen bi tu choi giong het 1 key la.
        when(request.getHeader(HEADER)).thenReturn(PLATFORM_ADMIN_KEY);
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        when(teamRepository.findByApiKey(PLATFORM_ADMIN_KEY)).thenReturn(Optional.empty());
        stubUnauthorizedResponseWriter();

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void chainThrow_vanClearCurrentTeamContext() throws Exception {
        when(request.getHeader(HEADER)).thenReturn("team-a-key");
        when(request.getRequestURI()).thenReturn("/api/endpoints");
        when(teamRepository.findByApiKey("team-a-key"))
                .thenReturn(Optional.of(Team.builder().teamCode("TEAM_A").teamName("Doi A").apiKey("team-a-key").build()));
        org.mockito.Mockito.doThrow(new RuntimeException("loi trong controller"))
                .when(filterChain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException ignored) {
            // Mong doi - filter KHONG nuot loi tu chain, chi dam bao finally{} van chay.
        }

        assertThat(currentTeamOrNull()).isNull();
    }

    private String currentTeamOrNull() {
        try {
            return CurrentTeamContext.require();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
