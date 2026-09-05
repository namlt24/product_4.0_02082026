import { CommonModule } from '@angular/common';
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
  emptyStep,
  EndpointConfig,
  FieldMapping,
  FieldMappingSourceType,
  FIELD_MAPPING_SOURCE_TYPES,
  GatewayInfo,
  HttpMethodType,
  HTTP_METHODS,
  MAPPING_TARGET_CONTEXTS,
  MAPPING_TARGET_TYPES,
  TryResult,
  UpstreamService,
  extractQueryParamNames,
} from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';
import { TryPanelComponent, TryRunRequest } from '../../components/try-panel/try-panel.component';

const NODE_W = 220;
const NODE_H = 96;
const CLIENT_X = 40;
const CLIENT_Y = 48;
const COL_GAP = 90;
const ROW_Y = 48;

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
  group: string;
  target: string;
  allowFieldsText: string;
  denyFieldsText: string;
  renameEntries: { source: string; target: string }[];
  // Re nhanh (P1-5) - conditionOperator=null la mac dinh/tat, giu nguyen hanh vi step binh thuong.
  conditionSourceType: FieldMappingSourceType;
  conditionSourceStepOrder: number | null;
  conditionSourceField: string;
  conditionOperator: ConditionOperator | null;
  conditionExpectedValue: string;
  nextStepOrderIfTrue: number | null;
  nextStepOrderIfFalse: number | null;
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
  outcome: 'true' | 'false' | 'error';
  path: string;
  labelX: number;
  labelY: number;
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
   * chinh no vo nghia) - ngoai pham vi, chi dung cho FieldMapping thuong. */
  readonly conditionSourceTypes = FIELD_MAPPING_SOURCE_TYPES.filter(
    (t) => t !== 'STEP_RESPONSE_ARRAY_AGGREGATE' && t !== 'QUERY_PARAM' && t !== 'CONSTANT',
  );
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

  /** Do lech dang keo (chua tha) cua 1 node - dung de duong noi SVG "chay theo" luc keo. */
  private readonly dragOffset = signal<{ index: number; dx: number; dy: number } | null>(null);

  // Panel truot ben phai - state don gian (khong dung Reactive Forms) vi chi 1 khoi
  // dang sua tai 1 thoi diem, khong can dong bo 2 bieu dien du lieu song song.
  editingStep: StepEditModel | null = null;
  editingStepIndex: number | null = null;
  editingMapping: FieldMapping | null = null;
  editingMappingIndex: number | null = null;

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
  readonly positionedBranches = computed<PositionedBranch[]>(() => {
    const positions = this.liveNodePositions();
    const byOrder = new Map(positions.map((p) => [p.step.stepOrder, p]));
    const branches: PositionedBranch[] = [];
    const DIP = 70;

    const addBranch = (p: NodePos, fromStepIndex: number, outcome: 'true' | 'false' | 'error', targetOrder: number | null | undefined) => {
      if (targetOrder == null) {
        return;
      }
      const target = byOrder.get(targetOrder);
      if (!target) {
        return;
      }
      const x1 = p.x + this.nodeWidth / 2;
      const y1 = p.y + this.nodeHeight;
      const x2 = target.x + this.nodeWidth / 2;
      const y2 = target.y + this.nodeHeight;
      branches.push({
        fromStepIndex,
        outcome,
        path: `M ${x1} ${y1} C ${x1} ${y1 + DIP}, ${x2} ${y2 + DIP}, ${x2} ${y2}`,
        labelX: (x1 + x2) / 2,
        labelY: Math.max(y1, y2) + DIP + 4,
      });
    };

    positions.forEach((p, fromStepIndex) => {
      if (p.step.conditionOperator) {
        addBranch(p, fromStepIndex, 'true', p.step.nextStepOrderIfTrue);
        addBranch(p, fromStepIndex, 'false', p.step.nextStepOrderIfFalse);
      }
      // onErrorStepOrder DOC LAP voi conditionOperator - ve rieng du step co dieu kien hay khong.
      addBranch(p, fromStepIndex, 'error', p.step.onErrorStepOrder);
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

  readonly canvasSize = computed(() => {
    let maxX = this.clientNodeX + this.nodeWidth + 260;
    let maxY = this.clientNodeY + this.nodeHeight + 160;
    for (const p of this.nodeBasePositions()) {
      maxX = Math.max(maxX, p.x + this.nodeWidth + 100);
      maxY = Math.max(maxY, p.y + this.nodeHeight + 160);
    }
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
    return this.editingStep !== null || this.editingMapping !== null || this.tryPanelOpen;
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
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.api.getGatewayInfo().subscribe({ next: (info) => this.gatewayInfo.set(info) });
    this.api.listUpstreams().subscribe({ next: (list) => this.upstreams.set(list) });

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
      nextStepOrderIfTrue: s.nextStepOrderIfTrue ?? null,
      nextStepOrderIfFalse: s.nextStepOrderIfFalse ?? null,
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
      this.editingStep.conditionSourceType = 'STEP_RESPONSE';
      this.editingStep.conditionSourceStepOrder = null;
      this.editingStep.conditionSourceField = '';
      this.editingStep.conditionOperator = null;
      this.editingStep.conditionExpectedValue = '';
      this.editingStep.nextStepOrderIfTrue = null;
      this.editingStep.nextStepOrderIfFalse = null;
    }
  }

  stepConditionNeedsSourceStep(): boolean {
    return this.editingStep?.conditionSourceType !== 'REQUEST_BODY';
  }

  stepConditionNeedsExpectedValue(): boolean {
    const op = this.editingStep?.conditionOperator;
    return op !== 'EXISTS' && op !== 'NOT_EXISTS';
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
      nextStepOrderIfTrue: edit.conditionOperator ? edit.nextStepOrderIfTrue : null,
      nextStepOrderIfFalse: edit.conditionOperator ? edit.nextStepOrderIfFalse : null,
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
    this.closePanel();
  }

  removeMappingFromPanel(): void {
    if (this.editingMappingIndex !== null) {
      this.mappings.set(this.mappings().filter((_, i) => i !== this.editingMappingIndex));
    }
    this.closePanel();
  }

  closePanel(): void {
    this.editingStep = null;
    this.editingStepIndex = null;
    this.editingMapping = null;
    this.editingMappingIndex = null;
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
      .tryAdhoc({ endpoint: payload, pathVariables: req.pathVariables, queryParams: req.queryParams, body: req.body })
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
  // Save
  // ------------------------------------------------------------------ //

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
