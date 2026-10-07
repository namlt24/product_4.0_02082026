/**
 * Model khop 1:1 voi DTO ben Backend (com.bccs.gatewaymanager.dto.Alert*)
 * - API trang "Canh bao" (/api/alerts) + duong day noi bo DP->CP
 * (/api/internal/alerts, khong dung tren FE). Tach file rieng voi
 * log-search.model.ts vi day la domain doc lap (canh bao van hanh, khong phai
 * log tra cuu).
 */

/** Muc do nghiem trong - CRITICAL to do (CB OPEN), WARNING to vang. */
export type AlertSeverity = 'CRITICAL' | 'WARNING';

/** Trang thai xu ly: ACTIVE (dang xay ra), ACKNOWLEDGED (da dong y xem), RESOLVED (da ket thuc). */
export type AlertStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED';

/** Loi muc tieu bi canh bao - UPSTREAM (ten UpstreamService) hoac ENDPOINT (path day du). */
export type AlertTargetType = 'UPSTREAM' | 'ENDPOINT';

export type AlertType =
  | 'CIRCUIT_OPEN'
  | 'ERROR_RATE_HIGH'
  | 'BULKHEAD_SATURATED'
  | 'ENDPOINT_ERROR_RATE';

/** 1 row GWM_ALERT tra ve FE - dung cho bang danh sach + chi tiet. */
export interface AlertResponse {
  id: string;
  alertType: AlertType;
  severity: AlertSeverity;
  status: AlertStatus;
  targetType: AlertTargetType;
  targetName: string;
  targetId: string | null;
  title: string;
  detail: string | null;
  firstSeenAt: string;
  lastSeenAt: string;
  occurrenceCount: number;
  acknowledgedAt: string | null;
  resolvedAt: string | null;
  /** DP_RESOLVED / STALENESS / MANUAL - chi co khi RESOLVED. */
  resolvedReason: string | null;
  createdAt: string;
}

export interface AlertPage {
  items: AlertResponse[];
  total: number;
  page: number;
  size: number;
}

/** Tong hop cho badge toolbar + dau trang danh sach (chi dem trang thai ACTIVE). */
export interface AlertSummary {
  activeCount: number;
  activeCriticalCount: number;
  activeWarningCount: number;
  countsByType: Record<string, number>;
}
