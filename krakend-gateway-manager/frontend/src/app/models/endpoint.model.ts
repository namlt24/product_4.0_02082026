/**
 * Model khop 1:1 voi DTO ben Backend (com.bccs.gatewaymanager.dto.*).
 * Giu dong bo thu cong vi day la du an nho, khong dung codegen OpenAPI.
 */

export type HttpMethodType = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

/** Noi field trich xuat tu step truoc se duoc bom vao step sau. */
export type MappingTargetType = 'PATH' | 'QUERY' | 'HEADER';

export interface FieldRenameMap {
  [sourceField: string]: string;
}

export interface BackendStep {
  id?: string;
  stepOrder: number;
  name: string;
  method: HttpMethodType;
  urlPattern: string;
  hosts: string[];
  group?: string | null;
  /** Ten field can "boc vo" response truoc khi mapping/allow/deny, vi du "data" (StandardResponse cua BCCS). */
  target?: string | null;
  allowFields: string[];
  denyFields: string[];
  fieldRenameMapping: FieldRenameMap;
}

/** Khai bao "trich xuat field step X -> bom vao step Y". */
export interface FieldMapping {
  id?: string;
  sourceStepOrder: number;
  sourceField: string;
  targetStepOrder: number;
  targetType: MappingTargetType;
  targetParamName: string;
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

export interface PreviewResponse {
  json: unknown;
  warnings: string[];
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

/** Thong tin chinh KrakenD gateway - dung de tu dong dien host cho Endpoint Picker. */
export interface GatewayInfo {
  port: number;
  selfBaseUrl: string;
  selfHostAliases: string[];
}

export const HTTP_METHODS: HttpMethodType[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];
export const MAPPING_TARGET_TYPES: MappingTargetType[] = ['PATH', 'QUERY', 'HEADER'];

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
    hosts: [''],
    group: '',
    target: '',
    allowFields: [],
    denyFields: [],
    fieldRenameMapping: {},
  };
}

export function emptyMapping(sourceStepOrder: number, targetStepOrder: number): FieldMapping {
  return {
    sourceStepOrder,
    sourceField: '',
    targetStepOrder,
    targetType: 'QUERY',
    targetParamName: '',
  };
}
