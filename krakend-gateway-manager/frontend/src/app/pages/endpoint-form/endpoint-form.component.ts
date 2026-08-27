import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
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
  emptyEndpoint,
  emptyStep,
  EndpointConfig,
  FieldMapping,
  FIELD_MAPPING_SOURCE_TYPES,
  GatewayInfo,
  HTTP_METHODS,
  MAPPING_TARGET_TYPES,
  UpstreamService,
} from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * Man hinh tao/sua 1 Endpoint Gateway.
 *
 * Cau truc form:
 *   form
 *   ├── name, description, path, method, sequential, outputEncoding
 *   ├── steps: FormArray<StepGroup>        (Step 1, Step 2, ... goi tuan tu)
 *   │     └── renameEntries: FormArray      (doi ten field truoc khi merge)
 *   └── mappings: FormArray<MappingGroup>   (trich xuat gia tri -> bom vao step)
 *
 * Luu = co hieu luc ngay (khong con "Deploy" theo nghia ghi file/restart nua -
 * xem CompositeOrchestratorEngine/EndpointRegistryCache o backend).
 */
@Component({
  selector: 'app-endpoint-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatExpansionModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './endpoint-form.component.html',
  styleUrl: './endpoint-form.component.scss',
})
export class EndpointFormComponent implements OnInit {
  readonly httpMethods = HTTP_METHODS;
  readonly mappingTargetTypes = MAPPING_TARGET_TYPES;
  readonly sourceTypes = FIELD_MAPPING_SOURCE_TYPES;
  /** Nguon dieu kien chi cho phep Step truoc/Body client - Gop mang khong co y nghia cho 1 dieu kien dung/sai,
   * Query param cua client / hang so co dinh KHONG dung cho re nhanh (ngoai pham vi, chi dung cho FieldMapping thuong). */
  readonly conditionSourceTypes = FIELD_MAPPING_SOURCE_TYPES.filter(
    (t) => t !== 'STEP_RESPONSE_ARRAY_AGGREGATE' && t !== 'QUERY_PARAM' && t !== 'CONSTANT',
  );
  readonly conditionOperators = CONDITION_OPERATORS;

  readonly saving = signal(false);
  readonly isEditMode = signal(false);

  readonly upstreams = signal<UpstreamService[]>([]);
  /** Danh sach endpoint KHAC (khong tinh chinh no) - dung cho Endpoint Picker khi cau hinh "goi lai gateway nay". */
  readonly otherEndpoints = signal<EndpointConfig[]>([]);
  readonly gatewayInfo = signal<GatewayInfo | null>(null);

  private endpointId: string | null = null;

  form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: EndpointApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {
    this.form = this.buildForm(emptyEndpoint());
  }

  ngOnInit(): void {
    this.api.getGatewayInfo().subscribe({ next: (info) => this.gatewayInfo.set(info) });
    this.api.listUpstreams().subscribe({ next: (list) => this.upstreams.set(list) });

    this.endpointId = this.route.snapshot.paramMap.get('id');
    this.loadOtherEndpoints();

    if (this.endpointId) {
      this.isEditMode.set(true);
      this.api.get(this.endpointId).subscribe({
        next: (ep) => (this.form = this.buildForm(ep)),
        error: () => this.snackBar.open('Khong tai duoc endpoint.', 'Dong', { duration: 3000 }),
      });
    }
  }

  // ------------------------------------------------------------------ //
  // Form construction
  // ------------------------------------------------------------------ //

