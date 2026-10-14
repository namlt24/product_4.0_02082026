import { CommonModule } from '@angular/common';
import { ConditionTreeEditorComponent } from '../../components/condition-tree-editor/condition-tree-editor.component';
import { FlowDiagramComponent, FlowSource } from '../../components/flow-diagram/flow-diagram.component';
import { CdkDragEnd, CdkDragMove, DragDropModule } from '@angular/cdk/drag-drop';
import { Component, ElementRef, HostListener, OnInit, ViewChild, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  BackendStep,
  CONDITION_OPERATORS,
  ConditionOperator,
  ConditionRule,
  conditionOperatorLabel,
  emptyStep,
  newConditionLeaf,
  newSwitchBranch,
  normalizeConditionTree,
  normalizeSwitchBranches,
  SwitchBranch,
  operatorNeedsExpectedValue,
  EndpointConfig,
  FieldMapping,
  FieldMappingSourceType,
  FIELD_MAPPING_SOURCE_TYPES,
  GatewayInfo,
  HttpMethodType,
  HTTP_METHODS,
  MAPPING_TARGET_CONTEXTS,
  MAPPING_TARGET_TYPES,
  MappingTargetType,
  ResponseFieldMapping,
  ResponseMappingMode,
  ResponseMappingSpec,
  TryResult,
  UpstreamService,
  extractFlagCodes,
  extractQueryParamNames,
} from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';
import { FlagApiService } from '../../services/flag-api.service';
import { FlagRule } from '../../models/flag.model';
import { TryPanelComponent, TryRunRequest } from '../../components/try-panel/try-panel.component';

const NODE_W = 220;
const NODE_H = 96;
const CLIENT_X = 40;
const CLIENT_Y = 48;
const COL_GAP = 90;
const ROW_Y = 48;
const RULE_X = 40;
const RULE_Y = 208;
const RULE_W = 220;
const RULE_H = 84;
const RESPONSE_H = 72;

/** Form nhap 1 Step trong panel truot ben phai - bien doi qua/lai voi BackendStep khi mo/luu. */
interface StepEditModel {
  name: string;
  method: HttpMethodType;
  urlPattern: string;
  upstreamServiceId: string;
  forwardOriginalBody: boolean;
  cacheEnabled: boolean;
  cacheTtlSeconds: number;
  /** Override timeout rieng cho step nay - null = dung mac dinh cua Upstream Service. */
  connectTimeoutMs: number | null;
  readTimeoutMs: number | null;
  /** Override Bulkhead rieng cho step nay - null = dung CHUNG Bulkhead cua Upstream Service. */
  maxConcurrentCallsOverride: number | null;
  maxWaitDurationMsOverride: number | null;
  group: string;
  target: string;
  allowFieldsText: string;
  denyFieldsText: string;
  renameEntries: { source: string; target: string }[];
  // Re nhanh (P1-5) - conditionOperator=null la mac dinh/tat, giu nguyen hanh vi step binh thuong.
  conditionSourceType: FieldMappingSourceType | null;
  conditionSourceStepOrder: number | null;
  conditionSourceField: string;
  conditionOperator: ConditionOperator | null;
  conditionExpectedValue: string;
  /** V5b - nhom dieu kien co khi SYSTEM_FLAG (AND trong nhom, OR giua nhom). */
  conditionFlagGroups: { conditions: { flagCode: string; expected: boolean }[] }[];
  /** V6 - nhom dieu kien nhieu nguon (REQUEST_BODY/STEP_RESPONSE), AND trong nhom, OR giua nhom. */
  conditionGroups: ConditionRule[][];
  /** Switch (nhieu nhanh): giu nguyen khi luu; canvas chua ve canh cho nhanh nay. */
  switchBranches: SwitchBranch[];
  nextStepOrderIfTrue: number | null;
  nextStepOrderIfFalse: number | null;
  /** Nhanh SAI: -1 = bao loi (dung falseErrorCode/falseErrorMessage). */
  falseErrorCode: string;
  falseErrorMessage: string;
  // Fallback khi step LOI (onErrorStepOrder) - DOC LAP voi conditionOperator o tren.
  onErrorStepOrder: number | null;
  // Wave song song trong chuoi sequential - DOC LAP voi conditionOperator/onErrorStepOrder
  // o tren, KHONG tham chieu stepOrder (chi la 1 ma nhom tuy chon).
  parallelGroup: number | null;
  // Bu tru/rollback nghiep vu (muc 6) - DOC LAP, KHONG tham chieu stepOrder.
  compensationUpstreamServiceId: string | null;
  compensationMethod: HttpMethodType | null;
  compensationUrlPattern: string;
}

/**
 * Form nhap 1 Rule cờ (V5) trong panel truot - bien doi qua/lai voi FlagRule.
 * conditions = danh sach NHOM (OR giua nhom, AND trong nhom) - UI vẽ ô vuông
 * trong khung nhom, giữa các khung ghi "HOẶC" (chốt với user 2026-10-01).
 */
interface RuleEditModel {
  name: string;
  priority: number;
  /** Moi nhom la danh sach {flagCode, expected} - cap so 2 = index nhom. */
  groups: { conditions: { flagCode: string; expected: boolean }[] }[];
  actionType: 'START_STEP' | 'BLOCK';
  startStepOrder: number | null;
  blockErrorCode: string;
  blockMessage: string;
}

interface HeaderModel {
  name: string;
  description: string;
  path: string;
  method: HttpMethodType;
  sequential: boolean;
  outputEncoding: string;
  idempotencyEnabled: boolean;
  idempotencyTtlSeconds: number;
  /** Chi co y nghia khi sequential=false - xem EndpointConfig.parallelExecution. */
  parallelExecution: boolean;
  responseCacheEnabled: boolean;
  responseCacheTtlSeconds: number;
}

interface NodePos {
  step: BackendStep;
  index: number;
  x: number;
  y: number;
}

/** Dau mut cua 1 duong noi tren canvas: stepOrder cua 1 step, hoac 'client' (node Client Request). */
type EdgeEnd = number | 'client';

/**
 * 1 DUONG NOI tren canvas = 1 cap (nguon -> dich), gom MOI FieldMapping cung cap do (xem
 * positionedMappings()). Cap chi co 1 mapping hien ten param dich; cap co >=2 hien nhan "×N".
 */
interface PositionedMapping {
  /** `${nguon}->${dich}` - dinh danh 1 cap. */
  key: string;
  /** Nguon/dich cua duong noi - dung de lam noi/lam mo duong theo node dang tap trung (xem focusKey()). */
  fromKey: EdgeEnd;
  toOrder: number;
  /** Cac FieldMapping thuoc cap nay kem chi so trong mappings() (de sua/xoa theo chi so). */
  items: { mapping: FieldMapping; index: number }[];
  path: string;
  /** Vi tri + noi dung nhan ten param (cap 1 mapping, da rut gon) - nam o khe truoc node dich de khong bi node che. */
  labelX: number;
  labelY: number;
  labelText: string;
  /** Vi tri nhan "×N" (khi >=2 mapping) - cung nam o khe truoc node dich. */
  pillX: number;
  pillY: number;
  /** Duong cang day khi cang nhieu mapping (toi da +2px) - gan nhu "do day" cua cap. */
  strokeWidth: number;
  /** Danh sach ten param dich, noi bang dau phay - hien khi re chuot vao nhan "×N". */
  tooltip: string;
}

/** Cac lop duong noi co the bat/tat tren canvas (thanh loc lop - xem layers/toggleLayer()). */
type CanvasLayer = 'mapping' | 'branch' | 'rule' | 'compensation' | 'response' | 'labels';

/** 1 mui ten re nhanh (P1-5) tren canvas - KHAC FieldMapping: khong chuyen du lieu, chi bieu dien luong dieu khien (dung/sai) tu step co dieu kien sang step ke tiep. */
interface PositionedBranch {
  fromStepIndex: number;
  /** stepOrder nguon/dich cua canh - dung de lam noi/lam mo canh theo node dang tap trung. */
  fromOrder: number;
  toOrder: number;
  outcome: 'true' | 'false' | 'error' | 'switch' | 'default';
  /** Nhan hien tren canh: Đúng/Sai/Lỗi, hoac "Nhánh n" / "Mặc định" cho switch. */
  label: string;
  path: string;
  labelX: number;
  labelY: number;
}

/**
 * 1 node Rule cờ (V5) treo DUOI day "Client Request" tren canvas - mirror
 * PositionedBranch/PositionedMapping: moi Rule co 1 duong noi TU canh phai node
 * Rule DEN canh trai step dich (START_STEP) hoac CHI co label hanh dong (BLOCK -
 * khong tro toi step nao). Rules khong keo duoc (vi tri tinh tu dong theo index).
 */
interface PositionedRule {
  rule: FlagRule;
  index: number;
  /** stepOrder dich cua duong noi (START_STEP); null neu rule BLOCK/khong co duong noi. */
  targetOrder: number | null;
  x: number;
  y: number;
  path: string;
  labelX: number;
  labelY: number;
  /** Mo ta hanh dong ngan: "→ Step 3" hoac "CHẶN GW-XXX". */
  actionLabel: string;
}

/**
 * Khung bao quanh cac step CUNG 1 "wave" song song (parallelGroup, muc 5) - KHAC moi
 * duong noi khac (khong tro toi 1 step/nguon cu the, chi bao quanh nhieu node de the
 * hien truc quan "cac step nay chay DONG THOI"). Ve o SVG layer, NAM DUOI node-layer
 * (thu tu DOM: SVG truoc, node sau -> node luon o tren, khung chi la nen).
 */
interface PositionedWaveGroup {
  groupId: number;
  x: number;
  y: number;
  width: number;
  height: number;
  labelX: number;
  labelY: number;
}

/**
 * Vong lap nho ("self-loop") hien thi cau hinh bu tru/rollback (muc 6) cua 1 step -
 * KHAC branch/mapping (khong tro toi step/nguon KHAC, vi lenh bu tru goi toi 1
 * Upstream ben ngoai do thi, khong phai 1 step trong chuoi) - ve mot vong nho o CANH
 * TRAI cua node (huong rieng, khong trung voi branch/error luon o duoi node hay
 * mapping luon o phai node) de tranh chong cheo voi cac duong noi khac.
 */
interface PositionedCompensation {
  stepIndex: number;
  stepOrder: number;
  path: string;
  labelX: number;
  labelY: number;
  label: string;
}

/**
 * 1 duong noi "ghép field vào response cuối" (ResponseMapping) - tu canh phai step
 * nguon DEN node "Response cuối" (co dinh góc phải dưới, không keo duoc). Nhom
 * mapping tu CUNG step fan ra nhieu duong song song (fan spread nhu FieldMapping).
 */
interface PositionedResponseEdge {
  rm: ResponseFieldMapping;
  index: number;
  path: string;
  labelX: number;
  labelY: number;
}

