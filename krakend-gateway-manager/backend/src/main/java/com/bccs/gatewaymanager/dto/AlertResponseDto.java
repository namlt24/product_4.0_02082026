package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.AlertSeverity;
import com.bccs.gatewaymanager.entity.AlertStatus;
import com.bccs.gatewaymanager.entity.AlertTargetType;
import com.bccs.gatewaymanager.entity.AlertType;
import com.bccs.gatewaymanager.entity.GwmAlert;

import java.time.Instant;

/**
 * 1 row GWM_ALERT tra ve FE - ban ghi 1:1 voi entity (KHONG tra entity truc
 * tiep, nhu quy uoc cac controller khac: entity co the thay doi cot ma DTO
 * API giu on dinh).
 */
public record AlertResponseDto(
        String id,
        AlertType alertType,
        AlertSeverity severity,
        AlertStatus status,
        AlertTargetType targetType,
        String targetName,
        String targetId,
        String title,
        String detail,
        Instant firstSeenAt,
        Instant lastSeenAt,
        int occurrenceCount,
        Instant acknowledgedAt,
        Instant resolvedAt,
        String resolvedReason,
        Instant createdAt
) {
    public static AlertResponseDto from(GwmAlert a) {
        return new AlertResponseDto(
                a.getId(), a.getAlertType(), a.getSeverity(), a.getStatus(),
                a.getTargetType(), a.getTargetName(), a.getTargetId(),
                a.getTitle(), a.getDetail(),
                a.getFirstSeenAt(), a.getLastSeenAt(), a.getOccurrenceCount(),
                a.getAcknowledgedAt(), a.getResolvedAt(), a.getResolvedReason(),
                a.getCreatedAt());
    }
}
