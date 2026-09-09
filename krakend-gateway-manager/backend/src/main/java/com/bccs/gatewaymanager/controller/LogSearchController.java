package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.audit.HopAuditEvent;
import com.bccs.gatewaymanager.audit.LogSearchResultDto;
import com.bccs.gatewaymanager.audit.LogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Control-Plane-only (@Profile) - "Tra cuu Log", doc RequestAuditEvent/
 * HopAuditEvent tu Elasticsearch (xem LogSearchService). Nam duoi /api/** nen
 * tu dong duoc ApiKeyAuthFilter bao ve y het cac API Control Plane khac -
 * khong can filter/auth rieng.
 *
 * LUU Y (gioi han da biet): Data Plane (moi doi rieng) moi la noi THUC SU ghi
 * audit log that (xem AuditLogService, khong gate profile - dung o CA 2 vi
 * UpstreamHttpExecutor dung chung cho ca Try). Trung tam Control Plane nay
 * doc qua 1 ElasticsearchClient RIENG cua no (gatewaymanager.audit.elasticsearch.*)
 * - CHI tra cuu duoc log neu instance nay tro toi CUNG 1 cum ES ma cac Data
 * Plane dang ghi vao (vd 1 cum ES dung chung giua cac doi, tuy chon) - neu moi
 * doi dung ES rieng biet hoan toan, trang nay se KHONG thay duoc log cua ho.
 * Day la quyet dinh ha tang cua tung trien khai, chua co giai phap tong hop
 * log da doi trong pham vi hien tai.
 */
@RestController
@RequestMapping("/api/logs")
@Profile("control-plane")
@RequiredArgsConstructor
public class LogSearchController {

    private final LogSearchService service;

    @GetMapping("/requests")
    public ResponseEntity<LogSearchResultDto> searchRequests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String endpointPath,
            @RequestParam(required = false) String bodyContains,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchRequests(from, to, status, endpointPath, bodyContains, page, size));
    }

    @GetMapping("/requests/{requestId}/hops")
    public ResponseEntity<List<HopAuditEvent>> getHops(@PathVariable String requestId) {
        return ResponseEntity.ok(service.getHops(requestId));
    }
}
