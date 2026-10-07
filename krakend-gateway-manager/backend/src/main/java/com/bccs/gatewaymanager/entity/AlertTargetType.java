package com.bccs.gatewaymanager.entity;

/**
 * Loi muc tieu bi canh bao - TARGET_NAME o day:
 * - UPSTREAM: ten UpstreamService (duoc dung lam ten instance Resilience4j).
 * - ENDPOINT: path day du cua EndpointConfig (vd "/vor/stock-management/search-isdn")
 *   - dung path thay vi ten de nhat quan voi audit log va de doc tren UI.
 */
public enum AlertTargetType {
    UPSTREAM,
    ENDPOINT
}
