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

interface PositionedMapping {
  mapping: FieldMapping;
  index: number;
  path: string;
  labelX: number;
  labelY: number;
}

/** 1 mui ten re nhanh (P1-5) tren canvas - KHAC FieldMapping: khong chuyen du lieu, chi bieu dien luong dieu khien (dung/sai) tu step co dieu kien sang step ke tiep. */
interface PositionedBranch {
  fromStepIndex: number;
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
    (t) =>
      t !== 'STEP_RESPONSE_ARRAY_AGGREGATE' &&
      t !== 'QUERY_PARAM' &&
      t !== 'CONSTANT' &&
      t !== 'STEP_RESPONSE_ARRAY_MERGE' &&
      t !== 'STEP_RESPONSE_ARRAY_FIND',
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

  readonly positionedMappings = computed<PositionedMapping[]>(() => {
    const byOrder = new Map(this.liveNodePositions().map((p) => [p.step.stepOrder, p]));
    const mappings = this.mappings();

    // Nhieu FieldMapping CUNG nguon (vd 2+ mapping CONSTANT - khong co sourceStepOrder,
    // hoac 2+ REQUEST_BODY/QUERY_PARAM deu xuat phat tu node Client) VA CUNG step dich se
    // tinh ra x1/y1/x2/y2 GIONG HET NHAU -> duong cong SVG trung khop 100%, chi con 1
    // duong nhin thay/bam duoc (duong ve SAU CUNG de len tren) - cac mapping khac VAN TON
    // TAI trong du lieu (khong mat), chi khong bam duoc qua canvas. Fix: nhom theo (nguon,
    // dich) giong het nhau, toa do BAT DAU/KET THUC cua tung duong trong 1 nhom duoc GIAN
    // DEU doc theo canh node (khac nhau vai px) de tach thanh nhieu duong cong song song,
    // duong nao cung bam duoc rieng.
    const groupKeyOf = (m: FieldMapping): string =>
      `${m.sourceType === 'REQUEST_BODY' || m.sourceType === 'QUERY_PARAM' || m.sourceType === 'CONSTANT' || m.sourceStepOrder == null ? 'client' : m.sourceStepOrder}->${m.targetStepOrder}`;
    const groupIndex = new Map<string, number>();
    const groupSize = new Map<string, number>();
    for (const m of mappings) {
      const key = groupKeyOf(m);
      groupSize.set(key, (groupSize.get(key) ?? 0) + 1);
    }
    // 20px - lon hon stroke-width=16 cua .edge-hit (vung bam an) de 2 duong ke nhau
    // trong 1 nhom KHONG con de vung bam trung nhau (16px de len 16px se giao nhau
    // giua tam neu spread <16px, kho bam trung dung 1 duong khi 2 duong sat nhau).
    const FAN_SPREAD = 20;

    return mappings.map((mapping, index) => {
      const key = groupKeyOf(mapping);
      const posInGroup = groupIndex.get(key) ?? 0;
      groupIndex.set(key, posInGroup + 1);
      const total = groupSize.get(key) ?? 1;
      const fanOffset = (posInGroup - (total - 1) / 2) * FAN_SPREAD;

      let x1: number;
      let y1: number;
      if (mapping.sourceType === 'REQUEST_BODY' || mapping.sourceType === 'QUERY_PARAM' || mapping.sourceType === 'CONSTANT' || mapping.sourceStepOrder == null) {
        x1 = this.clientNodeX + this.nodeWidth;
        y1 = this.clientNodeY + this.nodeHeight / 2 + fanOffset;
      } else {
        const source = byOrder.get(mapping.sourceStepOrder);
        x1 = source ? source.x + this.nodeWidth : this.clientNodeX + this.nodeWidth;
        y1 = (source ? source.y + this.nodeHeight / 2 : this.clientNodeY + this.nodeHeight / 2) + fanOffset;
      }
      const target = byOrder.get(mapping.targetStepOrder);
      const x2 = target ? target.x : x1;
      const y2 = (target ? target.y + this.nodeHeight / 2 : y1) + fanOffset;
      const midX = (x1 + x2) / 2;
      return {
        mapping,
        index,
        path: `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`,
        labelX: midX,
        labelY: (y1 + y2) / 2 - 6,
      };
    });
  });

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
        return { rule, index, x, y, path, labelX, labelY, actionLabel };
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

  openMappingPanel(index: number): void {
    this.closePanel();
    this.editingMappingIndex = index;
    this.editingMapping = { ...this.mappings()[index] };
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
      filterField: '',
      filterValue: '',
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
      this.editingMapping?.sourceType !== 'STEP_RESPONSE_ARRAY_MERGE' &&
      this.editingMapping?.sourceType !== 'STEP_RESPONSE_ARRAY_FIND'
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

  /**
   * sourceType=STEP_RESPONSE_ARRAY_FIND - tim PHAN TU khop filterField/filterValue trong 1
   * mang (sourceArrayField, dung chung UI voi ARRAY_AGGREGATE/ARRAY_MERGE), roi trich
   * sourceElementField tu DUNG phan tu do.
   */
  mappingUsesArrayFind(): boolean {
    return this.editingMapping?.sourceType === 'STEP_RESPONSE_ARRAY_FIND';
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
    this.closePanel();
  }

  removeMappingFromPanel(): void {
    if (this.editingMappingIndex !== null) {
      this.mappings.set(this.mappings().filter((_, i) => i !== this.editingMappingIndex));
    }
    this.closePanel();
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
