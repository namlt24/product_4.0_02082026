package com.bccs.gatewaymanager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteConfigSyncHealthIndicatorTest {

    @Mock
    private RemoteConfigSyncService syncService;

    @Test
    void chuaTungDongBoThanhCong_traVeDOWN() {
        when(syncService.isReady()).thenReturn(false);
        RemoteConfigSyncHealthIndicator indicator = new RemoteConfigSyncHealthIndicator(syncService);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void daDongBoThanhCongItNhat1Lan_traVeUP() {
        when(syncService.isReady()).thenReturn(true);
        RemoteConfigSyncHealthIndicator indicator = new RemoteConfigSyncHealthIndicator(syncService);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }
}
