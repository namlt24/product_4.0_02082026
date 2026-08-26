package com.bccs.gatewaymanager.dto;

import java.util.List;

/** Tom tat ket qua 1 lan import - dung upsert (theo ten Upstream / path Endpoint), khong bao gio xoa gi ca. */
public record ConfigImportResultDto(
        int upstreamsCreated,
        int upstreamsUpdated,
        int endpointsCreated,
        int endpointsUpdated,
        List<String> warnings
) {
}
