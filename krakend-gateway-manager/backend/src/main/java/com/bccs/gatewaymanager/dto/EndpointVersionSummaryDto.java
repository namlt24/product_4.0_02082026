package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.GatewayMethod;

import java.time.Instant;

/**
 * Dong trong danh sach "Lich su phien ban" - KHONG kem snapshotJson day du (co
 * the vai KB moi ban ghi neu endpoint nhieu step) de danh sach load nhanh; xem
 * chi tiet 1 phien ban rieng qua GET /api/endpoints/{id}/versions/{versionId}.
 */
public record EndpointVersionSummaryDto(
        String id,
        int versionNumber,
        EndpointChangeType changeType,
        String name,
        String path,
        GatewayMethod method,
        Instant createdAt
) {
}
