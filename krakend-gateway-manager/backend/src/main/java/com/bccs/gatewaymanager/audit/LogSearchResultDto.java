package com.bccs.gatewaymanager.audit;

import java.util.List;

/** Ket qua tim kiem 1 trang cua "gwm-requests-*" - dung cho trang "Tra cuu Log". */
public record LogSearchResultDto(
        List<RequestAuditEvent> items,
        long total,
        int page,
        int size
) {
}