/**
 * "Canvas mới" - kéo thả trực quan để khai báo 1 Endpoint (Step + Field Mapping),
 * năng lực tương đương form "Endpoint mới" nhưng xem/thao tác trực quan qua node-graph
 * thay vì form dài. Trang RIÊNG, KHÔNG đụng endpoint-form.component.* - dùng chung
 * đúng API create()/update() nên 2 giao diện luôn ra cùng 1 nguồn dữ liệu.
 *
 * Kiến trúc render: 2 layer tách biệt (không lồng node vao <foreignObject> như
 * dependency-graph, vì node ở đây phải KÉO ĐƯỢC - lồng trong toạ độ SVG dễ lệch
 * với pixel-delta cua CDK drag):
 *   - layer SVG duoi cung: chi ve duong noi Field Mapping (Bezier cong).
 *   - layer HTML tren: moi Step la 1 div.step-node, keo qua CDK Drag (freeDragPosition
 *     mac dinh {0,0} + goi reset() sau moi lan tha - tu quan ly vi tri that qua
 *     canvasX/canvasY, KHONG de CDK tu cong don delta qua nhieu lan keo).
 *
 * Sua chi tiet 1 Step/Field Mapping = panel truot ra tu ben phai (khong phai dialog) -
 * canvas van hien/tuong tac duoc phia sau, dung quyet dinh da chot voi nguoi dung.
 */
@Component({
  selector: 'app-endpoint-canvas',
  standalone: true,
  imports: [
    CommonModule,
    ConditionTreeEditorComponent,
    FlowDiagramComponent,
    FormsModule,
    DragDropModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    TryPanelComponent,
  ],
  templateUrl: './endpoint-canvas.component.html',
  styleUrl: './endpoint-canvas.component.scss',
})
export class EndpointCanvasComponent implements OnInit {
  readonly httpMethods = HTTP_METHODS;
  readonly mappingTargetTypes = MAPPING_TARGET_TYPES;
  readonly mappingTargetContexts = MAPPING_TARGET_CONTEXTS;
  readonly sourceTypes = FIELD_MAPPING_SOURCE_TYPES;
  /** Query param cua client / hang so co dinh KHONG dung cho re nhanh (so sanh 1 hang so co dinh voi
   * chinh no vo nghia) - ngoai pham vi, chi dung cho FieldMapping thuong.
   * V5: THEM 'SYSTEM_FLAG' - dieu kien doc CỜ hệ thống (conditionSourceField = mã cờ). */
  readonly conditionSourceTypes = FIELD_MAPPING_SOURCE_TYPES.filter(
    (t) => t !== 'STEP_RESPONSE_ARRAY_AGGREGATE' && t !== 'QUERY_PARAM' && t !== 'CONSTANT' && t !== 'STEP_RESPONSE_ARRAY_MERGE',
  );

  /** V5 - Nguồn điều kiện là CỜ hệ thống: Field = select mã cờ (không phải dot-path), bỏ Step nguồn. */
  isSystemFlagCondition(): boolean {
    return this.editingStep?.conditionSourceType === 'SYSTEM_FLAG';
  }
  readonly conditionOperators = CONDITION_OPERATORS;

  readonly nodeWidth = NODE_W;
  readonly nodeHeight = NODE_H;
  readonly clientNodeX = CLIENT_X;
  readonly clientNodeY = CLIENT_Y;

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly isEditMode = signal(false);

  readonly upstreams = signal<UpstreamService[]>([]);
  readonly otherEndpoints = signal<EndpointConfig[]>([]);
  readonly gatewayInfo = signal<GatewayInfo | null>(null);

  readonly steps = signal<BackendStep[]>([]);
  readonly mappings = signal<FieldMapping[]>([]);
  /** ResponseMapping (ghép field vào response cuối) - null = không dùng. Lưu cùng endpoint. */
  readonly responseMappingSpec = signal<ResponseMappingSpec | null>(null);
  /** V5 - Rule cờ gắn vào endpoint - lưu cùng endpoint (endpoint_config.flag_rules). */
  readonly flagRules = signal<FlagRule[]>([]);
  /** Danh sách cờ của team (trang "Cờ nghiệp vụ") - chọn khi thêm điều kiện vào Rule. */
  readonly flags = signal<import('../../models/flag.model').GwmFlag[]>([]);

  /** Do lech dang keo (chua tha) cua 1 node - dung de duong noi SVG "chay theo" luc keo. */
  private readonly dragOffset = signal<{ index: number; dx: number; dy: number } | null>(null);

  // Panel truot ben phai - state don gian (khong dung Reactive Forms) vi chi 1 khoi
  // dang sua tai 1 thoi diem, khong can dong bo 2 bieu dien du lieu song song.
  editingStep: StepEditModel | null = null;
  editingStepIndex: number | null = null;
  editingMapping: FieldMapping | null = null;
  editingMappingIndex: number | null = null;
  /** Panel danh sach mapping cua 1 cap (nguon -> dich) - mo khi bam nhan "×N" tren canvas (loai tru lan nhau nhu cac panel khac). */
  editingBundle: { fromKey: EdgeEnd; toOrder: number } | null = null;
  /** Nho cap de mo LAI panel danh sach sau khi Ap dung/Xoa/Dong 1 mapping vua mo tu danh sach do. */
  private returnToBundle: { fromKey: EdgeEnd; toOrder: number } | null = null;
  /** V5 - Rule cờ dang sua trong panel (null = mo "Rule moi"). */
  editingRule: RuleEditModel | null = null;
  editingRuleIndex: number | null = null;
  /** ResponseMapping - 1 dong ghép field dang sua trong panel (null = dong/tat). */
  editingResponseMapping: ResponseFieldMapping | null = null;
  editingResponseMappingIndex: number | null = null;

  // "Thu nhanh" (xem toolbar) - dung CHUNG panel truot ben phai voi editingStep/
  // editingMapping (loai trừ lan nhau, xem panelOpen()/closePanel()) - khong tach
  // panel rieng, dung quyet dinh da chot voi nguoi dung.
  tryPanelOpen = false;
  readonly tryRunning = signal(false);
  readonly tryOutcome = signal<TryResult | null>(null);

  /** Tham chieu DOM cua panel - dung de phat hien click RA NGOAI panel (dong panel). */
  @ViewChild('panelRef') private panelRef?: ElementRef<HTMLElement>;

  header: HeaderModel = {
    name: '',
    description: '',
    path: '/',
    method: 'GET',
    sequential: false,
    outputEncoding: 'json',
    idempotencyEnabled: false,
    idempotencyTtlSeconds: 86400,
    parallelExecution: false,
    responseCacheEnabled: false,
    responseCacheTtlSeconds: 300,
  };

  private endpointId: string | null = null;

  /** Vi tri "goc" (chua tinh do lech dang keo) cua tung Step - dung cho node HTML (CDK tu ve doi voi phan keo). */
  readonly nodeBasePositions = computed<NodePos[]>(() =>
    this.steps().map((step, index) => ({ step, index, ...this.basePosition(step, index) })),
  );

  /** Vi tri THAT (co tinh do lech dang keo) - chi dung cho duong noi SVG. */
  private readonly liveNodePositions = computed<NodePos[]>(() => {
    const offset = this.dragOffset();
    return this.nodeBasePositions().map((p) =>
      offset && offset.index === p.index ? { ...p, x: p.x + offset.dx, y: p.y + offset.dy } : p,
    );
  });

  /**
   * Mapping xuat phat tu node Client (REQUEST_BODY/QUERY_PARAM/CONSTANT, hoac khong co
   * sourceStepOrder) hay tu response cua 1 step - dung chung cho gom nhom, toa do duong
   * noi va lam noi/lam mo theo node dang tap trung.
   */
  private mappingSourceKey(m: FieldMapping): EdgeEnd {
    const fromClient =
      m.sourceType === 'REQUEST_BODY' || m.sourceType === 'QUERY_PARAM' || m.sourceType === 'CONSTANT' || m.sourceStepOrder == null;
    return fromClient ? 'client' : (m.sourceStepOrder as number);
  }