  private buildForm(ep: EndpointConfig): FormGroup {
    return this.fb.group({
      name: [ep.name, Validators.required],
      description: [ep.description ?? ''],
      path: [ep.path, [Validators.required, Validators.pattern(/^\//)]],
      method: [ep.method, Validators.required],
      sequential: [ep.sequential],
      outputEncoding: [ep.outputEncoding || 'json'],
      steps: this.fb.array(
        [...ep.steps].sort((a, b) => a.stepOrder - b.stepOrder).map((s) => this.buildStepGroup(s)),
      ),
      mappings: this.fb.array(ep.mappings.map((m) => this.buildMappingGroup(m))),
    });
  }

  private buildStepGroup(step: BackendStep): FormGroup {
    return this.fb.group({
      stepOrder: [step.stepOrder],
      name: [step.name, Validators.required],
      method: [step.method, Validators.required],
      urlPattern: [step.urlPattern, Validators.required],
      upstreamServiceId: [step.upstreamServiceId, Validators.required],
      forwardOriginalBody: [step.forwardOriginalBody ?? false],
      cacheEnabled: [step.cacheEnabled ?? false],
      cacheTtlSeconds: [step.cacheTtlSeconds ?? 300, [Validators.min(1)]],
      // Override timeout rieng cho step nay - de trong (null) = dung mac dinh cua Upstream Service.
      connectTimeoutMs: [step.connectTimeoutMs ?? null, [Validators.min(100), Validators.max(60000)]],
      readTimeoutMs: [step.readTimeoutMs ?? null, [Validators.min(100), Validators.max(60000)]],
      group: [step.group ?? ''],
      target: [step.target ?? ''],
      allowFieldsText: [step.allowFields.join(', ')],
      denyFieldsText: [step.denyFields.join(', ')],
      renameEntries: this.fb.array(
        Object.entries(step.fieldRenameMapping ?? {}).map(([source, target]) =>
          this.buildRenameEntry(source, target),
        ),
      ),
      // Re nhanh (P1-5) - conditionOperator=null la trang thai mac dinh/tat, giu
      // nguyen hanh vi step binh thuong. Cac field con lai chi co y nghia khi bat.
      conditionSourceType: [step.conditionSourceType ?? 'STEP_RESPONSE'],
      conditionSourceStepOrder: [step.conditionSourceStepOrder ?? null],
      conditionSourceField: [step.conditionSourceField ?? ''],
      conditionOperator: [step.conditionOperator ?? null],
      conditionExpectedValue: [step.conditionExpectedValue ?? ''],
      nextStepOrderIfTrue: [step.nextStepOrderIfTrue ?? null],
      nextStepOrderIfFalse: [step.nextStepOrderIfFalse ?? null],
    });
  }

  private buildRenameEntry(source = '', target = ''): FormGroup {
    return this.fb.group({ source: [source, Validators.required], target: [target, Validators.required] });
  }

  private buildMappingGroup(m: FieldMapping): FormGroup {
    return this.fb.group({
      sourceType: [m.sourceType ?? 'STEP_RESPONSE', Validators.required],
      sourceStepOrder: [m.sourceStepOrder ?? null],
      sourceField: [m.sourceField ?? ''],
      sourceArrayField: [m.sourceArrayField ?? ''],
      sourceElementField: [m.sourceElementField ?? ''],
      constantValue: [m.constantValue ?? ''],
      targetStepOrder: [m.targetStepOrder, Validators.required],
      targetType: [m.targetType, Validators.required],
      targetParamName: [m.targetParamName, Validators.required],
      // Khong gan UI o man hinh nay - chi giu nguyen gia tri da tai de KHONG lam mat thu tu
      // da sap xep qua trang "Khai bao endpoint keo tha" khi luu lai tu day (xem toPayload()).
      mappingOrder: [m.mappingOrder ?? 0],
    });
  }

  // ------------------------------------------------------------------ //
  // Getters cho template
  // ------------------------------------------------------------------ //

  get stepsArray(): FormArray {
    return this.form.get('steps') as FormArray;
  }

  get mappingsArray(): FormArray {
    return this.form.get('mappings') as FormArray;
  }

  renameEntries(stepIndex: number): FormArray {
    return this.stepsArray.at(stepIndex).get('renameEntries') as FormArray;
  }

  /** FormArray.at() tra ve AbstractControl - ep kieu ro rang ve FormGroup de dung voi [formGroup] trong template. */
  renameEntryGroup(stepIndex: number, entryIndex: number): FormGroup {
    return this.renameEntries(stepIndex).at(entryIndex) as FormGroup;
  }

  mappingGroup(index: number): FormGroup {
    return this.mappingsArray.at(index) as FormGroup;
  }

  /** true neu mapping nay can chon step nguon (khong ap dung cho REQUEST_BODY/QUERY_PARAM/CONSTANT - deu khong lay tu 1 step). */
  mappingNeedsSourceStep(index: number): boolean {
    const sourceType = this.mappingGroup(index).get('sourceType')!.value;
    return sourceType !== 'REQUEST_BODY' && sourceType !== 'QUERY_PARAM' && sourceType !== 'CONSTANT';
  }

  /** true neu mapping nay dung sourceField don (STEP_RESPONSE/REQUEST_BODY), khong phai gop mang/hang so. */
  mappingUsesSourceField(index: number): boolean {
    const sourceType = this.mappingGroup(index).get('sourceType')!.value;
    return sourceType !== 'STEP_RESPONSE_ARRAY_AGGREGATE' && sourceType !== 'CONSTANT';
  }

  mappingUsesArrayAggregate(index: number): boolean {
    return this.mappingGroup(index).get('sourceType')!.value === 'STEP_RESPONSE_ARRAY_AGGREGATE';
  }

  /** sourceType=CONSTANT - gia tri hang so co dinh khai bao truc tiep, khong doc tu request/response nao. */
  mappingUsesConstantValue(index: number): boolean {
    return this.mappingGroup(index).get('sourceType')!.value === 'CONSTANT';
  }

  /** Danh sach so thu tu step hien co - dung cho <mat-select> chon step nguon/dich trong mapping. */
  get availableStepOrders(): number[] {
    return this.stepsArray.controls.map((c) => c.get('stepOrder')!.value as number);
  }

  // ------------------------------------------------------------------ //
  // Dieu kien re nhanh (P1-5) - mac dinh AN, chi hien form chi tiet khi bat toggle,
  // giu UI gon cho da so step khong dung tinh nang nay.
  // ------------------------------------------------------------------ //

  stepConditionEnabled(index: number): boolean {
    return !!this.stepsArray.at(index).get('conditionOperator')!.value;
  }

  toggleStepCondition(index: number, enabled: boolean): void {
    const group = this.stepsArray.at(index);
    if (enabled) {
      group.patchValue({ conditionSourceType: 'STEP_RESPONSE', conditionOperator: 'EXISTS' });
    } else {
      // Tat toggle: xoa het field dieu kien de KHONG luu lai gia tri cu (tranh
      // gui len 1 dieu kien "ma", da tat tren UI nhung van con field trong payload).
      group.patchValue({
        conditionSourceType: 'STEP_RESPONSE',
        conditionSourceStepOrder: null,
        conditionSourceField: '',
        conditionOperator: null,
        conditionExpectedValue: '',
        nextStepOrderIfTrue: null,
        nextStepOrderIfFalse: null,
      });
    }
  }

  stepConditionNeedsSourceStep(index: number): boolean {
    return this.stepsArray.at(index).get('conditionSourceType')!.value !== 'REQUEST_BODY';
  }

  stepConditionNeedsExpectedValue(index: number): boolean {
    const op = this.stepsArray.at(index).get('conditionOperator')!.value;
    return op !== 'EXISTS' && op !== 'NOT_EXISTS';
  }

  /** 4 toan tu so sanh SO (>,>=,<,<=) - doi input sang type="number" cho de nhap, validate chac chan van o backend. */
  stepConditionExpectedValueIsNumeric(index: number): boolean {
    const op = this.stepsArray.at(index).get('conditionOperator')!.value;
    return op === 'GREATER_THAN' || op === 'GREATER_THAN_OR_EQUAL' || op === 'LESS_THAN' || op === 'LESS_THAN_OR_EQUAL';
  }

  // ------------------------------------------------------------------ //
  // Endpoint Picker - "goi lai 1 endpoint cua chinh gateway nay" (xem README
  // muc "Nested trick"): can 1 Upstream Service da dang ky tro ve chinh
  // gateway (host trung selfBaseUrl/selfHostAliases); tu dong chon Upstream do
  // + dien URL pattern = path cua endpoint duoc chon.
  // ------------------------------------------------------------------ //

  private loadOtherEndpoints(): void {
    this.api.list().subscribe({
      next: (all) => this.otherEndpoints.set(all.filter((e) => e.id !== this.endpointId)),
      error: () => this.otherEndpoints.set([]),
    });
  }

  pickEndpoint(stepIndex: number, endpointId: string): void {
    const target = this.otherEndpoints().find((e) => e.id === endpointId);
    const info = this.gatewayInfo();
    if (!target || !info) return;

    const selfUpstream = this.upstreams().find((u) => {
      try {
        const url = new URL(u.baseHost);
        const host = url.hostname;
        // Khop ca port voi chinh gateway (khong chi hostname) - dung y het logic
        // DependencyAnalyzer.isSelfHost() ben backend (dung java.net.URI.getPort(),
        // KHONG tu mac dinh 80/443 khi baseHost thieu port - port thieu = -1 = khong
        // bao gio khop gatewayPort). Khong tu suy đoán port mac dinh o day de tranh
        // 2 ben lech logic.
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

    this.stepsArray.at(stepIndex).patchValue({
      upstreamServiceId: selfUpstream.id,
      urlPattern: target.path,
    });
    this.snackBar.open(`Đã điền Upstream/URL pattern theo endpoint "${target.name}".`, 'Đóng', { duration: 2500 });
  }

  // ------------------------------------------------------------------ //
  // Steps: add / remove
  // ------------------------------------------------------------------ //

  addStep(): void {
    const nextOrder = this.stepsArray.length + 1;
    this.stepsArray.push(this.buildStepGroup(emptyStep(nextOrder)));
    if (this.stepsArray.length > 1) {
      this.form.get('sequential')!.setValue(true);
    }
  }

  removeStep(index: number): void {
    if (this.stepsArray.length <= 1) {
      this.snackBar.open('Phai co it nhat 1 backend step.', 'Dong', { duration: 2500 });
      return;
    }
    const removedOrder = this.stepsArray.at(index).get('stepOrder')!.value as number;
    this.stepsArray.removeAt(index);

    // Renumber cac step con lai theo dung thu tu trong mang (1-based)
    this.stepsArray.controls.forEach((ctrl, i) => ctrl.get('stepOrder')!.setValue(i + 1));

    // Don dep / dieu chinh cac mapping tham chieu toi step vua xoa
    for (let i = this.mappingsArray.length - 1; i >= 0; i--) {
      const m = this.mappingsArray.at(i);
      const src = m.get('sourceStepOrder')!.value as number | null;
      const tgt = m.get('targetStepOrder')!.value as number;
      if (src === removedOrder || tgt === removedOrder) {
        this.mappingsArray.removeAt(i);
        continue;
      }
      if (src !== null && src > removedOrder) m.get('sourceStepOrder')!.setValue(src - 1);
      if (tgt > removedOrder) m.get('targetStepOrder')!.setValue(tgt - 1);
    }

    // Don dep / dieu chinh dieu kien re nhanh (P1-5) cua CAC STEP CON LAI tham
    // chieu toi step vua xoa - dung y het cach lam voi mapping o tren: tham
    // chieu toi step da xoa thi TAT dieu kien do luon (khong con nghia gi ca,
    // giu lai se bi validate backend chan luc luu voi loi kho hieu), con tham
    // chieu toi step con lai thi dich stepOrder xuong 1 theo dung phep renumber.
    this.stepsArray.controls.forEach((ctrl) => {
      const srcStep = ctrl.get('conditionSourceStepOrder')!.value as number | null;
      const nextTrue = ctrl.get('nextStepOrderIfTrue')!.value as number | null;
      const nextFalse = ctrl.get('nextStepOrderIfFalse')!.value as number | null;

      if (srcStep === removedOrder) {
        this.toggleStepCondition(this.stepsArray.controls.indexOf(ctrl), false);
        return; // toggleStepCondition da xoa ca nextTrue/nextFalse cua step nay roi
      }
      if (srcStep !== null && srcStep > removedOrder) ctrl.get('conditionSourceStepOrder')!.setValue(srcStep - 1);

      if (nextTrue === removedOrder) ctrl.get('nextStepOrderIfTrue')!.setValue(null);
      else if (nextTrue !== null && nextTrue > removedOrder) ctrl.get('nextStepOrderIfTrue')!.setValue(nextTrue - 1);

      if (nextFalse === removedOrder) ctrl.get('nextStepOrderIfFalse')!.setValue(null);
      else if (nextFalse !== null && nextFalse > removedOrder) ctrl.get('nextStepOrderIfFalse')!.setValue(nextFalse - 1);
    });
  }

  // ------------------------------------------------------------------ //
  // Field rename (mapping doi ten truoc khi merge)
  // ------------------------------------------------------------------ //

  addRenameEntry(stepIndex: number): void {
    this.renameEntries(stepIndex).push(this.buildRenameEntry());
  }

  removeRenameEntry(stepIndex: number, entryIndex: number): void {
    this.renameEntries(stepIndex).removeAt(entryIndex);
  }

  // ------------------------------------------------------------------ //
  // Field mappings (trich xuat gia tri tu step/body -> bom vao step khac)
  // ------------------------------------------------------------------ //

  addMapping(): void {
    const orders = this.availableStepOrders;
    const target = orders.length > 1 ? orders[orders.length - 1] : orders[0] ?? 1;
    const source = orders.length > 1 ? orders[0] : 1;
    this.mappingsArray.push(
      this.buildMappingGroup({
        sourceType: 'STEP_RESPONSE',
        sourceStepOrder: source,
        sourceField: '',
        targetStepOrder: target,
        targetType: 'QUERY',
        targetParamName: '',
        mappingOrder: this.mappingsArray.length,
      }),
    );
  }

  removeMapping(index: number): void {
    this.mappingsArray.removeAt(index);
  }

  // ------------------------------------------------------------------ //
  // Save
  // ------------------------------------------------------------------ //

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Vui long kiem tra lai cac truong bat buoc (bao gom trong tung step).', 'Dong', {
        duration: 3500,
      });
      return;
    }

    const payload = this.toPayload();
    this.saving.set(true);
    const request$ = this.endpointId ? this.api.update(this.endpointId, payload) : this.api.create(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Da luu endpoint - co hieu luc ngay.', 'Dong', { duration: 2500 });
        this.router.navigate(['/endpoints']);
      },
      error: (err) => {
        this.saving.set(false);
        this.snackBar.open(err?.error?.message ?? 'Luu endpoint that bai.', 'Dong', { duration: 4000 });
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/endpoints']);
  }

  private toPayload(): EndpointConfig {
    const v = this.form.getRawValue();

    const steps: BackendStep[] = v.steps.map((s: any) => ({
      stepOrder: s.stepOrder,
      name: s.name,
      method: s.method,
      urlPattern: s.urlPattern,
      upstreamServiceId: s.upstreamServiceId,
      forwardOriginalBody: s.forwardOriginalBody,
      cacheEnabled: s.cacheEnabled,
      cacheTtlSeconds: s.cacheTtlSeconds,
      connectTimeoutMs: s.connectTimeoutMs || null,
      readTimeoutMs: s.readTimeoutMs || null,
      group: s.group || null,
      target: s.target || null,
      allowFields: splitCsv(s.allowFieldsText),
      denyFields: splitCsv(s.denyFieldsText),
      fieldRenameMapping: Object.fromEntries(
        (s.renameEntries as Array<{ source: string; target: string }>)
          .filter((e) => e.source && e.target)
          .map((e) => [e.source, e.target]),
      ),
      // Re nhanh (P1-5) - conditionOperator null nghia la khong dung, cac field
      // con lai deu gui null theo (dung UX voi toggleStepCondition() da xoa san
      // luc tat toggle, o day chi la lop bao ve them).
      conditionSourceType: s.conditionOperator ? s.conditionSourceType : null,
      conditionSourceStepOrder: s.conditionOperator ? s.conditionSourceStepOrder || null : null,
      conditionSourceField: s.conditionOperator ? s.conditionSourceField || null : null,
      conditionOperator: s.conditionOperator || null,
      conditionExpectedValue: s.conditionOperator ? s.conditionExpectedValue || null : null,
      nextStepOrderIfTrue: s.conditionOperator ? s.nextStepOrderIfTrue || null : null,
      nextStepOrderIfFalse: s.conditionOperator ? s.nextStepOrderIfFalse || null : null,
    }));

    const mappings: FieldMapping[] = v.mappings.map((m: any) => ({
      sourceType: m.sourceType,
      sourceStepOrder: m.sourceStepOrder || null,
      sourceField: m.sourceField || null,
      sourceArrayField: m.sourceArrayField || null,
      sourceElementField: m.sourceElementField || null,
      constantValue: m.constantValue || null,
      targetStepOrder: m.targetStepOrder,
      targetType: m.targetType,
      targetParamName: m.targetParamName,
      mappingOrder: m.mappingOrder,
    }));

    // Khong gui id trong body: PUT da mang id qua URL path (endpoint-api.service.ts
    // update(id, payload)), con EndpointRequestDto (Java record) khong co field id -
    // gui thua se bi Jackson tu choi (fail-on-unknown-properties mac dinh) va Sua
    // endpoint qua UI se luon loi 500.
    return {
      name: v.name,
      description: v.description,
      path: v.path,
      method: v.method,
      sequential: v.sequential,
      outputEncoding: v.outputEncoding || 'json',
      steps,
      mappings,
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
