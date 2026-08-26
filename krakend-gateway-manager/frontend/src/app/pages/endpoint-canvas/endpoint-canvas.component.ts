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
  emptyStep,
  EndpointConfig,
  FieldMapping,
  FIELD_MAPPING_SOURCE_TYPES,
  GatewayInfo,
  HttpMethodType,
  HTTP_METHODS,
  MAPPING_TARGET_TYPES,
  UpstreamService,
} from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

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
  group: string;
  target: string;
  allowFieldsText: string;
  denyFieldsText: string;
  renameEntries: { source: string; target: string }[];
}

interface HeaderModel {
  name: string;
  description: string;
  path: string;
  method: HttpMethodType;
  sequential: boolean;
  outputEncoding: string;
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
  ],
  templateUrl: './endpoint-canvas.component.html',
  styleUrl: './endpoint-canvas.component.scss',
})
export class EndpointCanvasComponent implements OnInit {
  readonly httpMethods = HTTP_METHODS;
  readonly mappingTargetTypes = MAPPING_TARGET_TYPES;
  readonly sourceTypes = FIELD_MAPPING_SOURCE_TYPES;

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

  /** Tham chieu DOM cua panel - dung de phat hien click RA NGOAI panel (dong panel). */
  @ViewChild('panelRef') private panelRef?: ElementRef<HTMLElement>;

  header: HeaderModel = {
    name: '',
    description: '',
    path: '/',
    method: 'GET',
    sequential: false,
    outputEncoding: 'json',
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
    return this.mappings().map((mapping, index) => {
      let x1: number;
      let y1: number;
      if (mapping.sourceType === 'REQUEST_BODY' || mapping.sourceStepOrder == null) {
        x1 = this.clientNodeX + this.nodeWidth;
        y1 = this.clientNodeY + this.nodeHeight / 2;
      } else {
        const source = byOrder.get(mapping.sourceStepOrder);
        x1 = source ? source.x + this.nodeWidth : this.clientNodeX + this.nodeWidth;
        y1 = source ? source.y + this.nodeHeight / 2 : this.clientNodeY + this.nodeHeight / 2;
      }
      const target = byOrder.get(mapping.targetStepOrder);
      const x2 = target ? target.x : x1;
      const y2 = target ? target.y + this.nodeHeight / 2 : y1;
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
    return this.editingStep !== null || this.editingMapping !== null;
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
      group: s.group ?? '',
      target: s.target ?? '',
      allowFieldsText: s.allowFields.join(', '),
      denyFieldsText: s.denyFields.join(', '),
      renameEntries: Object.entries(s.fieldRenameMapping ?? {}).map(([source, target]) => ({ source, target })),
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
      group: edit.group || null,
      target: edit.target || null,
      allowFields: splitCsv(edit.allowFieldsText),
      denyFields: splitCsv(edit.denyFieldsText),
      fieldRenameMapping: Object.fromEntries(
        edit.renameEntries.filter((e) => e.source && e.target).map((e) => [e.source, e.target]),
      ),
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
      .map((s, i) => ({ ...s, stepOrder: i + 1 }));
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
      targetStepOrder: target,
      targetType: 'QUERY',
      targetParamName: '',
      mappingOrder: this.mappings().length,
    };
  }

  mappingNeedsSourceStep(): boolean {
    return this.editingMapping?.sourceType !== 'REQUEST_BODY';
  }

  mappingUsesSourceField(): boolean {
    return this.editingMapping?.sourceType !== 'STEP_RESPONSE_ARRAY_AGGREGATE';
  }

  mappingUsesArrayAggregate(): boolean {
    return this.editingMapping?.sourceType === 'STEP_RESPONSE_ARRAY_AGGREGATE';
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
      steps: this.steps(),
      mappings: this.mappings(),
    };
  }
}

function splitCsv(value: string | null | undefined): string[] {
  if (!value) return [];
  return value
    .split(',')
    .map((x) => x.trim())
    .filter((x) => x.length > 0);
}