  /**
   * Gop cac FieldMapping CUNG (nguon -> dich) thanh 1 duong noi duy nhat (xem
   * PositionedMapping): so duong tren canvas chi phu thuoc so CAP (nguon -> dich), khong
   * phu thuoc so mapping. Cap co >=2 mapping hien nhan "×N" bam duoc de mo panel danh
   * sach; cap chi co 1 mapping giu hanh vi cu (nhan = ten param dich, bam la sua thang
   * mapping do).
   *
   * Cac cap CUNG nguon (hoac CUNG dich) xuat phat/ket thuc tai cung 1 diem se de len nhau
   * nen duoc gian deu doc canh node (FAN_SPREAD) - o muc CAP chu khong con o muc mapping
   * (truoc day moi mapping 1 duong, 20 mapping = 20 duong chong len nhau).
   */
  readonly positionedMappings = computed<PositionedMapping[]>(() => {
    const byOrder = new Map(this.liveNodePositions().map((p) => [p.step.stepOrder, p]));

    // Gom mapping theo (nguon -> dich), giu thu tu xuat hien dau tien de ket qua on dinh.
    const groups = new Map<string, { fromKey: EdgeEnd; toOrder: number; items: { mapping: FieldMapping; index: number }[] }>();
    this.mappings().forEach((mapping, index) => {
      const fromKey = this.mappingSourceKey(mapping);
      const key = `${fromKey}->${mapping.targetStepOrder}`;
      let group = groups.get(key);
      if (!group) {
        group = { fromKey, toOrder: mapping.targetStepOrder, items: [] };
        groups.set(key, group);
      }
      group.items.push({ mapping, index });
    });

    // Lane (vi tri gian) cua tung cap tai dau nguon va dau dich.
    const lanesBySource = new Map<EdgeEnd, string[]>();
    const lanesByTarget = new Map<number, string[]>();
    for (const [key, g] of groups) {
      lanesBySource.set(g.fromKey, [...(lanesBySource.get(g.fromKey) ?? []), key]);
      lanesByTarget.set(g.toOrder, [...(lanesByTarget.get(g.toOrder) ?? []), key]);
    }
    // Khoang cach giua 2 duong ke nhau tai cung 1 dau: toi da 34px (> chieu cao nhan "×N" 20px va
    // > stroke-width=16 cua .edge-hit nen nhan/vung bam cua 2 duong khong de len nhau), tu co lai
    // khi nhieu duong de cac duong van nam trong chieu cao node (nodeHeight - 24).
    const FAN_MAX = 34;
    const fanOf = (lanes: string[], key: string): number => {
      const spread = lanes.length <= 1 ? 0 : Math.min(FAN_MAX, (this.nodeHeight - 24) / (lanes.length - 1));
      return (lanes.indexOf(key) - (lanes.length - 1) / 2) * spread;
    };

    return [...groups.entries()].map(([key, g]) => {
      const fanSource = fanOf(lanesBySource.get(g.fromKey) ?? [key], key);
      const fanTarget = fanOf(lanesByTarget.get(g.toOrder) ?? [key], key);
      let x1: number;
      let y1: number;
      if (g.fromKey === 'client') {
        x1 = this.clientNodeX + this.nodeWidth;
        y1 = this.clientNodeY + this.nodeHeight / 2 + fanSource;
      } else {
        const source = byOrder.get(g.fromKey);
        x1 = source ? source.x + this.nodeWidth : this.clientNodeX + this.nodeWidth;
        y1 = (source ? source.y + this.nodeHeight / 2 : this.clientNodeY + this.nodeHeight / 2) + fanSource;
      }
      const target = byOrder.get(g.toOrder);
      const x2 = target ? target.x : x1;
      const y2 = (target ? target.y + this.nodeHeight / 2 : y1) + fanTarget;
      const midX = (x1 + x2) / 2;

      // Nhan "×N" nam o khe giua cac node NGAY TRUOC node dich (x2 - COL_GAP/2): voi 2 node ke
      // nhau do chinh la giua duong cong, con duong noi xa (di sau node o giua) khong bi node che mat.
      const pill = this.pointOnCurveNearX(x1, y1, x2, y2, x2 - COL_GAP / 2);
      // Do cao nhan keo ~70% ve do cao DIEM DEN (y2, da gian deu theo lane): gan cuoi duong cong y gan nhu
      // = y2 nen nhan van nam tren duong, nhung khoang cach giua cac nhan cung dich = dung khoang gian
      // lane (khong bi dau nguon khac nhau "keo" lai gan nhau - truoc day 2 nhan chi cach nhau ~19px).
      const pillY = pill.y * 0.3 + y2 * 0.7;
      // Cap chi co 1 mapping: nhan ten param cung dat o khe truoc node dich (khong o giua duong cong - giua
      // duong noi xa thuong nam SAU 1 node o giua nen bi node che, bam khong duoc). Ten dai rut gon de vua
      // khe (COL_GAP=90px ~ 12 ky tu mono 10.5px), ten day du trong tooltip.
      const paramName = g.items[0].mapping.targetParamName ?? '';
      const labelText = paramName.length > 12 ? paramName.slice(0, 11) + '…' : paramName;

      return {
        key,
        fromKey: g.fromKey,
        toOrder: g.toOrder,
        items: g.items,
        path: `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`,
        labelX: pill.x,
        labelY: pillY - 8,
        labelText,
        pillX: pill.x,
        pillY,
        strokeWidth: 2.25 + Math.min(g.items.length - 1, 4) * 0.5,
        tooltip: g.items.map((it) => it.mapping.targetParamName).join(', '),
      };
    });
  });

  /** Diem tren duong cong Bezier (x1,y1)->(x2,y2) (2 diem dieu khien o midX) co x gan targetX nhat. */
  private pointOnCurveNearX(x1: number, y1: number, x2: number, y2: number, targetX: number): { x: number; y: number } {
    const midX = (x1 + x2) / 2;
    let best = { x: midX, y: (y1 + y2) / 2 };
    let bestDist = Infinity;
    for (let i = 0; i <= 50; i++) {
      const t = i / 50;
      const u = 1 - t;
      const x = u * u * u * x1 + 3 * u * u * t * midX + 3 * u * t * t * midX + t * t * t * x2;
      const y = u * u * u * y1 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t * t * t * y2;
      const dist = Math.abs(x - targetX);
      if (dist < bestDist) {
        bestDist = dist;
        best = { x, y };
      }
    }
    return best;
  }

  /**
   * Mui ten re nhanh (P1-5) + fallback loi (P-3, onErrorStepOrder) - RIENG voi
   * positionedMappings (FieldMapping): 1 step co conditionOperator co the tro toi 2 step
   * khac (nextStepOrderIfTrue/False), VA/HOAC co onErrorStepOrder (DOC LAP voi
   * conditionOperator - step khong dieu kien van co the co fallback loi rieng) - khong
   * qua FieldMapping nen truoc day KHONG duoc ve gi tren canvas (chi co badge tren node
   * nguon) - de lo hoan toan quan he giua cac step. Ve dang cung vong xuong duoi hang
   * (khac kieu Bezier ngang cua FieldMapping) de khong lan lon voi duong FieldMapping
   * giua CUNG 2 node do.
   */
  branchColor(outcome: PositionedBranch['outcome']): string {
    switch (outcome) {
      case 'true': return '#16a34a';
      case 'false': return '#dc2626';
      case 'switch': return '#7c3aed';
      case 'default': return '#64748b';
      default: return '#d97706';
    }
  }

  branchMarker(outcome: PositionedBranch['outcome']): string {
    return 'branch-arrow-' + (outcome === 'true' || outcome === 'false' || outcome === 'switch' || outcome === 'default' ? outcome : 'error');
  }

  readonly positionedBranches = computed<PositionedBranch[]>(() => {
    const positions = this.liveNodePositions();
    const byOrder = new Map(positions.map((p) => [p.step.stepOrder, p]));
    const branches: PositionedBranch[] = [];
    const DIP = 70;

    // Moi canh cua cung 1 node xuat phat xep o do cao khac nhau (DIP + lane*LANE_GAP) de nhan khong de chong len nhau.
    const LANE_GAP = 22;
    const addBranch = (p: NodePos, fromStepIndex: number, outcome: PositionedBranch['outcome'], label: string,
                       targetOrder: number | null | undefined, lane: number) => {
      if (targetOrder == null) {
        return;
      }
      const target = byOrder.get(targetOrder);
      if (!target) {
        return;
      }
      const dip = DIP + lane * LANE_GAP;
      const x1 = p.x + this.nodeWidth / 2;
      const y1 = p.y + this.nodeHeight;
      const x2 = target.x + this.nodeWidth / 2;
      const y2 = target.y + this.nodeHeight;
      branches.push({
        fromStepIndex,
        fromOrder: p.step.stepOrder,
        toOrder: target.step.stepOrder,
        outcome,
        label,
        path: `M ${x1} ${y1} C ${x1} ${y1 + dip}, ${x2} ${y2 + dip}, ${x2} ${y2}`,
        labelX: (x1 + x2) / 2,
        labelY: Math.max(y1, y2) + dip + 4,
      });
    };

    positions.forEach((p, fromStepIndex) => {
      let lane = 0;
      if (p.step.conditionOperator) {
        addBranch(p, fromStepIndex, 'true', 'Đúng', p.step.nextStepOrderIfTrue, lane++);
        addBranch(p, fromStepIndex, 'false', 'Sai', p.step.nextStepOrderIfFalse, lane++);
      }
      // Switch (nhieu nhanh): moi nhanh 1 canh rieng; nextStepOrderIfFalse la canh "Mac dinh".
      (p.step.switchBranches ?? []).forEach((branch, bi) => {
        addBranch(p, fromStepIndex, 'switch', `Nhánh ${bi + 1}`, branch.nextStepOrder, lane++);
      });
      if (p.step.switchBranches?.length) {
        addBranch(p, fromStepIndex, 'default', 'Mặc định', p.step.nextStepOrderIfFalse, lane++);
      }
      // onErrorStepOrder DOC LAP voi conditionOperator - ve rieng du step co dieu kien hay khong.
      addBranch(p, fromStepIndex, 'error', 'Lỗi', p.step.onErrorStepOrder, lane++);
    });
    return branches;
  });

  /**
   * Khung bao quanh "wave" song song (parallelGroup, muc 5) - CHI ve khi nhom co >=2
   * thanh vien DANG TON TAI trong steps hien tai (1 thanh vien don le khong can khung,
   * badge tren node da du ro nghia). Padding co dinh quanh bounding box cua toa do
   * THAT (lien tuc theo drag) cua tat ca thanh vien - luon bao dung du nut du chung
   * co nam lien ke (mac dinh) hay da bi keo tay ra vi tri khac nhau.
   */
  readonly positionedWaveGroups = computed<PositionedWaveGroup[]>(() => {
    const positions = this.liveNodePositions();
    const byGroup = new Map<number, NodePos[]>();
    for (const p of positions) {
      const g = p.step.parallelGroup;
      if (g == null) continue;
      const list = byGroup.get(g) ?? [];
      list.push(p);
      byGroup.set(g, list);
    }
    const PAD = 18;
    const groups: PositionedWaveGroup[] = [];
    for (const [groupId, members] of byGroup) {
      if (members.length < 2) continue;
      const minX = Math.min(...members.map((m) => m.x)) - PAD;
      const minY = Math.min(...members.map((m) => m.y)) - PAD;
      const maxX = Math.max(...members.map((m) => m.x + this.nodeWidth)) + PAD;
      const maxY = Math.max(...members.map((m) => m.y + this.nodeHeight)) + PAD;
      groups.push({ groupId, x: minX, y: minY, width: maxX - minX, height: maxY - minY, labelX: minX + 10, labelY: minY + 16 });
    }
    return groups;
  });

  /**
   * Vong lap nho ("self-loop") o CANH TRAI cua node hien thi cau hinh bu tru/rollback
   * (muc 6) - xem javadoc PositionedCompensation. Kich thuoc co dinh (26px) nho hon
   * DIP cua branch/error (70px) VA nam o huong KHAC (trai thay vi duoi) de khong
   * chong lan du 1 step co CA HAI (hoan toan hop le - onErrorStepOrder xu ly loi CUA
   * CHINH step nay, compensation xu ly khi step nay THANH CONG nhung chuoi SAU DO loi).
   */
  readonly positionedCompensations = computed<PositionedCompensation[]>(() => {
    const LOOP = 26;
    return this.liveNodePositions()
      .filter((p) => p.step.compensationUpstreamServiceId)
      .map((p) => {
        const x = p.x;
        const y1 = p.y + this.nodeHeight * 0.32;
        const y2 = p.y + this.nodeHeight * 0.68;
        return {
          stepIndex: p.index,
          stepOrder: p.step.stepOrder,
          path: `M ${x} ${y1} C ${x - LOOP} ${y1}, ${x - LOOP} ${y2}, ${x} ${y2}`,
          labelX: x - LOOP - 6,
          labelY: (y1 + y2) / 2 + 3,
          label: p.step.compensationMethod ?? '?',
        };
      });
  });

