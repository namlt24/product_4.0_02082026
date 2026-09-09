package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.service.EndpointService;
import com.bccs.gatewaymanager.service.UpstreamServiceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DataSeeder chay nhu 1 CommandLineRunner LUC KHOI DONG - KHONG co request/
 * ApiKeyAuthFilter nao di qua truoc do, nen KHONG duoc phep phu thuoc
 * CurrentTeamContext da duoc thiet lap tu ben ngoai (khac moi service Control
 * Plane khac, luon chay TRONG 1 request that). Test nay xac nhan run() tu
 * gan/don CurrentTeamContext, khong throw SystemException "chua thiet lap".
 */
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private EndpointConfigRepository repository;
    @Mock
    private EndpointService endpointService;
    @Mock
    private UpstreamServiceService upstreamServiceService;

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    @Test
    void run_khongCoCurrentTeamContextTuTruoc_khongThrow_tuGanVaDonSachSau() {
        when(repository.count()).thenReturn(0L);
        when(upstreamServiceService.create(any())).thenAnswer(inv -> {
            UpstreamServiceDto dto = inv.getArgument(0);
            return new UpstreamServiceDto("u-1", dto.name(), dto.description(), dto.baseHost(),
                    dto.connectTimeoutMs(), dto.readTimeoutMs(), dto.circuitBreakerEnabled(),
                    dto.failureRateThreshold(), dto.retryEnabled(), dto.maxConcurrentCalls(), dto.maxWaitDurationMs(),
                    null, null);
        });
        when(endpointService.create(any())).thenReturn(
                new EndpointResponseDto("ep-1", "n", null, "/v1/user-orders/{userId}", GatewayMethod.GET, true, "json",
                        List.of(), List.of(), null, null, false, 86400, false, false, 300));

        DataSeeder seeder = new DataSeeder(repository, endpointService, upstreamServiceService);

        // KHONG duoc throw - day la yeu cau quan trong nhat cua test nay (truoc khi
        // sua, goi nay throw SystemException "CurrentTeamContext chua duoc thiet lap").
        seeder.run();

        verify(upstreamServiceService, org.mockito.Mockito.times(2)).create(any());
        verify(endpointService).create(any(EndpointRequestDto.class));
        // Khong duoc de lai team_code "default" trong ThreadLocal cho request/test
        // ke tiep tren cung thread - xac nhan qua require() throw dung nhu truoc khi set.
        org.assertj.core.api.Assertions.assertThatThrownBy(CurrentTeamContext::require)
                .isInstanceOf(com.bccs.gatewaymanager.exception.SystemException.class);
    }

    @Test
    void run_dbDaCoDuLieu_boQuaSeed_khongGoiCreate() {
        when(repository.count()).thenReturn(5L);

        DataSeeder seeder = new DataSeeder(repository, endpointService, upstreamServiceService);
        seeder.run();

        verify(upstreamServiceService, never()).create(any());
        verify(endpointService, never()).create(any());
    }
}
