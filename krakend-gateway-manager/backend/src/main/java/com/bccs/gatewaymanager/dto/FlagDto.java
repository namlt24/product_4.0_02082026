package com.bccs.gatewaymanager.dto;

import java.time.Instant;

/**
 * DTO cờ nghiệp vụ dung cho DP sync (ConfigExportDto.flags) va
 * FlagRegistryCache.reload() - KHONG dung entity JPA truc tiep o day vi
 * entity co @CreationTimestamp/@UpdateTimestamp khong can thiet tren Data
 * Plane (DP khong co JPA). Mirror dung shape GwmFlag entity.
 */
public record FlagDto(
        String id,
        String teamCode,
        String flagCode,
        String displayName,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