  /**
   * Node Rule cờ (V5) - xep DOC KOI tren 1 cot ben TRAI day node step (canh
   * Client Request), duong noi tu node Rule den step dich (START_STEP).
   * BLOCK khong co duong noi (chi label hanh dong tren node).
   */
  readonly positionedRules = computed<PositionedRule[]>(() => {
    const positions = this.liveNodePositions();
    const byOrder = new Map(positions.map((p) => [p.step.stepOrder, p]));
    return this.flagRules()
      .map((rule, index) => {
        const x = RULE_X;
        const y = RULE_Y + index * (RULE_H + 24);
        let path = '';
        let labelX = x + RULE_W;
        let labelY = y + RULE_H / 2 - 6;
        let actionLabel = '';
        if (rule.actionType === 'START_STEP' && rule.startStepOrder != null) {
          const target = byOrder.get(rule.startStepOrder);
          if (target) {
            const x1 = x + RULE_W;
            const y1 = y + RULE_H / 2;
            const x2 = target.x;
            const y2 = target.y + this.nodeHeight / 2;
            const midX = (x1 + x2) / 2;
            path = `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`;
            labelX = midX;
            labelY = (y1 + y2) / 2 - 6;
          }
          actionLabel = `→ Step ${rule.startStepOrder}`;
        } else if (rule.actionType === 'BLOCK') {
          actionLabel = `CHẶN ${rule.blockErrorCode ?? ''}`;
        }
        const targetOrder = path && rule.startStepOrder != null ? rule.startStepOrder : null;
        return { rule, index, targetOrder, x, y, path, labelX, labelY, actionLabel };
      });
  });

  /**
   * Node "Response cuối" (ResponseMapping) - co dinh nam DUOI day step cuoi cung
   * (trung tam ngang theo day node), xep duoi khoang cach co dinh. KHONG keo duoc -
   * vi tri tinh tu dong theo day node step (vi tri drag cua step co the lam node
   * nay doi cho, trung muc tieu "response cuoi do engine tong hop tu cac step").
   */
  readonly responseNodePos = computed(() => {
    const positions = this.nodeBasePositions();
    let lastX = this.clientNodeX;
    let lastY = this.clientNodeY;
    if (positions.length > 0) {
      const last = positions[positions.length - 1];
      lastX = last.x;
      lastY = last.y;
    }
    // Nam duoi day node step 1 khoang 2 hang - tranh chong voi duong branch (DIP 70px).
    const x = lastX;
    const y = lastY + this.nodeHeight + 170;
    return { x, y };
  });

  /** Vi tri THAT cua node Response cuoi (chay theo drag cua step cuoi qua liveNodePositions). */
  private readonly liveResponseNodePos = computed(() => {
    const positions = this.liveNodePositions();
    if (positions.length === 0) {
      return this.responseNodePos();
    }
    const last = positions[positions.length - 1];
    return { x: last.x, y: last.y + this.nodeHeight + 170 };
  });

  /**
   * Duong noi ResponseMapping: tu canh phai step nguon -> canh trai node "Response cuối".
   * Nhom mapping tu cung step fan ra nhieu duong song song (20px nhu FieldMapping).
   */
  readonly positionedResponseEdges = computed<PositionedResponseEdge[]>(() => {
    const spec = this.responseMappingSpec();
    if (!spec || spec.mappings.length === 0) {
      return [];
    }
    const byOrder = new Map(this.liveNodePositions().map((p) => [p.step.stepOrder, p]));
    const groupIndex = new Map<number, number>();
    const groupSize = new Map<number, number>();
    for (const rm of spec.mappings) {
      groupSize.set(rm.sourceStepOrder, (groupSize.get(rm.sourceStepOrder) ?? 0) + 1);
    }
    const FAN_SPREAD = 20;
    return spec.mappings.map((rm, index) => {
      const posInGroup = groupIndex.get(rm.sourceStepOrder) ?? 0;
      groupIndex.set(rm.sourceStepOrder, posInGroup + 1);
      const total = groupSize.get(rm.sourceStepOrder) ?? 1;
      const fanOffset = (posInGroup - (total - 1) / 2) * FAN_SPREAD;
      const source = byOrder.get(rm.sourceStepOrder);
      const x1 = source ? source.x + this.nodeWidth : this.clientNodeX + this.nodeWidth;
      const y1 = (source ? source.y + this.nodeHeight / 2 : this.clientNodeY + this.nodeHeight / 2) + fanOffset;
      const x2 = this.liveResponseNodePos().x;
      const y2 = this.liveResponseNodePos().y + RESPONSE_H / 2 + fanOffset;
      const midX = (x1 + x2) / 2;
      return {
        rm,
        index,
        path: `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`,
        labelX: midX,
        labelY: (y1 + y2) / 2 - 6,
      };
    });
  });

  readonly canvasSize = computed(() => {
    let maxX = this.clientNodeX + this.nodeWidth + 260;
    let maxY = this.clientNodeY + this.nodeHeight + 160;
    for (const p of this.nodeBasePositions()) {
      maxX = Math.max(maxX, p.x + this.nodeWidth + 100);
      maxY = Math.max(maxY, p.y + this.nodeHeight + 160);
    }
    // Rule cờ (V5) nam doc koi cot trai - keo dai canvas theo so Rule dang co.
    for (const r of this.positionedRules()) {
      maxY = Math.max(maxY, r.y + RULE_H + 60);
    }
    // Node "Response cuối" (ResponseMapping) nam duoi day step - keo dai canvas cho du.
    const resp = this.responseNodePos();
    maxY = Math.max(maxY, resp.y + RESPONSE_H + 80);
    return { width: maxX, height: maxY };
  });

  // ------------------------------------------------------------------ //
  // Giam roi canvas: lam noi duong noi theo node dang tap trung + bo loc lop
  // ------------------------------------------------------------------ //

  /** Node dang duoc re chuot vao (step -> stepOrder, node Client -> 'client'); null = khong co. */
  hoverFocus: EdgeEnd | null = null;

  /** Lop nao dang hien. 'labels' tat -> chi hien nhan cua cac duong dang noi (khi co node tap trung). */
  readonly layers = signal<Record<CanvasLayer, boolean>>({
    mapping: true,
    branch: true,
    rule: true,
    compensation: true,
    response: true,
    labels: true,
  });

  /** So duong tung lop dang co - thanh loc chi hien chip cho lop co du lieu. */
  readonly layerCounts = computed(() => ({
    // Dem MAPPING (khong phai so duong da gop) - con so nguoi dung that su quan tam.
    mapping: this.mappings().length,
    branch: this.positionedBranches().length,
    rule: this.positionedRules().filter((r) => r.path).length,
    compensation: this.positionedCompensations().length,
    response: this.positionedResponseEdges().length,
  }));

  toggleLayer(layer: CanvasLayer): void {
    this.layers.update((l) => ({ ...l, [layer]: !l[layer] }));
  }

  /**
   * Node dang tap trung: node dang re chuot, neu khong co thi step dang mo trong panel
   * sua. Method thuong (KHONG computed) vi doc field thuong editingStepIndex - cung ly
   * do voi panelOpen() ben duoi.
   */
  focusKey(): EdgeEnd | null {
    if (this.hoverFocus !== null) {
      return this.hoverFocus;
    }
    if (this.editingStepIndex !== null) {
      return this.steps()[this.editingStepIndex]?.stepOrder ?? null;
    }
    return null;
  }

  /** true = co node dang tap trung VA duong noi nay khong di qua node do -> ve mo. */
  isEdgeDim(from: EdgeEnd | null, to: EdgeEnd | null): boolean {
    const focus = this.focusKey();
    return focus !== null && focus !== from && focus !== to;
  }

  /**
   * Nhu isEdgeDim, them: khi KHONG co node tap trung ma dang sua 1 mapping hoac dang mo
   * danh sach 1 cap -> chi duong chua mapping/cap do noi, cac duong khac mo.
   */
  isMappingDim(pe: PositionedMapping): boolean {
    if (this.focusKey() === null) {
      if (this.editingMapping !== null && this.editingMappingIndex !== null) {
        const editing = this.editingMappingIndex;
        return !pe.items.some((it) => it.index === editing);
      }
      if (this.editingBundle !== null) {
        return pe.fromKey !== this.editingBundle.fromKey || pe.toOrder !== this.editingBundle.toOrder;
      }
    }
    return this.isEdgeDim(pe.fromKey, pe.toOrder);
  }

  /** Lop "Nhan" dang hien: bat, hoac tat nhung dang co node tap trung (khi do chi duong dang noi moi hien nhan). */
  labelsShown(): boolean {
    return this.layers().labels || this.focusKey() !== null;
  }

  /** Nhan chu cua 1 duong noi: luon an khi duong dang mo; tat lop "Nhan" thi chi hien khi co node tap trung. */
  labelVisible(dim: boolean): boolean {
    return !dim && this.labelsShown();
  }

  /**
   * QUAN TRONG: KHONG duoc dung computed() o day - editingStep/editingMapping la
   * field thuong (khong phai signal), computed() chi re-evaluate khi 1 SIGNAL no
   * doc thay doi. Doc field thuong ben trong computed() khong tao dependency nao
   * ca -> computed() cache DUY NHAT gia tri lan dau (luon la false, vi luc component
   * khoi tao ca 2 field deu null) va KHONG BAO GIO tinh lai nua, du sau do click mo
   * step/mapping that su gan gia tri moi cho editingStep/editingMapping - panel se
   * render noi dung trong DOM nhung class "side-panel--open" khong bao gio duoc gan,
   * nen panel nam ngoai man hinh vinh vien (xem CSS transform: translateX(100%)).
   * Dung method thuong: Angular (zone.js, change detection mac dinh - component nay
   * khong dung OnPush) tu goi lai moi lan CD chay, luon tra ve gia tri dung hien tai.
   */
  panelOpen(): boolean {
    return (
      this.editingStep !== null ||
      this.editingMapping !== null ||
      this.editingBundle !== null ||
      this.editingRule !== null ||
      this.editingResponseMapping !== null ||
      this.tryPanelOpen
    );
  }

  /**
   * Bam ra ngoai panel -> dong panel (khong lam mat thay doi chua "Ap dung" - dong
   * y het nut "Dong", chi la trigger khac). Dung 'mousedown' (KHONG phai 'click'):
   * mousedown luon xay ra TRUOC 'click' cua chinh target vua bam, nen neu nguoi dung
   * bam thang sang 1 node/canh khac de mo panel MOI, panel cu bi dong o day truoc,
   * roi (click)="openStepPanel(...)" cua node do moi chay va mo panel moi - khong bi
   * dong ngay sau khi vua mo (neu dung 'click' o day se bi loi nay vi 'click' tren
   * document luon no SAU 'click' tren chinh phan tu duoc bam, do bubbling).
   */
  @HostListener('document:mousedown', ['$event'])
  onDocumentMouseDown(event: MouseEvent): void {
    if (!this.panelOpen()) return;
    const target = event.target as HTMLElement | null;
    if (!target) return;
    if (this.panelRef?.nativeElement.contains(target)) return;
    // <mat-select>/tooltip cua Angular Material render dropdown qua CDK Overlay,
    // gan THANG vao <body> (ngoai cay DOM cua panel) - phai bo qua, khong thi chon
    // 1 option trong dropdown se vo tinh dong panel ngay lap tuc.
    if (target.closest('.cdk-overlay-container')) return;
    this.closePanel();
  }

