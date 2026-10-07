/**
 * Model khop 1:1 voi DTO ben Backend (com.bccs.gatewaymanager.dto.*).
 * Giu dong bo thu cong vi day la du an nho, khong dung codegen OpenAPI.
 */

import { GwmFlag, FlagCondition, FlagRule } from './flag.model';

export type HttpMethodType = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

/** Noi 1 gia tri duoc bom vao khi goi step sau: path/query/header, hoac 1 field trong JSON body gui di. */
export type MappingTargetType = 'PATH' | 'QUERY' | 'HEADER' | 'BODY_FIELD';

/**
 * 1 FieldMapping xay dung loi goi CHINH cua targetStepOrder (MAIN, mac dinh - 100%
 * mapping tu truoc gio) hay loi goi BU TRU/rollback cua step do (COMPENSATION, xem
 * BackendStep.compensationUpstreamServiceId) - muc 6, saga best-effort.
 */
export type MappingTargetContext = 'MAIN' | 'COMPENSATION';

/** Nguon du lieu cua 1 FieldMapping. */
export type FieldMappingSourceType =
  | 'STEP_RESPONSE'
  | 'REQUEST_BODY'
  | 'QUERY_PARAM'
  | 'STEP_RESPONSE_ARRAY_AGGREGATE'
  | 'CONSTANT'
  | 'STEP_RESPONSE_ARRAY_MERGE'
  /** V5 - CHI dung cho conditionSourceType cua re nhanh P1-5: dieu kien doc CỜ hệ thống. */
  | 'SYSTEM_FLAG'
  /** CHI dung cho dieu kien re nhanh: header cua client (khong phai FieldMapping). */
  | 'HEADER';

/**
 * Toan tu so sanh dung cho dieu kien re nhanh (P1-5) cua 1 BackendStep.
 * 4 toan tu so sanh SO (GREATER_THAN...LESS_THAN_OR_EQUAL) bat buoc
 * conditionExpectedValue phai parse duoc thanh so (validate o backend).
 */
export type ConditionOperator =
  | 'EQUALS'
  | 'NOT_EQUALS'
  | 'EXISTS'
  | 'NOT_EXISTS'
  | 'GREATER_THAN'
  | 'GREATER_THAN_OR_EQUAL'
  | 'LESS_THAN'
  | 'LESS_THAN_OR_EQUAL'
  /** Mang chua 1 phan tu bang expectedValue (vd permissionCodes CONTAINS "A"). Khong phai mang -> khong khop. */
  | 'CONTAINS'
  | 'NOT_CONTAINS'
  /** conditionExpectedValue la mang JSON, vd ["TT_02","TT_03"]. */
  | 'IN'
  | 'NOT_IN'
  | 'STARTS_WITH'
  | 'ENDS_WITH'
  | 'STRING_CONTAINS'
  /** Chuoi rong, mang rong hoac khong ton tai - khong can expectedValue. */
  | 'IS_EMPTY'
  | 'IS_NOT_EMPTY'
  | 'MATCHES_REGEX'
  /** Kich thuoc: so phan tu mang / so ky tu chuoi / so key object. expectedValue la SO. */
  | 'SIZE_EQUALS'
  | 'SIZE_GREATER_THAN'
  | 'SIZE_GREATER_THAN_OR_EQUAL'
  | 'SIZE_LESS_THAN'
  | 'SIZE_LESS_THAN_OR_EQUAL';

/** 1 dieu kien la trong conditionGroups - nguon REQUEST_BODY/STEP_RESPONSE, moi dieu kien tu khai bao nguon. */
export interface ConditionRule {
  /** Null khi la nut NHOM (xem children). */
  sourceType?: FieldMappingSourceType | null;
  sourceField?: string | null;
  /** Chi dung khi sourceType=STEP_RESPONSE. */
  sourceStepOrder?: number | null;
  operator?: ConditionOperator | null;
  expectedValue?: string | null;
  /** Nut NHOM long nhau: danh sach nut con (khi co thi day la nhom, khong phai la). */
  children?: ConditionRule[] | null;
  /** Logic cua nut NHOM: AND (mac dinh) hoac OR. */
  logic?: 'AND' | 'OR' | null;
  /** So sanh 2 field: nguon cua ve phai. Khi co thi bo qua expectedValue. */
  compareSourceType?: FieldMappingSourceType | null;
  compareSourceField?: string | null;
  compareSourceStepOrder?: number | null;
  /** Nut LAP MANG: ANY | ALL | COUNT (xem ConditionRuleDto). Khi co, sourceType/sourceField la mang can duyet. */
  quantifier?: 'ANY' | 'ALL' | 'COUNT' | null;
  /** Chi voi COUNT: toan tu so sanh so phan tu khop, va nguong (chuoi so). */
  countOperator?: ConditionOperator | null;
  countValue?: string | null;
}

