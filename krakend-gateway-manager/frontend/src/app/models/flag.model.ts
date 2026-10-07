/**
 * Model CỜ NGHIỆP VỤ (GWM_FLAG - DDL V5) + Rule cờ của endpoint.
 * Xem GwmFlag/FlagService/FlagRuleDto o backend.
 */

/** 1 cờ nghiệp vụ của đội (boolean, quản lý tập trung trang "Cờ nghiệp vụ"). */
export interface GwmFlag {
  id: string;
  teamCode: string;
  flagCode: string;
  displayName: string;
  description: string | null;
  enabled: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

/** 1 dòng lịch sử đổi giá trị cờ (GWM_FLAG_HISTORY - toggle BẬT/TẮT). */
export interface GwmFlagHistory {
  id: string;
  oldValue: boolean | null;
  newValue: boolean;
  source: string;
  changedAt: string;
}

/** 1 điều kiện đơn trong nhóm của Rule - cờ flagCode phải có giá trị = expected. */
export interface FlagCondition {
  flagCode: string;
  expected: boolean;
}

/** 1 nhóm điều kiện AND của Rule - Rule khớp nếu tồn tại ít nhất 1 nhóm thỏa (OR giữa nhóm). */
export type FlagConditionGroup = FlagCondition[];

/**
 * 1 Rule cờ của endpoint - "khớp tổ hợp cờ -> hành động".
 * actionType: START_STEP (chạy từ startStepOrder) | BLOCK (chặn request, trả blockErrorCode).
 * Xem FlagRuleDto o backend.
 */
export interface FlagRule {
  id?: string;
  name: string;
  priority: number;
  conditions: FlagConditionGroup[];
  actionType: 'START_STEP' | 'BLOCK';
  startStepOrder?: number | null;
  blockErrorCode?: string | null;
  blockMessage?: string | null;
}
