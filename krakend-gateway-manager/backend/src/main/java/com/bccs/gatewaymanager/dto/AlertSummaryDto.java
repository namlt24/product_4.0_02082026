package com.bccs.gatewaymanager.dto;

import java.util.Map;

/**
 * Tong hop nho cho badge toolbar + dau trang danh sach: so alert con ACTIVE
 * (theo muc do de to mau) va so luong theo loai. Chi dem ACTIVE - resolved la
 * lich su, khong doi mau badge.
 */
public record AlertSummaryDto(
        long activeCount,
        long activeCriticalCount,
        long activeWarningCount,
        /** So luong ACTIVE theo AlertType (vd CIRCUIT_OPEN=2, ERROR_RATE_HIGH=1). */
        Map<String, Long> countsByType
) {
    public static AlertSummaryDto of(long critical, long warning, Map<String, Long> countsByType) {
        return new AlertSummaryDto(critical + warning, critical, warning, countsByType);
    }
}
