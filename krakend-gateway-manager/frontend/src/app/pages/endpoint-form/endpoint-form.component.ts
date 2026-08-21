import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
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
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { JsonPreviewComponent } from '../../components/json-preview/json-preview.component';
import {
  BackendStep,
  emptyEndpoint,
  emptyStep,
  EndpointConfig,
  FieldMapping,
  GatewayInfo,
  HTTP_METHODS,
  MAPPING_TARGET_TYPES,
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
 *   └── mappings: FormArray<MappingGroup>   (chain field giua cac step)
 *
 * Preview JSON duoc goi lai (debounce 400ms) moi khi form thay doi, thong qua
 * POST /api/endpoints/preview - khong can luu DB truoc.
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
    JsonPreviewComponent,
  ],
  templateUrl: './endpoint-form.component.html',
  styleUrl: './endpoint-form.component.scss',
})
export class EndpointFormComponent implements OnInit, OnDestroy {
  readonly httpMethods = HTTP_METHODS;
  readonly mappingTargetTypes = MAPPING_TARGET_TYPES;

  readonly saving = signal(false);
  readonly loadingPreview = signal(false);
  readonly previewJson = signal<unknown>(null);
  readonly previewWarnings = signal<string[]>([]);
  readonly isEditMode = signal(false);

  /** Danh sach endpoint KHAC (khong tinh chinh no) - dung cho Endpoint Picker khi cau hinh "goi lai KrakenD". */
  readonly otherEndpoints = signal<EndpointConfig[]>([]);
  readonly gatewayInfo = signal<GatewayInfo | null>(null);

  private endpointId: string | null = null;
  private readonly previewTrigger = new Subject<void>();
  private subscriptions = new Subscription();

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
    this.subscriptions.add(
      this.previewTrigger.pipe(debounceTime(400)).subscribe(() => this.refreshPreview()),
    );
    this.subscriptions.add(this.form.valueChanges.subscribe(() => this.previewTrigger.next()));

    this.api.getGatewayInfo().subscribe({ next: (info) => this.gatewayInfo.set(info) });

    this.endpointId = this.route.snapshot.paramMap.get('id');
    this.loadOtherEndpoints();

    if (this.endpointId) {
      this.isEditMode.set(true);
      this.api.get(this.endpointId).subscribe({
        next: (ep) => {
          this.form = this.buildForm(ep);
          this.subscriptions.add(this.form.valueChanges.subscribe(() => this.previewTrigger.next()));
          this.previewTrigger.next();
        },
        error: () => this.snackBar.open('Khong tai duoc endpoint.', 'Dong', { duration: 3000 }),
      });
    } else {
      this.previewTrigger.next();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
      hostsText: [step.hosts.join(', '), Validators.required],
      group: [step.group ?? ''],
      target: [step.target ?? ''],
      allowFieldsText: [step.allowFields.join(', ')],
      denyFieldsText: [step.denyFields.join(', ')],
      renameEntries: this.fb.array(
        Object.entries(step.fieldRenameMapping ?? {}).map(([source, target]) =>
          this.buildRenameEntry(source, target),
        ),
      ),
    });
  }

  private buildRenameEntry(source = '', target = ''): FormGroup {
    return this.fb.group({ source: [source, Validators.required], target: [target, Validators.required] });
  }

  private buildMappingGroup(m: FieldMapping): FormGroup {
    return this.fb.group({
      sourceStepOrder: [m.sourceStepOrder, Validators.required],
      sourceField: [m.sourceField, Validators.required],
      targetStepOrder: [m.targetStepOrder, Validators.required],
      targetType: [m.targetType, Validators.required],
      targetParamName: [m.targetParamName, Validators.required],
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

  /** Danh sach so thu tu step hien co - dung cho <mat-select> chon step nguon/dich trong mapping. */
  get availableStepOrders(): number[] {
    return this.stepsArray.controls.map((c) => c.get('stepOrder')!.value as number);
  }

  // ------------------------------------------------------------------ //
  // Endpoint Picker - "goi lai 1 endpoint KrakenD khac" (xem README muc
  // "Nested trick"): tu dong dien Host = chinh KrakenD + URL pattern = path
  // cua endpoint duoc chon, thay vi phai go tay (de gay sai host/port/path).
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

    this.stepsArray.at(stepIndex).patchValue({
      hostsText: info.selfBaseUrl,
      urlPattern: target.path,
    });
    this.snackBar.open(`Đã điền Host/URL pattern theo endpoint "${target.name}".`, 'Đóng', { duration: 2500 });
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
      const src = m.get('sourceStepOrder')!.value as number;
      const tgt = m.get('targetStepOrder')!.value as number;
      if (src === removedOrder || tgt === removedOrder) {
        this.mappingsArray.removeAt(i);
        continue;
      }
      if (src > removedOrder) m.get('sourceStepOrder')!.setValue(src - 1);
      if (tgt > removedOrder) m.get('targetStepOrder')!.setValue(tgt - 1);
    }
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
  // Field mappings (chain giua cac step)
  // ------------------------------------------------------------------ //

  addMapping(): void {
    const orders = this.availableStepOrders;
    const target = orders.length > 1 ? orders[orders.length - 1] : orders[0] ?? 1;
    const source = orders.length > 1 ? orders[0] : 1;
    this.mappingsArray.push(
      this.buildMappingGroup({
        sourceStepOrder: source,
        sourceField: '',
        targetStepOrder: target,
        targetType: 'QUERY',
        targetParamName: '',
      }),
    );
  }

  removeMapping(index: number): void {
    this.mappingsArray.removeAt(index);
  }

  // ------------------------------------------------------------------ //
  // Preview & Save
  // ------------------------------------------------------------------ //

  private refreshPreview(): void {
    if (!this.form.get('path')?.value || this.stepsArray.length === 0) {
      return;
    }
    this.loadingPreview.set(true);
    this.api.previewDraft(this.toPayload()).subscribe({
      next: (res) => {
        this.loadingPreview.set(false);
        this.previewJson.set(res.json);
        this.previewWarnings.set(res.warnings);
      },
      error: () => {
        // Form chua hop le du de sinh preview (vi du dang go do) - khong lam phien nguoi dung bang loi
        this.loadingPreview.set(false);
        this.previewJson.set(null);
        this.previewWarnings.set([]);
      },
    });
  }

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
        this.snackBar.open('Da luu endpoint thanh cong.', 'Dong', { duration: 2500 });
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
      hosts: splitCsv(s.hostsText),
      group: s.group || null,
      target: s.target || null,
      allowFields: splitCsv(s.allowFieldsText),
      denyFields: splitCsv(s.denyFieldsText),
      fieldRenameMapping: Object.fromEntries(
        (s.renameEntries as Array<{ source: string; target: string }>)
          .filter((e) => e.source && e.target)
          .map((e) => [e.source, e.target]),
      ),
    }));

    const mappings: FieldMapping[] = v.mappings.map((m: any) => ({
      sourceStepOrder: m.sourceStepOrder,
      sourceField: m.sourceField,
      targetStepOrder: m.targetStepOrder,
      targetType: m.targetType,
      targetParamName: m.targetParamName,
    }));

    return {
      id: this.endpointId ?? undefined,
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
