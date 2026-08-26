/**
 * Model khop 1:1 voi audit event ben Backend (com.bccs.gatewaymanager.audit.*)
 * - RequestAuditEvent/HopAuditEvent/LogSearchResultDto. Tach file rieng voi
 * endpoint.model.ts vi day la domain khac han (log tra cuu, khong phai cau
 * hinh Endpoint/Upstream).
 */

export type AuditStatus = 'SUCCESS' | 'ERROR';

/** 1 dong trong bang ket qua tra cuu - ung voi 1 request client that goi vao gateway. */
export interface RequestAuditEvent {
  requestId: string;
  timestamp: string;
  endpointId: string | null;
  endpointName: string | null;
  clientMethod: string;
  clientPath: string;
  status: AuditStatus;
  httpStatus: number | null;
  errorCode: string | null;
  errorMessage: string | null;
  durationMs: number;
  requestBody: string | null;
  requestBodyTruncated: boolean;
  /** Trace APM tuong ung (Elastic APM) - null neu khong doc duoc (agent chua gan). */
  traceId: string | null;
}

/** 1 hop = 1 lan CompositeOrchestratorEngine goi 1 BackendStep toi Upstream that. */
export interface HopAuditEvent {
  requestId: string;
  stepOrder: number;
  stepName: string;
  upstreamName: string;
  method: string;
  resolvedUrl: string;
  requestBody: string | null;
  requestBodyTruncated: boolean;
  responseStatus: number | null;
  responseBody: string | null;
  responseBodyTruncated: boolean;
  durationMs: number;
  cacheHit: boolean;
  success: boolean;
  errorMessage: string | null;
  timestamp: string;
}

export interface LogSearchResult {
  items: RequestAuditEvent[];
  total: number;
  page: number;
  size: number;
}

/** Bo loc tren thanh tim kiem - tat ca deu tuy chon (rong = khong loc theo tieu chi do). */
export interface LogSearchFilter {
  from: string | null;
  to: string | null;
  status: AuditStatus | '' | null;
  endpointPath: string | null;
  bodyContains: string | null;
}
