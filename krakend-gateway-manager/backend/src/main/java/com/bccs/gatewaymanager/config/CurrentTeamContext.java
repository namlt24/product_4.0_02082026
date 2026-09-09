package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.exception.SystemException;

/**
 * Ngu canh "dang xu ly request cua doi nao" - ThreadLocal thuan (KHONG phai
 * Spring bean), cung phong cach voi org.slf4j.MDC da dung san trong
 * DynamicDispatcherController de mang tracing id xuyen suot 1 request ma
 * khong phai truyen tham so qua tung layer/method.
 *
 * ApiKeyAuthFilter.doFilterInternal() la NOI DUY NHAT goi set() (sau khi tra
 * cuu key trong bang gwm_team thanh cong) va PHAI luon clear() trong finally
 * - Tomcat tai su dung thread qua connection pool, khong clear se ro ri
 * team_code cua 1 request sang request KHAC tren CUNG thread sau do (loi bao
 * mat nghiem trong: dau du du lieu giua cac doi).
 *
 * Moi service/repository lop Control Plane (EndpointService,
 * UpstreamServiceService, ConfigExportImportService, EndpointMapper...) goi
 * require() de biet dang phuc vu doi nao - throw SystemException neu goi
 * ngoai 1 request da qua ApiKeyAuthFilter (loi lap trinh, khong phai loi
 * nguoi dung - nen la SystemException chu khong phai BusinessException).
 */
public final class CurrentTeamContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CurrentTeamContext() {
    }

    public static void set(String teamCode) {
        CURRENT.set(teamCode);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static String require() {
        String teamCode = CURRENT.get();
        if (teamCode == null) {
            throw new SystemException("CurrentTeamContext chua duoc thiet lap - dang goi ngoai 1 request da qua ApiKeyAuthFilter (loi lap trinh).");
        }
        return teamCode;
    }
}
