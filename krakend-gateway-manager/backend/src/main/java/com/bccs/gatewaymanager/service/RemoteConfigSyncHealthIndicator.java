package com.bccs.gatewaymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Data-Plane-only (@Profile) - bao cho k8s biet Pod nay SAN SANG NHAN
 * TRAFFIC hay chua: chi UP sau khi RemoteConfigSyncService dong bo THANH
 * CONG it nhat 1 lan (co endpoint/upstream trong bo nho de dinh tuyen) -
 * tranh Pod moi len nhan traffic khi cache dinh tuyen con rong hoan toan.
 *
 * Ten bean "remoteConfigSyncHealthIndicator" -> Spring Boot tu suy ten
 * indicator "remoteConfigSync" trong /actuator/health (bo hau to
 * "HealthIndicator") - da khai bao gop vao group "readiness" qua
 * management.endpoint.health.group.readiness.include trong
 * application-data-plane.yml (mac dinh Spring Boot chi gom san
 * "readinessState", khong tu dong gom health indicator tuy chinh nao).
 *
 * KHONG anh huong "liveness" - mat dong bo tam thoi khong phai ly do de k8s
 * restart Pod (xem RemoteConfigSyncService, fail-open giu cache cu).
 */
@Component
@Profile("data-plane")
@RequiredArgsConstructor
public class RemoteConfigSyncHealthIndicator implements HealthIndicator {

    private final RemoteConfigSyncService syncService;

    @Override
    public Health health() {
        if (syncService.isReady()) {
            return Health.up().withDetail("everSynced", true).build();
        }
        return Health.down().withDetail("everSynced", false).build();
    }
}
