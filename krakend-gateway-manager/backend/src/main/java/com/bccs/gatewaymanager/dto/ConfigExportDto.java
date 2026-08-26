package com.bccs.gatewaymanager.dto;

import java.time.Instant;
import java.util.List;

/**
 * Toan bo cau hinh Gateway Manager (Upstream + Endpoint) tai 1 thoi diem -
 * dung de backup/restore hoac review qua Pull Request (JSON de doc/de git
 * diff) truoc khi ap dung. Cung shape duoc dung LAI cho ca export lan import
 * (doi xung - file xuat ra co the import thang lai duoc, kem ca sang 1 DB
 * khac/moi trong rong).
 */
public record ConfigExportDto(
        String schemaVersion,
        Instant exportedAt,
        List<UpstreamServiceDto> upstreams,
        List<EndpointResponseDto> endpoints
) {
}