  constructor(
    private readonly api: EndpointApiService,
    private readonly flagApi: FlagApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.api.getGatewayInfo().subscribe({ next: (info) => this.gatewayInfo.set(info) });
    this.api.listUpstreams().subscribe({ next: (list) => this.upstreams.set(list) });
    // V5 - danh sach cờ cua team (chọn khi them dieu kien vao Rule cờ).
    this.flagApi.list().subscribe({
      next: (list) => this.flags.set(list),
      error: () => this.flags.set([]),
    });

    this.endpointId = this.route.snapshot.paramMap.get('id');
    this.loadOtherEndpoints();

    if (this.endpointId) {
      this.isEditMode.set(true);
      this.loading.set(true);
      this.api.get(this.endpointId).subscribe({
        next: (ep) => {
          this.header = {
            name: ep.name,
            description: ep.description ?? '',
            path: ep.path,
            method: ep.method,
            sequential: ep.sequential,
            outputEncoding: ep.outputEncoding || 'json',
            idempotencyEnabled: ep.idempotencyEnabled ?? false,
            idempotencyTtlSeconds: ep.idempotencyTtlSeconds ?? 86400,
            parallelExecution: ep.parallelExecution ?? false,
            responseCacheEnabled: ep.responseCacheEnabled ?? false,
            responseCacheTtlSeconds: ep.responseCacheTtlSeconds ?? 300,
          };
          this.steps.set([...ep.steps].sort((a, b) => a.stepOrder - b.stepOrder));
          this.mappings.set([...ep.mappings]);
          this.responseMappingSpec.set(ep.responseMapping ?? null);
          this.flagRules.set([...(ep.flagRules ?? [])].sort((a, b) => a.priority - b.priority));
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.snackBar.open('Không tải được endpoint.', 'Đóng', { duration: 3000 });
        },
      });
    } else {
      this.steps.set([emptyStep(1)]);
    }
  }

  private loadOtherEndpoints(): void {
    this.api.list().subscribe({
      next: (all) => this.otherEndpoints.set(all.filter((e) => e.id !== this.endpointId)),
      error: () => this.otherEndpoints.set([]),
    });
  }

  /** Vi tri "goc" cua 1 Step: da tung keo tay (canvasX/Y != null) -> giu nguyen; chua tung -> tu xep 1 hang ngang theo stepOrder. */
  private basePosition(step: BackendStep, index: number): { x: number; y: number } {
    if (step.canvasX != null && step.canvasY != null) {
      return { x: step.canvasX, y: step.canvasY };
    }
    return { x: this.clientNodeX + this.nodeWidth + COL_GAP + index * (this.nodeWidth + COL_GAP), y: this.clientNodeY + ROW_Y };
  }

  // ------------------------------------------------------------------ //
  // Keo tha node
  // ------------------------------------------------------------------ //

  onNodeDragMoved(event: CdkDragMove, index: number): void {
    this.dragOffset.set({ index, dx: event.distance.x, dy: event.distance.y });
  }

  onNodeDragEnded(event: CdkDragEnd, index: number): void {
    const base = this.basePosition(this.steps()[index], index);
    const list = [...this.steps()];
    list[index] = {
      ...list[index],
      canvasX: Math.round(base.x + event.distance.x),
      canvasY: Math.round(base.y + event.distance.y),
    };
    this.steps.set(list);
    this.dragOffset.set(null);
    // Da tu quan ly vi tri that qua canvasX/canvasY - reset CDK ve 0,0 de lan keo
    // sau khong bi cong don delta cu (tranh lech kep giua CDK va state cua ta).
    event.source.reset();
  }

  // ------------------------------------------------------------------ //
  // Panel Step
  // ------------------------------------------------------------------ //

  openStepPanel(index: number): void {
    this.closePanel();
    const s = this.steps()[index];
    this.editingStepIndex = index;
    this.editingStep = {
      name: s.name,
      method: s.method,
      urlPattern: s.urlPattern,
      upstreamServiceId: s.upstreamServiceId,
      forwardOriginalBody: s.forwardOriginalBody ?? false,
      cacheEnabled: s.cacheEnabled ?? false,
      cacheTtlSeconds: s.cacheTtlSeconds ?? 300,
      connectTimeoutMs: s.connectTimeoutMs ?? null,
      readTimeoutMs: s.readTimeoutMs ?? null,
      maxConcurrentCallsOverride: s.maxConcurrentCallsOverride ?? null,
      maxWaitDurationMsOverride: s.maxWaitDurationMsOverride ?? null,
      group: s.group ?? '',
      target: s.target ?? '',
      allowFieldsText: s.allowFields.join(', '),
      denyFieldsText: s.denyFields.join(', '),
      renameEntries: Object.entries(s.fieldRenameMapping ?? {}).map(([source, target]) => ({ source, target })),
      conditionSourceType: s.conditionSourceType ?? 'STEP_RESPONSE',
      conditionSourceStepOrder: s.conditionSourceStepOrder ?? null,
      conditionSourceField: s.conditionSourceField ?? '',
      conditionOperator: s.conditionOperator ?? null,
      conditionExpectedValue: s.conditionExpectedValue ?? '',
      conditionFlagGroups: (s.conditionFlagGroups ?? []).map((g) => ({
        conditions: (g ?? []).map((c) => ({ flagCode: c.flagCode, expected: c.expected })),
      })),
      conditionGroups: structuredClone(s.conditionGroups ?? []),
      switchBranches: structuredClone(s.switchBranches ?? []),
      nextStepOrderIfTrue: s.nextStepOrderIfTrue ?? null,
      nextStepOrderIfFalse: s.falseErrorCode ? -1 : s.nextStepOrderIfFalse ?? null,
      falseErrorCode: s.falseErrorCode ?? 'GW-CONDITION-FALSE',
      falseErrorMessage: s.falseErrorMessage ?? '',
      onErrorStepOrder: s.onErrorStepOrder ?? null,
      parallelGroup: s.parallelGroup ?? null,
      compensationUpstreamServiceId: s.compensationUpstreamServiceId ?? null,
      compensationMethod: s.compensationMethod ?? null,
      compensationUrlPattern: s.compensationUrlPattern ?? '',
    };
  }

  // ------------------------------------------------------------------ //
  // Dieu kien re nhanh (P1-5) - dung y het endpoint-form.component.ts (trung
  // lap co chu dich, khong dung chung component voi form - xem ly do da ghi
  // trong plan canvas ban dau).
  // ------------------------------------------------------------------ //

  stepConditionEnabled(): boolean {
    return !!this.editingStep?.conditionOperator;
  }

  toggleStepCondition(enabled: boolean): void {
    if (!this.editingStep) return;
    if (enabled) {
      this.editingStep.conditionSourceType = 'STEP_RESPONSE';
      this.editingStep.conditionOperator = 'EXISTS';
    } else {
      this.editingStep.conditionSourceType = null;
      this.editingStep.conditionSourceStepOrder = null;
      this.editingStep.conditionSourceField = '';
      this.editingStep.conditionOperator = null;
      this.editingStep.conditionExpectedValue = '';
      this.editingStep.conditionFlagGroups = [];
      this.editingStep.conditionGroups = [];
      this.editingStep.nextStepOrderIfTrue = null;
      this.editingStep.nextStepOrderIfFalse = null;
    }
  }

  /** V5 - Đổi nguồn điều kiện: chọn Cờ hệ thống thì bỏ Step nguồn (validate backend không dùng),
   * operator mặc định EQUALS và giá trị mong đợi "true" cho đúng ngữ nghĩa boolean,
   * khởi tạo sẵn 1 nhóm 1 điều kiện cờ (editor nhóm V5b). */
  onConditionSourceTypeChange(type: string): void {
    if (!this.editingStep) return;
    if (type === 'SYSTEM_FLAG') {
      this.editingStep.conditionSourceStepOrder = null;
      this.editingStep.conditionOperator = 'EQUALS';
      this.editingStep.conditionExpectedValue = 'true';
      if (!this.editingStep.conditionFlagGroups.length) {
        this.editingStep.conditionFlagGroups = [{ conditions: [{ flagCode: '', expected: true }] }];
      }
    } else if (type === 'REQUEST_BODY') {
      this.editingStep.conditionSourceStepOrder = null;
    } else if (!this.editingStep.conditionOperator) {
      this.editingStep.conditionOperator = 'EXISTS';
    }
  }

  // ------------------------------------------------------------------ //
  // V5b - Nhom dieu kien cờ tại step (AND trong nhom, OR giua nhom),
  // mirror RuleEditModel groups của Rule cờ.
  // ------------------------------------------------------------------ //

  addFlagCondition(groupIndex: number): void {
    this.editingStep?.conditionFlagGroups[groupIndex]?.conditions.push({ flagCode: '', expected: true });
  }

  removeFlagCondition(groupIndex: number, condIndex: number): void {
    const group = this.editingStep?.conditionFlagGroups[groupIndex];
    if (!group) return;
    group.conditions.splice(condIndex, 1);
    if (!group.conditions.length) {
      this.editingStep!.conditionFlagGroups.splice(groupIndex, 1);
    }
  }

  addFlagGroup(): void {
    this.editingStep?.conditionFlagGroups.push({ conditions: [{ flagCode: '', expected: true }] });
  }

  removeFlagGroup(groupIndex: number): void {
    this.editingStep?.conditionFlagGroups.splice(groupIndex, 1);
  }

  // ------------------------------------------------------------------ //
  // V6 - Nhom dieu kien nhieu nguon (REQUEST_BODY/STEP_RESPONSE), AND trong
  // nhom, OR giua nhom. Mirror endpoint-form.component.ts (xem conditionGroupsEnabled).
  // ------------------------------------------------------------------ //

  /** Switch (nhieu nhanh) tren panel canvas - cung hanh vi voi form (xem endpoint-form). */
  switchEnabled(): boolean {
    return (this.editingStep?.switchBranches.length ?? 0) > 0;
  }

  toggleSwitch(enabled: boolean): void {
    if (!this.editingStep) return;
    this.editingStep.switchBranches = enabled ? [newSwitchBranch()] : [];
  }

  addSwitchBranch(): void {
    this.editingStep?.switchBranches.push(newSwitchBranch());
  }

  removeSwitchBranch(branchIndex: number): void {
    this.editingStep?.switchBranches.splice(branchIndex, 1);
  }

  conditionGroupsEnabled(): boolean {
    return (this.editingStep?.conditionGroups.length ?? 0) > 0;
  }

  toggleConditionGroups(enabled: boolean): void {
    if (!this.editingStep) return;
    this.editingStep.conditionGroups = enabled ? [[newConditionLeaf()]] : [];
  }