/** 1 nhanh cua step RE NHIEU NHANH (switch): groups khop -> chuyen sang nextStepOrder. */
export interface SwitchBranch {
  groups: ConditionRule[][];
  nextStepOrder: number | null;
}

export interface FieldRenameMap {
  [sourceField: string]: string;
}

/** Backend that duoc dang ky 1 lan, tai su dung o nhieu BackendStep. */
export interface UpstreamService {
  id?: string;
  name: string;
  description?: string | null;
  baseHost: string;
  connectTimeoutMs: number;
  readTimeoutMs: number;
  circuitBreakerEnabled: boolean;
  failureRateThreshold: number;
  retryEnabled: boolean;
  /** So luong lenh goi dong thoi toi da qua Bulkhead cho Upstream nay - truoc day fix cung 20 cho MOI Upstream. */
  maxConcurrentCalls: number;
  /** Thoi gian (ms) cho "cho" trong Bulkhead truoc khi bi tu choi - truoc day fix cung 500ms. */
  maxWaitDurationMs: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface BackendStep {
  id?: string;
  stepOrder: number;
  name: string;
  method: HttpMethodType;
  urlPattern: string;
  /** Id cua UpstreamService da dang ky - thay the danh sach "hosts" go tay truoc day. */
  upstreamServiceId: string;
  /** Ten Upstream, chi de hien thi - khong gui len khi luu (backend tu resolve lai theo id). */
  upstreamServiceName?: string | null;
  /** true = lay nguyen body goc cua client lam nen truoc khi ap BODY_FIELD mapping. */
  forwardOriginalBody: boolean;
  /** Cache Redis rieng cho step nay (chi GET) - moi Upstream bi nhieu step goi toi nhieu ham khac nhau, khong phai ham nao cung nen cache. */
  cacheEnabled: boolean;
  cacheTtlSeconds: number;
  group?: string | null;
  /** Ten field can "boc vo" response truoc khi mapping/allow/deny, vi du "data" (StandardResponse cua BCCS). */
  target?: string | null;
  allowFields: string[];
  denyFields: string[];
  fieldRenameMapping: FieldRenameMap;
  /** Vi tri tren trang "Canvas mới" (kéo thả trực quan) - null = chưa từng kéo thả, FE tự suy auto-layout theo stepOrder. */
  canvasX?: number | null;
  canvasY?: number | null;

  /** Override connect/read timeout RIÊNG cho step này - null/để trống = dùng mặc định của Upstream Service. */
  connectTimeoutMs?: number | null;
  readTimeoutMs?: number | null;

  /**
   * Override Bulkhead (Resilience4j) RIÊNG cho step này - null/để trống = dùng CHUNG
   * Bulkhead của Upstream Service (mặc định, mọi step/Endpoint khác gọi cùng Upstream
   * vẫn CHIA SẺ chung 1 giới hạn số lệnh gọi đồng thời). Đặt giá trị khác null để tách
   * ra 1 Bulkhead riêng, độc lập, chỉ áp dụng cho step này - dùng khi 1 Endpoint/step cụ
   * thể có nguy cơ "chiếm hết chỗ" của các Endpoint khác đang gọi chung Upstream (noisy
   * neighbor). Hệ thống KHÔNG tự kiểm tra tổng các override có vượt sức chịu thật của
   * Upstream hay không - tự tính theo công thức P99(giây) × rps thực tế × 1.2.
   */
  maxConcurrentCallsOverride?: number | null;
  /** Chỉ có ý nghĩa khi maxConcurrentCallsOverride khác null - null = dùng Upstream Service.maxWaitDurationMs. */
  maxWaitDurationMsOverride?: number | null;

  /**
   * Rẽ nhánh (P1-5) - tất cả optional, không khai báo (conditionOperator=null/undefined)
   * = step chạy bình thường theo đúng stepOrder kế tiếp, không đổi hành vi cũ.
   * Chỉ dùng STEP_RESPONSE/REQUEST_BODY (không dùng STEP_RESPONSE_ARRAY_AGGREGATE).
   * V5: sourceType=SYSTEM_FLAG - conditionSourceField = mã cờ, conditionExpectedValue = "true"/"false".
   */
  conditionSourceType?: FieldMappingSourceType | null;
  conditionSourceStepOrder?: number | null;
  conditionSourceField?: string | null;
  conditionOperator?: ConditionOperator | null;
  /** Chỉ dùng khi conditionOperator=EQUALS/NOT_EQUALS. */
  conditionExpectedValue?: string | null;
  /**
   * V5b - điều kiện cờ NHIỀU NHÓM khi conditionSourceType=SYSTEM_FLAG:
   * AND trong nhóm, OR giữa nhóm (mirror FlagRule.conditions). Khác null/rỗng
   * → engine đánh giá nhóm, bỏ qua điều kiện 1 cờ đơn lẻ. Null = endpoint cũ.
   */
  conditionFlagGroups?: FlagCondition[][] | null;
  /**
   * V6 - điều kiện NHIỀU NGUỒN (REQUEST_BODY/STEP_RESPONSE) NHIỀU NHÓM: AND trong nhóm,
   * OR giữa nhóm. Khác null/rỗng → engine BỎ QUA điều kiện đơn lẻ và SYSTEM_FLAG.
   */
  conditionGroups?: ConditionRule[][] | null;
  /** Re nhieu nhanh (switch): nhanh dau tien khop duoc chon; khong nhanh nao khop -> nextStepOrderIfFalse. */
  switchBranches?: SwitchBranch[] | null;
  /** Nhanh SAI / khong nhanh nao khop: bao loi (ma + thong diep) thay vi chuyen step. */
  falseErrorCode?: string | null;
  falseErrorMessage?: string | null;
  /** null = nếu điều kiện ĐÚNG thì kết thúc chuỗi tại đây (kết quả step này là response cuối cùng). */
  nextStepOrderIfTrue?: number | null;
  /** null = nếu điều kiện SAI thì kết thúc chuỗi tại đây. */
  nextStepOrderIfFalse?: number | null;
  /**
   * Fallback khi step này LỖI (upstream timeout/4xx/5xx) - null = throw ngay, kết thúc cả
   * chuỗi (hành vi cũ). ĐỘC LẬP với conditionOperator - dùng được cả cho step tuần tự thường.
   */
  onErrorStepOrder?: number | null;
  /**
   * "Wave" song song trong 1 chuỗi sequential - các step CÙNG giá trị parallelGroup
   * chạy ĐỒNG THỜI, rồi step tiếp theo (tuần tự) mới chạy, có thể đọc dữ liệu đã gộp
   * của cả wave. Chỉ có ý nghĩa khi EndpointConfig.sequential=true. Ràng buộc V1 (xem
   * backend EndpointService.validateParallelGroups()): step trong wave KHÔNG được có
   * conditionOperator/onErrorStepOrder riêng; KHÔNG step nào khác được rẽ nhánh/fallback
   * TỚI 1 step trong wave; stepOrder của 1 wave PHẢI liên tiếp (vd {2,3}, không {2,4}).
   */
  parallelGroup?: number | null;

  /**
   * Bu tru/rollback nghiep vu (saga best-effort, muc 6) - khai bao TUY CHON: neu step
   * nay CHAY THANH CONG nhung SAU DO ca chuoi that bai (loi khong duoc onErrorStepOrder
   * xu ly), engine tu dong goi 1 "lenh bu tru" rieng (vi du DELETE /orders/{orderId}).
   * Ca 3 field deu tuy chon nhung PHAI cung co hoac cung khong co (xem backend
   * EndpointService.validateCompensationConfig()). Chi hoat dong khi sequential=true.
   */
  compensationUpstreamServiceId?: string | null;
  /** Ten Upstream bu tru, chi de hien thi (mirror upstreamServiceName). */
  compensationUpstreamServiceName?: string | null;
  compensationMethod?: HttpMethodType | null;
  compensationUrlPattern?: string | null;
}

/** Khai bao "trich xuat 1 gia tri -> bom vao step Y". Xem FieldMappingSourceType cho y nghia cac field con lai. */
export interface FieldMapping {
  id?: string;
  sourceType: FieldMappingSourceType;
  /** Bat buoc khi sourceType=STEP_RESPONSE hoac STEP_RESPONSE_ARRAY_AGGREGATE. */
  sourceStepOrder?: number | null;
  /** Bat buoc khi sourceType=STEP_RESPONSE, REQUEST_BODY hoac QUERY_PARAM (ten query param can doc). */
  sourceField?: string | null;
  /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE: duong dan toi mang, vi du "data". */
  sourceArrayField?: string | null;
  /** Bat buoc khi sourceType=STEP_RESPONSE_ARRAY_AGGREGATE: field lay tu MOI phan tu, vi du "code". */
  sourceElementField?: string | null;
  /** Bat buoc khi sourceType=CONSTANT: gia tri hang so co dinh, khong doc tu request/response. */
  constantValue?: string | null;
  targetStepOrder: number;
  targetType: MappingTargetType;
  targetParamName: string;
  /** Vi tri hien thi khi sap xep (trang "Khai bao endpoint keo tha") - KHONG anh huong hanh vi engine. */
  mappingOrder: number;
  /** Mapping nay danh cho loi goi CHINH (MAIN) hay loi goi BU TRU (COMPENSATION) cua targetStepOrder - xem MappingTargetContext. */
  targetContext?: MappingTargetContext | null;
}

/**
 * Tinh nang ResponseMapping - "ghép field tu BAT KY step nao vao RESPONSE CUOI"
 * (khac FieldMapping: target la request cua step khac). Field nguon doc tu stepResults
 * cua step (DA boc step.target + DA transform) - muon map field trong envelope BCCS
 * thi dat target cua step do rong. Xem ResponseMappingSpecDto ben backend.
 */
export type ResponseMappingMode = 'MERGE' | 'REPLACE';

/** 1 dong: lay sourceField (dot-path, ho tro [N]) tu step sourceStepOrder -> ghi vao targetField cua response cuoi. */
export interface ResponseFieldMapping {
  sourceStepOrder: number;
  sourceField: string;
  targetField: string;
  /** Thu tu ap dung - gia tri lon hon ghi sau (de len targetField trung ten). */
  mappingOrder: number;
}

/** null/khong khai bao = khong dung (100% endpoint cu giu hanh vi cu). */
export interface ResponseMappingSpec {
  mode: ResponseMappingMode;
  mappings: ResponseFieldMapping[];
}

/**
 * Tim TEN cac query param client can truyen khi goi endpoint nay - dua tren cac
 * FieldMapping sourceType=QUERY_PARAM (sourceField = dung ten query param, xem
 * CompositeOrchestratorEngine.firstQueryParamValue() ben backend). Dung de tu sinh
 * o nhap query param cho "Thu ngay"/"Thu nhanh" (xem TryPanelComponent) - truoc day
 * UI bo qua hoan toan phan nay vi tuong nham engine chua ho tro QUERY_PARAM.
 */
export function extractQueryParamNames(mappings: FieldMapping[]): string[] {
  const names = new Set<string>();
  for (const m of mappings) {
    if (m.sourceType === 'QUERY_PARAM' && m.sourceField) {
      names.add(m.sourceField);
    }
  }
  return [...names];
}

/**
 * V5 - Tim TAT CA ma cờ mà 1 endpoint dùng: từ Rule cờ (flagRules[].conditions)
 * + điều kiện rẽ nhánh sourceType=SYSTEM_FLAG của step (conditionSourceField = mã cờ).
 * Dùng để hiện toggle cờ trong "Thu ngay"/"Thu nhanh".
 */
export function extractFlagCodes(endpoint: EndpointConfig): string[] {
  const names = new Set<string>();
  for (const rule of endpoint.flagRules ?? []) {
    for (const group of rule.conditions ?? []) {
      for (const cond of group) {
        if (cond.flagCode) names.add(cond.flagCode);
      }
    }
  }
  for (const step of endpoint.steps) {
    if (step.conditionSourceType !== 'SYSTEM_FLAG') continue;
    if (step.conditionFlagGroups?.length) {
      for (const group of step.conditionFlagGroups) {
        for (const cond of group) {
          if (cond.flagCode) names.add(cond.flagCode);
        }
      }
    } else if (step.conditionSourceField) {
      names.add(step.conditionSourceField);
    }
  }
  return [...names];
}

export interface EndpointConfig {
  id?: string;
  name: string;
  description?: string | null;
  path: string;
  method: HttpMethodType;
  sequential: boolean;
  outputEncoding: string;
  steps: BackendStep[];
  mappings: FieldMapping[];
  createdAt?: string;
  updatedAt?: string;
  /**
   * Bat idempotency-key: client gui header "Idempotency-Key" - lan goi dau tien voi 1 key
   * duoc thuc thi + cache response THANH CONG trong idempotencyTtlSeconds; lan goi tiep
   * theo CUNG key trong TTL do tra thang response cu, khong goi lai backend that (tranh
   * side-effect lap khi client tu dong retry). Mac dinh tat.
   */
  idempotencyEnabled: boolean;
  idempotencyTtlSeconds: number;
  /**
   * Chi co y nghia khi sequential=false (step doc lap) - bat song song hoa THAT SU cac
   * step qua thread pool rieng thay vi vong lap tuan tu. DANH DOI: step co side-effect
   * that (POST/PUT/DELETE) co the da chay xong truoc khi loi cua step khac duoc phat
   * hien - chi nen bat cho step khong side-effect quan trong/idempotent. Mac dinh tat.
   */
  parallelExecution: boolean;
  /**
   * Cache TOAN BO response cuoi cung, dung CHUNG cho MOI client goi cung tham so - khac
   * idempotencyEnabled (cache theo 1 key rieng client tu khai bao qua header). CHAN CUNG
   * (backend validate): chi bat duoc khi endpoint VA TOAN BO step deu la GET, tranh 2
   * client khac nhau nhan nham ket qua cua nhau tren step mutating. Mac dinh tat.
   */
  responseCacheEnabled: boolean;
  responseCacheTtlSeconds: number;
  /**
   * V5 - Mang RULE CỜ gắn vào endpoint (null/rỗng = không dùng, 100% endpoint cũ giữ hành vi).
   * "Khớp tổ hợp cờ -> bắt đầu từ step nào / chặn request" - xem FlagRule trong flag.model.ts
   * và FlagRuleDto o backend. Chỉ hợp lệ khi sequential=true.
   */
  flagRules?: FlagRule[];
  /**
   * Tinh nang ResponseMapping - ghép field tu bat ky step nao vao response cuoi
   * (null = khong dung). Xem ResponseMappingSpec o tren.
   */
  responseMapping?: ResponseMappingSpec | null;
}

export type EndpointChangeType = 'CREATED' | 'UPDATED' | 'ROLLED_BACK';

/** 1 dong trong "Lich su phien ban" cua 1 Endpoint - xem EndpointVersionSummaryDto o backend. */
export interface EndpointVersionSummary {
  id: string;
  versionNumber: number;
  changeType: EndpointChangeType;
  name: string;
  path: string;
  method: HttpMethodType;
  createdAt: string;
}

export interface DeployResult {
  success: boolean;
  message: string;
  warnings: string[];
}

export interface ApiErrorBody {
  errorCode: string;
  message: string;
  timestamp: string;
}

/** 1 endpoint trong so do phu thuoc, kem so lieu hien thi badge. */
export interface GraphNode {
  id: string;
  name: string;
  path: string;
  method: HttpMethodType;
  sequential: boolean;
  stepCount: number;
  usedByCount: number;
  callsCount: number;
  inCycle: boolean;
  layer: number;
  /** Cum lien thong (connected component) - endpoint cung componentId thi lien quan truc/gian tiep voi nhau. Tinh san o backend. */
  componentId: number;
  /** Khong co quan he phu thuoc nao (khong goi ai, khong bi ai goi) - nen an khoi so do khi so luong lon. */
  isolated: boolean;
}

/** 1 canh phu thuoc: endpoint fromEndpointId co step (viaStepOrder) goi nguoc vao endpoint toEndpointId. */
export interface GraphEdge {
  fromEndpointId: string;
  toEndpointId: string;
  viaStepOrder: number;
}

export interface DependencyGraph {
  nodes: GraphNode[];
  edges: GraphEdge[];
  cycleWarnings: string[];
}

/** Thong tin chinh gateway - dung de tu dong dien host cho Endpoint Picker. */
export interface GatewayInfo {
  port: number;
  selfBaseUrl: string;
  selfHostAliases: string[];
}

/** Doi tuong ung voi API key hien tai - GET /api/me, dung hien badge tren toolbar. */
export interface CurrentTeam {
  teamCode: string;
  teamName: string;
}

/** Suc khoe 1 Upstream Service tai thoi diem goi API - xem UpstreamHealthDto o backend. */
export interface UpstreamHealth {
  id: string;
  name: string;
  baseHost: string;
  circuitBreakerEnabled: boolean;
  circuitState: string;
  failureRatePercent: number;
  bufferedCalls: number;
  cacheHits: number;
  cacheMisses: number;
  /** -1 = chua co du lieu (chua tung goi qua step cacheEnabled nao), khac 0% that su. */
  cacheHitRate: number;
  /** Giới hạn số lệnh gọi đồng thời (Bulkhead dùng chung theo Upstream). */
  maxConcurrentCalls: number;
  /** Số "chỗ" Bulkhead còn trống tại thời điểm xem màn hình - = maxConcurrentCalls nếu chưa từng có lệnh gọi nào. */
  availableConcurrentCalls: number;
  /** Đếm dồn từ lúc app khởi động - số lần Bulkhead từ chối 1 lệnh gọi. */
  bulkheadRejectedCount: number;
  /** true = maxConcurrentCalls vẫn đang ở giá trị mặc định lúc tạo - nên kiểm tra lại trước khi nhận tải thật. */
  usingDefaultBulkheadSizing: boolean;
}

/** Payload "Thu ngay" - xem EndpointTryRequestDto o backend. */
export interface EndpointTryRequest {
  pathVariables: Record<string, string>;
  queryParams: Record<string, string>;
  body: string | null;
  /** V5 - Ghe đe giá trị cờ CHI TRONG lấn chạy thử này ("Thu ngay") - không ảnh hưởng giá trị thật. */
  flagOverrides?: Record<string, boolean>;
}

/** Payload "Thu nhanh" cho 1 draft CHUA LUU - xem EndpointAdhocTryRequestDto o backend. */
export interface EndpointAdhocTryRequest {
  endpoint: EndpointConfig;
  pathVariables: Record<string, string>;
  queryParams: Record<string, string>;
  body: string | null;
  /** V5 - Ghe đe giá trị cờ trong lần chạy thử này ("Thu nhanh"). */
  flagOverrides?: Record<string, boolean>;
}

/** 1 dong trong waterfall tung step - xem StepTraceDto o backend. */
export interface StepTraceDto {
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
  /** V5 - Thông tin Rule cờ khớp (stepOrder=-1, stepName="RULE"): "Rule 'X' khớp → bắt đầu từ step Y" / CHẶN. */
  matchedFlagRuleInfo?: string | null;
  /** Chi tiet 1 dieu kien re nhanh da danh gia (field doc, gia tri thay, ket qua) - dong "DIEU KIEN". */
  conditionInfo?: string | null;
}

/** Ket qua 1 lan "Thu ngay"/"Thu nhanh" - xem TryResultDto o backend. LUON tra ve (HTTP 200) ke ca that bai. */
export interface TryResult {
  success: boolean;
  result: unknown | null;
  errorCode: string | null;
  errorMessage: string | null;
  hops: StepTraceDto[];
}

/** Bundle Export/Import cau hinh - xem ConfigExportDto o backend. */
export interface ConfigExportBundle {
  schemaVersion: string;
  exportedAt: string;
  upstreams: UpstreamService[];
  endpoints: EndpointConfig[];
  /** V5 - Các cờ nghiệp vụ của đội (GWM_FLAG) - import upsert khớp flagCode. */
  flags?: GwmFlag[];
}

/** Ket qua 1 lan Import - xem ConfigImportResultDto o backend. */
export interface ConfigImportResult {
  upstreamsCreated: number;
  upstreamsUpdated: number;
  endpointsCreated: number;
  endpointsUpdated: number;
  warnings: string[];
}

export const HTTP_METHODS: HttpMethodType[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];
export const MAPPING_TARGET_TYPES: MappingTargetType[] = ['PATH', 'QUERY', 'HEADER', 'BODY_FIELD'];
export const MAPPING_TARGET_CONTEXTS: MappingTargetContext[] = ['MAIN', 'COMPENSATION'];
export const FIELD_MAPPING_SOURCE_TYPES: FieldMappingSourceType[] = [
  'STEP_RESPONSE',
  'REQUEST_BODY',
  'QUERY_PARAM',
  'STEP_RESPONSE_ARRAY_AGGREGATE',
  'CONSTANT',
  'STEP_RESPONSE_ARRAY_MERGE',
];

/** Endpoint rong dung lam gia tri khoi tao cho form "tao moi". */
export function emptyEndpoint(): EndpointConfig {
  return {
    name: '',
    description: '',
    path: '/',
    method: 'GET',
    sequential: false,
    outputEncoding: 'json',
    steps: [emptyStep(1)],
    mappings: [],
    idempotencyEnabled: false,
    idempotencyTtlSeconds: 86400,
    parallelExecution: false,
    responseCacheEnabled: false,
    responseCacheTtlSeconds: 300,
    responseMapping: undefined,
  };
}

export function emptyStep(stepOrder: number): BackendStep {
  return {
    stepOrder,
    name: '',
    method: 'GET',
    urlPattern: '/',
    upstreamServiceId: '',
    upstreamServiceName: '',
    forwardOriginalBody: false,
    cacheEnabled: false,
    cacheTtlSeconds: 300,
    group: '',
    target: '',
    allowFields: [],
    denyFields: [],
    fieldRenameMapping: {},
    canvasX: null,
    canvasY: null,
    maxConcurrentCallsOverride: null,
    maxWaitDurationMsOverride: null,
    connectTimeoutMs: null,
    readTimeoutMs: null,
    conditionSourceType: null,
    conditionSourceStepOrder: null,
    conditionSourceField: null,
    conditionOperator: null,
    conditionExpectedValue: null,
    nextStepOrderIfTrue: null,
    nextStepOrderIfFalse: null,
    onErrorStepOrder: null,
    parallelGroup: null,
    compensationUpstreamServiceId: null,
    compensationUpstreamServiceName: null,
    compensationMethod: null,
    compensationUrlPattern: null,
  };
}

export const CONDITION_OPERATORS: ConditionOperator[] = [
  'EQUALS',
  'NOT_EQUALS',
  'EXISTS',
  'NOT_EXISTS',
  'GREATER_THAN',
  'GREATER_THAN_OR_EQUAL',
  'LESS_THAN',
  'LESS_THAN_OR_EQUAL',
  'CONTAINS',
  'NOT_CONTAINS',
  'IN',
  'NOT_IN',
  'STARTS_WITH',
  'ENDS_WITH',
  'STRING_CONTAINS',
  'IS_EMPTY',
  'IS_NOT_EMPTY',
  'MATCHES_REGEX',
  'SIZE_EQUALS',
  'SIZE_GREATER_THAN',
  'SIZE_GREATER_THAN_OR_EQUAL',
  'SIZE_LESS_THAN',
  'SIZE_LESS_THAN_OR_EQUAL',
];

/** Toan tu khong can conditionExpectedValue. */
export function operatorNeedsExpectedValue(op: ConditionOperator | null | undefined): boolean {
  return op !== 'EXISTS' && op !== 'NOT_EXISTS' && op !== 'IS_EMPTY' && op !== 'IS_NOT_EMPTY';
}

export function emptyMapping(sourceStepOrder: number, targetStepOrder: number): FieldMapping {
  return {
    sourceType: 'STEP_RESPONSE',
    sourceStepOrder,
    sourceField: '',
    sourceArrayField: '',
    sourceElementField: '',
    constantValue: '',
    targetStepOrder,
    targetType: 'QUERY',
    targetParamName: '',
    mappingOrder: 0,
    targetContext: 'MAIN',
  };
}

export function emptyUpstreamService(): UpstreamService {
  return {
    name: '',
    description: '',
    baseHost: 'http://',
    connectTimeoutMs: 1000,
    readTimeoutMs: 3000,
    circuitBreakerEnabled: true,
    failureRateThreshold: 50,
    retryEnabled: true,
    maxConcurrentCalls: 20,
    maxWaitDurationMs: 500,
  };
}

const OPERATOR_LABELS: Record<string, string> = {
  EQUALS: 'Bằng',
  NOT_EQUALS: 'Khác',
  EXISTS: 'Tồn tại',
  NOT_EXISTS: 'Không tồn tại',
  GREATER_THAN: 'Lớn hơn (>)',
  GREATER_THAN_OR_EQUAL: 'Lớn hơn hoặc bằng (>=)',
  LESS_THAN: 'Nhỏ hơn (<)',
  LESS_THAN_OR_EQUAL: 'Nhỏ hơn hoặc bằng (<=)',
  CONTAINS: 'Mảng chứa giá trị',
  NOT_CONTAINS: 'Mảng không chứa giá trị',
  IN: 'Thuộc danh sách',
  NOT_IN: 'Không thuộc danh sách',
  STARTS_WITH: 'Bắt đầu bằng',
  ENDS_WITH: 'Kết thúc bằng',
  STRING_CONTAINS: 'Chuỗi chứa',
  IS_EMPTY: 'Rỗng',
  IS_NOT_EMPTY: 'Không rỗng',
  MATCHES_REGEX: 'Khớp regex',
  SIZE_EQUALS: 'Số phần tử / độ dài bằng',
  SIZE_GREATER_THAN: 'Số phần tử / độ dài lớn hơn (>)',
  SIZE_GREATER_THAN_OR_EQUAL: 'Số phần tử / độ dài >=',
  SIZE_LESS_THAN: 'Số phần tử / độ dài nhỏ hơn (<)',
  SIZE_LESS_THAN_OR_EQUAL: 'Số phần tử / độ dài <=',
};

export function conditionOperatorLabel(op: string | null | undefined): string {
  return op ? OPERATOR_LABELS[op] ?? op : '';
}

/** Dieu kien LA moi (nguon REQUEST_BODY, toan tu EXISTS). */
export function newConditionLeaf(): ConditionRule {
  return { sourceType: 'REQUEST_BODY', sourceField: '', sourceStepOrder: null, operator: 'EXISTS', expectedValue: null };
}

/** Nut NHOM con moi (AND) chua 1 dieu kien la. */
export function newConditionSubGroup(): ConditionRule {
  return { sourceType: null, sourceField: null, operator: null, children: [newConditionLeaf()], logic: 'AND' };
}

/**
 * Chuan hoa cay dieu kien truoc khi gui backend: bo nhom/nhom con rong, chi giu cac
 * truong dung voi tung loai nut (la: nguon/field/toan tu/gia tri hoac vế phải;
 * nhom: children + logic).
 */
export function normalizeConditionTree(groups: ConditionRule[][] | null | undefined): ConditionRule[][] {
  const normalizeNodes = (nodes: ConditionRule[]): ConditionRule[] =>
    nodes
      .filter((n) => n)
      .map((n): ConditionRule | null => {
        if (n.children) {
          const children = normalizeNodes(n.children);
          if (!children.length) {
            return null;
          }
          const logic = n.logic === 'OR' ? 'OR' : 'AND';
          if (n.quantifier) {
            return {
              sourceType: n.sourceType,
              sourceField: n.sourceField,
              sourceStepOrder: n.sourceType === 'STEP_RESPONSE' ? n.sourceStepOrder ?? null : null,
              operator: null,
              expectedValue: null,
              children,
              logic,
              quantifier: n.quantifier,
              countOperator: n.quantifier === 'COUNT' ? n.countOperator ?? null : null,
              countValue: n.quantifier === 'COUNT' ? n.countValue ?? null : null,
            };
          }
          return { sourceType: null, sourceField: null, operator: null, children, logic };
        }
        const compare = n.compareSourceType != null;
        const needsValue = operatorNeedsExpectedValue(n.operator);
        return {
          sourceType: n.sourceType,
          sourceField: n.sourceField,
          sourceStepOrder: n.sourceType === 'STEP_RESPONSE' && !compare ? n.sourceStepOrder ?? null : null,
          operator: n.operator,
          expectedValue: needsValue && !compare ? n.expectedValue || null : null,
          compareSourceType: compare ? n.compareSourceType ?? null : null,
          compareSourceField: compare ? n.compareSourceField ?? '' : null,
          compareSourceStepOrder: compare && n.compareSourceType === 'STEP_RESPONSE' ? n.compareSourceStepOrder ?? null : null,
        };
      })
      .filter((n): n is ConditionRule => n !== null);

  return (groups ?? [])
    .map((g) => normalizeNodes(g ?? []))
    .filter((g) => g.length > 0);
}

/** Chuan hoa danh sach nhanh switch: giu ca nhanh rong de backend bao loi ro rang (khong bo im lang). */
export function normalizeSwitchBranches(branches: SwitchBranch[] | null | undefined): SwitchBranch[] {
  return (branches ?? []).map((b) => ({
    groups: normalizeConditionTree(b.groups),
    nextStepOrder: b.nextStepOrder ?? null,
  }));
}

export function newSwitchBranch(): SwitchBranch {
  return { groups: [[newConditionLeaf()]], nextStepOrder: null };
}

/** Nut lap mang moi: mang nguon REQUEST_BODY, quantifier ANY, dieu kien con la 1 la. */
export function newConditionQuantifier(): ConditionRule {
  return {
    sourceType: 'REQUEST_BODY',
    sourceField: '',
    sourceStepOrder: null,
    operator: null,
    children: [newConditionLeaf()],
    logic: 'AND',
    quantifier: 'ANY',
    countOperator: null,
    countValue: null,
  };
}
