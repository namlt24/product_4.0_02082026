package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.AlertSeverity;
import com.bccs.gatewaymanager.entity.AlertTargetType;
import com.bccs.gatewaymanager.entity.AlertType;

import java.time.Instant;

/**
 * 1 event canh bao Data Plane day ve Control Plane qua POST /api/internal/alerts
 * (batch trong AlertEventBatchDto). DP KHONG biet team_code - CP tu resolve tu
 * team key trong header X-Gateway-Admin-Key (ApiKeyAuthFilter da set
 * CurrentTeamContext truoc khi controller chay).
 *
 * GIAO THUC DEDUP (chong spam - xem javadoc GwmAlert): khi van de van con ton
 * tai, DP tai gui cung event (cung alertType/targetType/targetName,
 * resolved=false) moi chu ky detector (30s) - CP chi "touch" (update
 * lastSeenAt/occurrenceCount), KHONG tao row moi, KHONG thong bao lai. Khi van
 * de het, DP gui resolved=true (1 lan) - CP resolve row ACTIVE tuong ung.
 */
public record AlertEventDto(
        AlertType alertType,
        AlertTargetType targetType,
        String targetName,
        String targetId,
        /** false = van de DANG ton tai (moi/touch); true = van de vua het (yeu cau resolve). */
        boolean resolved,
        AlertSeverity severity,
        String title,
        String detail,
        /** Thoi diem DP phat hien (lan dau cho event moi) - CP dung lam firstSeenAt. */
        Instant occurredAt
) {
}