  operatorLabel(op: string): string {
    return conditionOperatorLabel(op);
  }

  stepConditionNeedsSourceStep(): boolean {
    const t = this.editingStep?.conditionSourceType;
    return t !== 'REQUEST_BODY' && t !== 'SYSTEM_FLAG';
  }

  stepConditionNeedsExpectedValue(): boolean {
    return operatorNeedsExpectedValue(this.editingStep?.conditionOperator);
  }

  /** 4 toan tu so sanh SO (>,>=,<,<=) - doi input sang type="number" cho de nhap, validate chac chan van o backend. */
  stepConditionExpectedValueIsNumeric(): boolean {
    const op = this.editingStep?.conditionOperator;
    return op === 'GREATER_THAN' || op === 'GREATER_THAN_OR_EQUAL' || op === 'LESS_THAN' || op === 'LESS_THAN_OR_EQUAL';
  }

  /**
   * Dieu chinh dieu kien re nhanh cua 1 step CON LAI sau khi xoa step co stepOrder
   * removedOrder - dung y het cach lam voi Field Mapping (removeStepFromPanel):
   * tham chieu toi step VUA XOA thi tat dieu kien luon (khong con nghia gi ca),
   * tham chieu toi step con lai thi dich stepOrder xuong 1.
   */
  private reindexStepCondition(step: BackendStep, removedOrder: number): BackendStep {
    const shift = (v: number | null | undefined): number | null => {
      if (v == null) return null;
      if (v === removedOrder) return null;
      return v > removedOrder ? v - 1 : v;
    };
    // onErrorStepOrder DOC LAP voi conditionOperator - reindex/xoa rieng, ap dung cho MOI
    // nhanh return ben duoi (khac conditionSourceStepOrder chi gan voi dieu kien re nhanh).
    const onErrorStepOrder = shift(step.onErrorStepOrder);

    const srcStep = step.conditionSourceStepOrder ?? null;
    if (srcStep === removedOrder) {
      return {
        ...step,
        conditionSourceStepOrder: null,
        conditionSourceField: null,
        conditionOperator: null,
        conditionExpectedValue: null,
        nextStepOrderIfTrue: null,
        nextStepOrderIfFalse: null,
        onErrorStepOrder,
      };
    }
    return {
      ...step,
      conditionSourceStepOrder: srcStep !== null && srcStep > removedOrder ? srcStep - 1 : srcStep,
      nextStepOrderIfTrue: shift(step.nextStepOrderIfTrue),
      nextStepOrderIfFalse: shift(step.nextStepOrderIfFalse),
      onErrorStepOrder,
    };
  }

  addRenameEntry(): void {
    this.editingStep?.renameEntries.push({ source: '', target: '' });
  }

  removeRenameEntry(i: number): void {
    this.editingStep?.renameEntries.splice(i, 1);
  }

  /** Endpoint Picker - dien nhanh Upstream/URL pattern theo 1 endpoint gateway khac. Y het endpoint-form.pickEndpoint(). */
  pickEndpoint(endpointId: string): void {
    if (!this.editingStep) return;
    const target = this.otherEndpoints().find((e) => e.id === endpointId);
    const info = this.gatewayInfo();
    if (!target || !info) return;

    const selfUpstream = this.upstreams().find((u) => {
      try {
        const url = new URL(u.baseHost);
        const host = url.hostname;
        const port = url.port ? Number(url.port) : -1;
        const hostMatches = info.selfHostAliases.some((alias) => alias.toLowerCase() === host.toLowerCase());
        return hostMatches && port === info.port;
      } catch {
        return false;
      }
    });

    if (!selfUpstream) {
      this.snackBar.open(
        `Chưa có Upstream Service nào trỏ về chính gateway này (${info.selfBaseUrl}) - hãy đăng ký 1 cái trong trang "Upstream Services" trước.`,
        'Đóng',
        { duration: 5000 },
      );
      return;
    }

    this.editingStep.upstreamServiceId = selfUpstream.id!;
    this.editingStep.urlPattern = target.path;
    this.snackBar.open(`Đã điền Upstream/URL pattern theo endpoint "${target.name}".`, 'Đóng', { duration: 2500 });
  }

  applyStepPanel(): void {
    const idx = this.editingStepIndex;
    const edit = this.editingStep;
    if (idx === null || !edit) return;
    if (!edit.name.trim() || !edit.urlPattern.trim() || !edit.upstreamServiceId) {
      this.snackBar.open('Vui lòng nhập đủ Tên step, URL pattern và chọn Upstream Service.', 'Đóng', { duration: 3000 });
      return;
    }
    const upstream = this.upstreams().find((u) => u.id === edit.upstreamServiceId);
    const list = [...this.steps()];
    list[idx] = {
      ...list[idx],
      name: edit.name,
      method: edit.method,
      urlPattern: edit.urlPattern,
      upstreamServiceId: edit.upstreamServiceId,
      upstreamServiceName: upstream?.name ?? list[idx].upstreamServiceName,
      forwardOriginalBody: edit.forwardOriginalBody,
      cacheEnabled: edit.cacheEnabled,
      cacheTtlSeconds: edit.cacheTtlSeconds,
      connectTimeoutMs: edit.connectTimeoutMs || null,
      readTimeoutMs: edit.readTimeoutMs || null,
      maxConcurrentCallsOverride: edit.maxConcurrentCallsOverride || null,
      maxWaitDurationMsOverride: edit.maxWaitDurationMsOverride || null,
      group: edit.group || null,
      target: edit.target || null,
      allowFields: splitCsv(edit.allowFieldsText),
      denyFields: splitCsv(edit.denyFieldsText),
      fieldRenameMapping: Object.fromEntries(
        edit.renameEntries.filter((e) => e.source && e.target).map((e) => [e.source, e.target]),
      ),
      conditionSourceType: edit.conditionOperator ? edit.conditionSourceType : null,
      conditionSourceStepOrder: edit.conditionOperator ? edit.conditionSourceStepOrder : null,
      conditionSourceField: edit.conditionOperator ? edit.conditionSourceField || null : null,
      conditionOperator: edit.conditionOperator,
      conditionExpectedValue: edit.conditionOperator ? edit.conditionExpectedValue || null : null,
      // V5b - chi gui nhom khi nguon la SYSTEM_FLAG va co nhom hop le (co it nhat
      // 1 dieu kien da chon cờ); rong -> null (endpoint cu khong doi hanh vi).
      conditionFlagGroups:
        edit.conditionOperator && edit.conditionSourceType === 'SYSTEM_FLAG'
          ? edit.conditionFlagGroups
              .map((g) => g.conditions.filter((c) => c.flagCode))
              .filter((g) => g.length > 0)
          : null,
      // V6 - chi gui khi dang bat re nhanh. Khong loc dieu kien thieu field (backend bao loi ro rang).
      conditionGroups: edit.conditionOperator ? normalizeConditionTree(edit.conditionGroups) : null,
      switchBranches: normalizeSwitchBranches(edit.switchBranches),
      nextStepOrderIfTrue: edit.conditionOperator ? edit.nextStepOrderIfTrue : null,
      nextStepOrderIfFalse: (edit.conditionOperator || edit.switchBranches.length) && edit.nextStepOrderIfFalse !== -1 ? edit.nextStepOrderIfFalse : null,
      falseErrorCode: edit.nextStepOrderIfFalse === -1 ? edit.falseErrorCode || null : null,
      falseErrorMessage: edit.nextStepOrderIfFalse === -1 ? edit.falseErrorMessage || null : null,
      // onErrorStepOrder DOC LAP voi conditionOperator - khong gate theo dieu kien re nhanh.
      onErrorStepOrder: edit.onErrorStepOrder,
      // parallelGroup cung DOC LAP - khong tham chieu stepOrder, khong gate theo dieu kien.
      parallelGroup: edit.parallelGroup,
      // Bu tru/rollback (muc 6) - DOC LAP, khong tham chieu stepOrder, khong gate theo dieu kien.
      compensationUpstreamServiceId: edit.compensationUpstreamServiceId,
      compensationMethod: edit.compensationMethod,
      compensationUrlPattern: edit.compensationUrlPattern || null,
    };
    this.steps.set(list);
    this.closePanel();
    this.snackBar.open('Đã cập nhật Step.', 'Đóng', { duration: 2000 });
  }

  addStep(): void {
    const nextOrder = this.steps().length + 1;
    this.steps.set([...this.steps(), emptyStep(nextOrder)]);
    if (this.steps().length > 1) {
      this.header.sequential = true;
      // parallelExecution chi hop le voi sequential=false (xem GW-003 o backend) - tu
      // dong tat khi UI tu dong bat sequential, tranh nguoi dung phai tu nho tat lai
      // truoc khi luu (se bi 400 GW-003 neu quen).
      this.header.parallelExecution = false;
    }
    this.openStepPanel(this.steps().length - 1);
  }

  /** Xoa 1 Step: renumber stepOrder cac step con lai + don dep/dich chuyen cac Field Mapping tham chieu toi no. Y het endpoint-form.removeStep(). */
  removeStepFromPanel(): void {
    const idx = this.editingStepIndex;
    if (idx === null) return;
    if (this.steps().length <= 1) {
      this.snackBar.open('Phải có ít nhất 1 backend step.', 'Đóng', { duration: 2500 });
      return;
    }
    const removedOrder = this.steps()[idx].stepOrder;
    const remaining = this.steps()
      .filter((_, i) => i !== idx)
      .map((s, i) => ({ ...s, stepOrder: i + 1 }))
      .map((s) => this.reindexStepCondition(s, removedOrder));
    this.steps.set(remaining);

    const remainingMappings = this.mappings()
      .filter((m) => m.sourceStepOrder !== removedOrder && m.targetStepOrder !== removedOrder)
      .map((m) => ({
        ...m,
        sourceStepOrder: m.sourceStepOrder != null && m.sourceStepOrder > removedOrder ? m.sourceStepOrder - 1 : m.sourceStepOrder,
        targetStepOrder: m.targetStepOrder > removedOrder ? m.targetStepOrder - 1 : m.targetStepOrder,
      }));
    this.mappings.set(remainingMappings);

    // V5 - Rule cờ tham chieu stepOrder (startStepOrder): tro toi step vua xoa ->
    // xoa rule luon (khong con nghia gi); tro toi step con lai -> dich stepOrder xuong 1.
    this.flagRules.set(
      this.flagRules()
        .filter((r) => !(r.actionType === 'START_STEP' && r.startStepOrder === removedOrder))
        .map((r) => ({
          ...r,
          startStepOrder:
            r.actionType === 'START_STEP' && r.startStepOrder != null && r.startStepOrder > removedOrder
              ? r.startStepOrder - 1
              : r.startStepOrder,
        })),
    );
    this.closePanel();
  }

