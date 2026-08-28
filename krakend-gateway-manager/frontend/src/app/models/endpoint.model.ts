/**
 * Model khop 1:1 voi DTO ben Backend (com.bccs.gatewaymanager.dto.*).
 * Giu dong bo thu cong vi day la du an nho, khong dung codegen OpenAPI.
 */

export type HttpMethodType = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

/** Noi 1 gia tri duoc bom vao khi goi step sau: path/query/header, hoac 1 field trong JSON body gui di. */
export type MappingTargetType = 'PATH' | 'QUERY' | 'HEADER' | 'BODY_FIELD';

/** Nguon du lieu cua 1 FieldMapping. */
export type FieldMappingSourceType =
  | 'STEP_RESPONSE'
  | 'REQUEST_BODY'
  | 'QUERY_PARAM'
  | 'STEP_RESPONSE_ARRAY_AGGREGATE'
  | 'CONSTANT'
  | 'STEP_RESPONSE_ARRAY_MERGE';

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
  | 'LESS_THAN_OR_EQUAL';

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
   * Rẽ nhánh (P1-5) - tất cả optional, không khai báo (conditionOperator=null/undefined)
   * = step chạy bình thường theo đúng stepOrder kế tiếp, không đổi hành vi cũ.
   * Chỉ dùng STEP_RESPONSE/REQUEST_BODY (không dùng STEP_RESPONSE_ARRAY_AGGREGATE).
   */
  conditionSourceType?: FieldMappingSourceType | null;
  conditionSourceStepOrder?: number | null;
  conditionSourceField?: string | null;
  conditionOperator?: ConditionOperator | null;
  /** Chỉ dùng khi conditionOperator=EQUALS/NOT_EQUALS. */
  conditionExpectedValue?: string | null;
  /** null = nếu điều kiện ĐÚNG thì kết thúc chuỗi tại đây (kết quả step này là response cuối cùng). */
  nextStepOrderIfTrue?: number | null;
  /** null = nếu điều kiện SAI thì kết thúc chuỗi tại đây. */
  nextStepOrderIfFalse?: number | null;
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
}

/** Payload "Thu ngay" - xem EndpointTryRequestDto o backend. */
export interface EndpointTryRequest {
  pathVariables: Record<string, string>;
  queryParams: Record<string, string>;
  body: string | null;
}

/** Bundle Export/Import cau hinh - xem ConfigExportDto o backend. */
export interface ConfigExportBundle {
  schemaVersion: string;
  exportedAt: string;
  upstreams: UpstreamService[];
  endpoints: EndpointConfig[];
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
    connectTimeoutMs: null,
    readTimeoutMs: null,
    conditionSourceType: null,
    conditionSourceStepOrder: null,
    conditionSourceField: null,
    conditionOperator: null,
    conditionExpectedValue: null,
    nextStepOrderIfTrue: null,
    nextStepOrderIfFalse: null,
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
];

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
  };
}