  // ------------------------------------------------------------------ //
  // Panel Field Mapping
  // ------------------------------------------------------------------ //

  get availableStepOrders(): number[] {
    return this.steps().map((s) => s.stepOrder);
  }

  /** fromBundle=true: mo tu panel danh sach 1 cap -> Ap dung/Xoa/Dong xong quay lai danh sach do. */
  openMappingPanel(index: number, fromBundle = false): void {
    const back = fromBundle ? this.editingBundle : null;
    this.closePanel();
    this.returnToBundle = back;
    this.editingMappingIndex = index;
    this.editingMapping = { ...this.mappings()[index] };
  }

  /** Bam vao 1 duong noi mapping: cap chi co 1 mapping -> sua thang; cap da gop nhieu mapping -> mo danh sach. */
  onMappingEdgeClick(pe: PositionedMapping): void {
    if (pe.items.length === 1) {
      this.openMappingPanel(pe.items[0].index);
    } else {
      this.openBundlePanel(pe);
    }
  }

  openBundlePanel(pe: PositionedMapping): void {
    this.closePanel();
    this.editingBundle = { fromKey: pe.fromKey, toOrder: pe.toOrder };
  }

  /** Cac mapping cua cap dang mo (tinh lai moi lan CD - chi so trong mappings() doi khi them/xoa mapping). */
  bundleRows(): { mapping: FieldMapping; index: number }[] {
    const b = this.editingBundle;
    if (!b) {
      return [];
    }
    return this.mappings()
      .map((mapping, index) => ({ mapping, index }))
      .filter((r) => this.mappingSourceKey(r.mapping) === b.fromKey && r.mapping.targetStepOrder === b.toOrder);
  }

  bundleEndLabel(end: EdgeEnd): string {
    return end === 'client' ? 'Client Request' : `Step ${end}`;
  }

  /** Mo ta ngan nguon du lieu cua 1 mapping - hien trong dong cua panel danh sach. */
  mappingSourceText(m: FieldMapping): string {
    switch (m.sourceType) {
      case 'REQUEST_BODY':
        return `body.${m.sourceField}`;
      case 'QUERY_PARAM':
        return `query.${m.sourceField}`;
      case 'CONSTANT':
        return `"${m.constantValue ?? ''}"`;
      case 'STEP_RESPONSE_ARRAY_AGGREGATE':
        return `Step ${m.sourceStepOrder}.${m.sourceArrayField}[].${m.sourceElementField}`;
      case 'STEP_RESPONSE_ARRAY_MERGE':
        return `Step ${m.sourceStepOrder}.${m.sourceArrayField}[] (gộp)`;
      default:
        return `Step ${m.sourceStepOrder}.${m.sourceField}`;
    }
  }

  /** Xoa 1 mapping ngay tu danh sach; cap het mapping thi dong panel (duong noi cung bien mat). */
  removeBundleRow(index: number): void {
    this.mappings.set(this.mappings().filter((_, i) => i !== index));
    if (this.bundleRows().length === 0) {
      this.closePanel();
    }
  }

  /** Them 1 mapping moi, dien san nguon/dich cua cap dang mo; Ap dung xong tu quay lai danh sach. */
  addMappingToBundle(): void {
    const b = this.editingBundle;
    if (!b) {
      return;
    }
    // Loai dich PHO BIEN NHAT trong cap (vd cap Client -> Step 2 chu yeu BODY_FIELD) lam mac dinh cho mapping moi.
    const typeCounts = new Map<MappingTargetType, number>();
    for (const row of this.bundleRows()) {
      typeCounts.set(row.mapping.targetType, (typeCounts.get(row.mapping.targetType) ?? 0) + 1);
    }
    const commonTargetType = [...typeCounts.entries()].sort((a, c) => c[1] - a[1])[0]?.[0];
    this.openNewMappingPanel();
    this.returnToBundle = b;
    const m = this.editingMapping;
    if (!m) {
      return;
    }
    if (b.fromKey === 'client') {
      m.sourceType = 'REQUEST_BODY';
      m.sourceStepOrder = null;
    } else {
      m.sourceType = 'STEP_RESPONSE';
      m.sourceStepOrder = b.fromKey;
    }
    m.targetStepOrder = b.toOrder;
    if (commonTargetType) {
      m.targetType = commonTargetType;
    }
  }

  /** Dong panel sua mapping; neu vua mo tu danh sach 1 cap (va cap do con mapping) thi mo lai danh sach do. */
  closeMappingPanel(): void {
    const back = this.returnToBundle;
    this.closePanel();
    if (back && this.mappings().some((m) => this.mappingSourceKey(m) === back.fromKey && m.targetStepOrder === back.toOrder)) {
      this.editingBundle = back;
    }
  }

  openNewMappingPanel(): void {
    this.closePanel();
    const orders = this.availableStepOrders;
    const target = orders.length > 1 ? orders[orders.length - 1] : orders[0] ?? 1;
    const source = orders.length > 1 ? orders[0] : 1;
    this.editingMappingIndex = null;
    this.editingMapping = {
      sourceType: 'STEP_RESPONSE',
      sourceStepOrder: source,
      sourceField: '',
      sourceArrayField: '',
      sourceElementField: '',
      constantValue: '',
      targetStepOrder: target,
      targetType: 'QUERY',
      targetParamName: '',
      mappingOrder: this.mappings().length,
      targetContext: 'MAIN',
    };
  }

  mappingNeedsSourceStep(): boolean {
    return (
      this.editingMapping?.sourceType !== 'REQUEST_BODY' &&
      this.editingMapping?.sourceType !== 'QUERY_PARAM' &&
      this.editingMapping?.sourceType !== 'CONSTANT'
    );
  }

  mappingUsesSourceField(): boolean {
    return (
      this.editingMapping?.sourceType !== 'STEP_RESPONSE_ARRAY_AGGREGATE' &&
      this.editingMapping?.sourceType !== 'CONSTANT' &&
      this.editingMapping?.sourceType !== 'STEP_RESPONSE_ARRAY_MERGE'
    );
  }

  mappingUsesArrayAggregate(): boolean {
    return this.editingMapping?.sourceType === 'STEP_RESPONSE_ARRAY_AGGREGATE';
  }

  /**
   * sourceType=STEP_RESPONSE_ARRAY_MERGE - gop TOAN BO field cua tung phan tu trong 1 mang
   * thanh 1 object duy nhat. Chi can duong dan toi mang (sourceArrayField, dung chung UI
   * voi STEP_RESPONSE_ARRAY_AGGREGATE) - KHONG can sourceElementField (lay nguyen ca
   * object cua phan tu, khong trich 1 field).
   */
  mappingUsesArrayMerge(): boolean {
    return this.editingMapping?.sourceType === 'STEP_RESPONSE_ARRAY_MERGE';
  }

  /** sourceType=CONSTANT - gia tri hang so co dinh khai bao truc tiep, khong doc tu request/response nao. */
  mappingUsesConstantValue(): boolean {
    return this.editingMapping?.sourceType === 'CONSTANT';
  }

  applyMappingPanel(): void {
    const m = this.editingMapping;
    if (!m) return;
    if (!m.targetParamName?.trim()) {
      this.snackBar.open('Vui lòng nhập tên param/token/header/field đích.', 'Đóng', { duration: 2500 });
      return;
    }
    const list = [...this.mappings()];
    if (this.editingMappingIndex === null) {
      list.push(m);
    } else {
      list[this.editingMappingIndex] = m;
    }
    this.mappings.set(list);
    this.closeMappingPanel();
  }

  removeMappingFromPanel(): void {
    if (this.editingMappingIndex !== null) {
      this.mappings.set(this.mappings().filter((_, i) => i !== this.editingMappingIndex));
    }
    this.closeMappingPanel();
  }

  // ------------------------------------------------------------------ //
  // Panel ResponseMapping (ghép field vào response cuối)
  // ------------------------------------------------------------------ //

  readonly responseMappingModes: ResponseMappingMode[] = ['MERGE', 'REPLACE'];

  /** Them 1 dong moi vao responseMappingSpec (tao spec MERGE neu chua co). */
  addResponseMapping(): void {
    const orders = this.availableStepOrders;
    const lastOrder = orders.length > 0 ? orders[orders.length - 1] : 1;
    const spec = this.responseMappingSpec() ?? { mode: 'MERGE' as ResponseMappingMode, mappings: [] };
    const mappings = [...spec.mappings];
    this.editingResponseMappingIndex = mappings.length;
    this.editingResponseMapping = {
      sourceStepOrder: lastOrder,
      sourceField: '',
      targetField: '',
      mappingOrder: mappings.length,
    };
    spec.mappings = mappings;
    this.responseMappingSpec.set(spec);
  }

  openResponseMappingPanel(index: number): void {
    this.closePanel();
    const spec = this.responseMappingSpec();
    if (!spec || !spec.mappings[index]) {
      return;
    }
    this.editingResponseMappingIndex = index;
    this.editingResponseMapping = { ...spec.mappings[index] };
  }

  applyResponseMappingPanel(): void {
    const rm = this.editingResponseMapping;
    const idx = this.editingResponseMappingIndex;
    if (!rm || idx === null) {
      return;
    }
    if (!rm.sourceField.trim() || !rm.targetField.trim()) {
      this.snackBar.open('Nhập đủ field nguồn và field response cuối.', 'Đóng', { duration: 2500 });
      return;
    }
    rm.sourceField = rm.sourceField.trim();
    rm.targetField = rm.targetField.trim();
    const spec = this.responseMappingSpec();
    if (!spec) {
      return;
    }
    const mappings = [...spec.mappings];
    mappings[idx] = rm;
    this.responseMappingSpec.set({ ...spec, mappings });
    this.closePanel();
  }

  removeResponseMappingAt(index: number): void {
    const spec = this.responseMappingSpec();
    if (!spec) {
      return;
    }
    const mappings = spec.mappings.filter((_, i) => i !== index);
    this.responseMappingSpec.set({ ...spec, mappings: mappings.length > 0 ? mappings : [] });
    this.closePanel();
  }

  /** Doi mode MERGE/REPLACE - giu nguyen danh sach mapping. */
  setResponseMappingMode(mode: ResponseMappingMode): void {
    const spec = this.responseMappingSpec();
    if (spec) {
      this.responseMappingSpec.set({ ...spec, mode });
    }
  }

  /** Tat hoan toan ResponseMapping (xoa spec) - endpoint quay ve hanh vi khong ghép field. */
  disableResponseMapping(): void {
    this.responseMappingSpec.set(null);
    this.closePanel();
  }

  closePanel(): void {
    this.editingStep = null;
    this.editingStepIndex = null;
    this.editingMapping = null;
    this.editingMappingIndex = null;
    this.editingBundle = null;
    this.returnToBundle = null;
    this.editingRule = null;
    this.editingRuleIndex = null;
    this.editingResponseMapping = null;
    this.editingResponseMappingIndex = null;
    this.tryPanelOpen = false;
  }

  /** "Thu nhanh" - xem toPayload()/runAdhocTry(). Dung chung side-panel, dong panel sua step/mapping khac neu dang mo. */
  openTryPanel(): void {
    this.closePanel();
    this.tryPanelOpen = true;
  }

  /** O nhap Query param cua panel "Thu nhanh" tu sinh theo mapping HIEN TAI dang cau hinh tren canvas (co the doi lien tuc khi anh sua mapping). */
  tryQueryParamNames(): string[] {
    return extractQueryParamNames(this.mappings());
  }

  /** V5 - Ma cờ endpoint đang dựng dùng (Rule + SYSTEM_FLAG) - sinh toggle ghi đe cho "Thu nhanh". */
  tryFlagCodes(): string[] {
    return extractFlagCodes(this.toPayload());
  }

  /**
   * Goi thang API "Thu nhanh" (khong luu DB) voi dung toPayload() hien tai -
   * tai dung 100% logic build payload da co, khong viet lai. Loi o day chi con
   * la loi GOI SAI API that su (mat mang...) vi backend gio LUON tra HTTP 200
   * (envelope TryResultDto) ke ca khi draft khong hop le/engine loi.
   */
  runAdhocTry(req: TryRunRequest): void {
    this.tryRunning.set(true);
    this.tryOutcome.set(null);
    const payload = this.toPayload();
    this.api
      .tryAdhoc({
        endpoint: payload,
        pathVariables: req.pathVariables,
        queryParams: req.queryParams,
        body: req.body,
        flagOverrides: req.flagOverrides,
      })
      .subscribe({
        next: (result) => {
          this.tryRunning.set(false);
          this.tryOutcome.set(result);
        },
        error: (err) => {
          this.tryRunning.set(false);
          this.tryOutcome.set({
            success: false,
            result: null,
            errorCode: err?.status ? `HTTP_${err.status}` : null,
            errorMessage: err?.error?.message ?? 'Không gọi được thử nhanh.',
            hops: [],
          });
        },
      });
  }

  // ------------------------------------------------------------------ //
  // Rule cờ (V5) - panel sua Rule + dieu kien ô vuông theo nhóm AND/OR
  // ------------------------------------------------------------------ //

  openNewRulePanel(): void {
    this.closePanel();
    const usedPriorities = new Set(this.flagRules().map((r) => r.priority));
    let nextPriority = 1;
    while (usedPriorities.has(nextPriority)) nextPriority++;
    this.editingRuleIndex = null;
    this.editingRule = {
      name: '',
      priority: nextPriority,
      groups: [{ conditions: [{ flagCode: '', expected: true }] }],
      actionType: 'START_STEP',
      startStepOrder: this.availableStepOrders[0] ?? 1,
      blockErrorCode: '',
      blockMessage: '',
    };
  }

  openRulePanel(index: number): void {
    this.closePanel();
    const r = this.flagRules()[index];
    this.editingRuleIndex = index;
    this.editingRule = {
      name: r.name,
      priority: r.priority,
      groups: (r.conditions ?? []).map((group) => ({
        conditions: group.map((c) => ({ flagCode: c.flagCode, expected: c.expected })),
      })),
      actionType: r.actionType,
      startStepOrder: r.startStepOrder ?? null,
      blockErrorCode: r.blockErrorCode ?? '',
      blockMessage: r.blockMessage ?? '',
    };
  }

  addRuleGroup(): void {
    this.editingRule?.groups.push({ conditions: [{ flagCode: '', expected: true }] });
  }

  removeRuleGroup(groupIndex: number): void {
    this.editingRule?.groups.splice(groupIndex, 1);
  }

  addRuleCondition(groupIndex: number): void {
    const group = this.editingRule?.groups[groupIndex];
    if (group) group.conditions.push({ flagCode: '', expected: true });
  }

  removeRuleCondition(groupIndex: number, condIndex: number): void {
    const group = this.editingRule?.groups[groupIndex];
    if (group) group.conditions.splice(condIndex, 1);
  }

  /** Các flagCode ĐANG chọn ở nhóm khác - không cho chọn trùng trong cùng 1 nhóm (backend chặn GW-003). */
  duplicateInGroup(groupIndex: number, flagCode: string, exceptIndex: number): boolean {
    const group = this.editingRule?.groups[groupIndex];
    if (!group) return false;
    return group.conditions.some((c, i) => i !== exceptIndex && c.flagCode && c.flagCode === flagCode);
  }

  applyRulePanel(): void {
    const edit = this.editingRule;
    if (!edit) return;
    if (!edit.name.trim()) {
      this.snackBar.open('Vui lòng nhập tên Rule.', 'Đóng', { duration: 2500 });
      return;
    }
    if (edit.groups.length === 0 || edit.groups.some((g) => g.conditions.length === 0)) {
      this.snackBar.open('Mỗi nhóm phải có ít nhất 1 điều kiện (nhóm rỗng luôn khớp - vô nghĩa).', 'Đóng', { duration: 3000 });
      return;
    }
    if (edit.groups.some((g) => g.conditions.some((c) => !c.flagCode))) {
      this.snackBar.open('Vui lòng chọn cờ cho mọi điều kiện.', 'Đóng', { duration: 2500 });
      return;
    }
    if (edit.actionType === 'START_STEP' && edit.startStepOrder == null) {
      this.snackBar.open('Rule START_STEP phải chọn step bắt đầu.', 'Đóng', { duration: 2500 });
      return;
    }
    if (edit.actionType === 'BLOCK' && !edit.blockErrorCode.trim()) {
      this.snackBar.open('Rule CHẶN phải có mã lỗi trả về client.', 'Đóng', { duration: 2500 });
      return;
    }
    const rule: FlagRule = {
      id: this.editingRuleIndex !== null ? this.flagRules()[this.editingRuleIndex].id : undefined,
      name: edit.name.trim(),
      priority: edit.priority,
      conditions: edit.groups.map((g) => g.conditions.map((c) => ({ flagCode: c.flagCode, expected: c.expected }))),
      actionType: edit.actionType,
      startStepOrder: edit.actionType === 'START_STEP' ? edit.startStepOrder : null,
      blockErrorCode: edit.actionType === 'BLOCK' ? edit.blockErrorCode.trim() : null,
      blockMessage: edit.actionType === 'BLOCK' ? edit.blockMessage.trim() || null : null,
    };
    const list = [...this.flagRules()];
    if (this.editingRuleIndex === null) {
      list.push(rule);
    } else {
      list[this.editingRuleIndex] = rule;
    }
    list.sort((a, b) => a.priority - b.priority);
    this.flagRules.set(list);
    this.closePanel();
    this.snackBar.open('Đã cập nhật Rule cờ.', 'Đóng', { duration: 2000 });
  }

  removeRuleFromPanel(): void {
    if (this.editingRuleIndex !== null) {
      this.flagRules.set(this.flagRules().filter((_, i) => i !== this.editingRuleIndex));
    }
    this.closePanel();
  }

  /** Tên hiển thị của cờ theo mã - gợi ý trong node Rule và dropdown chọn cờ. */
  flagDisplayName(flagCode: string): string {
    return this.flags().find((f) => f.flagCode === flagCode)?.displayName ?? flagCode;
  }

  // ------------------------------------------------------------------ //
  // Save
  // ------------------------------------------------------------------ //

  /** Panel "Sơ đồ" trong canvas: anh chup ban nhap hien tai (chua luu); bam "Làm mới" de cap nhat. */
  readonly showFlow = signal(false);
  readonly flowSnapshot = signal<FlowSource | null>(null);

  toggleFlow(): void {
    const next = !this.showFlow();
    this.showFlow.set(next);
    if (next) {
      this.refreshFlow();
    }
  }

  refreshFlow(): void {
    this.flowSnapshot.set({ steps: this.toPayload().steps });
  }

  save(): void {
    if (!this.header.name.trim()) {
      this.snackBar.open('Vui lòng nhập Tên endpoint.', 'Đóng', { duration: 3000 });
      return;
    }
    if (!this.header.path.trim() || !this.header.path.startsWith('/')) {
      this.snackBar.open('Path phải bắt đầu bằng "/".', 'Đóng', { duration: 3000 });
      return;
    }
    if (this.steps().length === 0) {
      this.snackBar.open('Endpoint phải có ít nhất 1 backend step.', 'Đóng', { duration: 3000 });
      return;
    }

    const payload = this.toPayload();
    this.saving.set(true);
    const request$ = this.endpointId ? this.api.update(this.endpointId, payload) : this.api.create(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Đã lưu endpoint qua Canvas - có hiệu lực ngay.', 'Đóng', { duration: 2500 });
        this.router.navigate(['/endpoints']);
      },
      error: (err) => {
        this.saving.set(false);
        this.snackBar.open(err?.error?.message ?? 'Lưu endpoint thất bại.', 'Đóng', { duration: 4000 });
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/endpoints']);
  }

  private toPayload(): EndpointConfig {
    // Khong gui id/createdAt/updatedAt o cap Endpoint: PUT da mang id qua URL,
    // EndpointRequestDto (Java record) khong co field id - gui thua se bi Jackson
    // tu choi (dung bug da fix o endpoint-form finding #1).
    return {
      name: this.header.name,
      description: this.header.description,
      path: this.header.path,
      method: this.header.method,
      sequential: this.header.sequential,
      outputEncoding: this.header.outputEncoding || 'json',
      idempotencyEnabled: this.header.idempotencyEnabled ?? false,
      idempotencyTtlSeconds: this.header.idempotencyTtlSeconds || 86400,
      parallelExecution: this.header.parallelExecution ?? false,
      responseCacheEnabled: this.header.responseCacheEnabled ?? false,
      responseCacheTtlSeconds: this.header.responseCacheTtlSeconds || 300,
      steps: this.steps(),
      mappings: this.mappings(),
      responseMapping: this.responseMappingSpec() ?? undefined,
      flagRules: this.flagRules(),
    };
  }

  /**
   * Mirror dung endpoint-form.component.ts (2 noi luon phai dong bo tay) - dieu kien
   * bat header.responseCacheEnabled: endpoint VA TOAN BO step deu phai la GET hoac POST
   * (xem EndpointService.validateResponseCache() - backend chan cung, ham nay chi de
   * disable toggle + hien canh bao SOM tren UI).
   */
  allStepsAreGetOrPost(): boolean {
    const isGetOrPost = (method: string) => method === 'GET' || method === 'POST';
    if (!isGetOrPost(this.header.method)) {
      return false;
    }
    return this.steps().every((s) => isGetOrPost(s.method));
  }
}

function splitCsv(value: string | null | undefined): string[] {
  if (!value) return [];
  return value
    .split(',')
    .map((x) => x.trim())
    .filter((x) => x.length > 0);
}
