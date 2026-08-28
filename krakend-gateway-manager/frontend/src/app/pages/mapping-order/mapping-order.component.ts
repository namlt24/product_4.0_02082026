import { CommonModule } from '@angular/common';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EndpointConfig, FieldMapping } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * "Khai báo endpoint kéo thả" - trang RIÊNG, độc lập hoàn toàn với endpoint-form.
 * Chỉ làm 1 việc: kéo thả sắp xếp lại thứ tự hiển thị của Field Mapping trong 1
 * Endpoint (thuần tổ chức/dễ đọc - KHÔNG ảnh hưởng hành vi CompositeOrchestratorEngine,
 * engine áp tất cả mapping của 1 step cùng lúc, không tuần tự theo mappingOrder).
 * Không sửa chi tiết từng mapping ở đây (tránh trùng logic sửa với endpoint-form) -
 * sửa field/nguồn/đích vẫn phải quay lại trang Sửa Endpoint.
 */
@Component({
  selector: 'app-mapping-order',
  standalone: true,
  imports: [
    CommonModule,
    DragDropModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
  ],
  templateUrl: './mapping-order.component.html',
  styleUrl: './mapping-order.component.scss',
})
export class MappingOrderComponent implements OnInit {
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly endpoint = signal<EndpointConfig | null>(null);
  readonly mappings = signal<FieldMapping[]>([]);

  private endpointId = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: EndpointApiService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.endpointId = this.route.snapshot.paramMap.get('id') ?? '';
    if (!this.endpointId) {
      this.router.navigate(['/endpoints']);
      return;
    }
    this.fetch();
  }

  private fetch(): void {
    this.loading.set(true);
    this.api.get(this.endpointId).subscribe({
      next: (ep) => {
        this.endpoint.set(ep);
        // Backend da @OrderBy("mappingOrder ASC") nen list tra ve dung thu tu san -
        // sort lai o day cho chac chan (khong phu thuoc backend luon dung 100%).
        this.mappings.set([...ep.mappings].sort((a, b) => (a.mappingOrder ?? 0) - (b.mappingOrder ?? 0)));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Không tải được endpoint.', 'Đóng', { duration: 3000 });
      },
    });
  }

  drop(event: CdkDragDrop<FieldMapping[]>): void {
    const current = [...this.mappings()];
    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.mappings.set(current);
  }

  sourceLabel(m: FieldMapping): string {
    switch (m.sourceType) {
      case 'REQUEST_BODY':
        return `Body của client · ${m.sourceField}`;
      case 'QUERY_PARAM':
        return `Query param của client · ${m.sourceField}`;
      case 'STEP_RESPONSE_ARRAY_AGGREGATE':
        return `Gộp mảng Step ${m.sourceStepOrder} · ${m.sourceArrayField}[].${m.sourceElementField}`;
      case 'STEP_RESPONSE_ARRAY_MERGE':
        return `Gộp N object Step ${m.sourceStepOrder} · ${m.sourceArrayField}[] → 1 object`;
      case 'CONSTANT':
        return `Hằng số cố định · ${m.constantValue}`;
      default:
        return `Response Step ${m.sourceStepOrder} · ${m.sourceField}`;
    }
  }

  targetLabel(m: FieldMapping): string {
    return `Step ${m.targetStepOrder} · ${m.targetType} · ${m.targetParamName}`;
  }

  save(): void {
    const ep = this.endpoint();
    if (!ep) return;
    this.saving.set(true);
    // Chi doi mappingOrder theo vi tri hien thi hien tai + thay mang mappings - moi field
    // khac cua endpoint (name/path/steps/...) giu NGUYEN nhu luc GET, khong dung toi.
    const reordered = this.mappings().map((m, index) => ({ ...m, mappingOrder: index }));
    const payload: EndpointConfig = { ...ep, mappings: reordered };
    this.api.update(this.endpointId, payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Đã lưu thứ tự.', 'Đóng', { duration: 2500 });
        this.fetch();
      },
      error: (err) => {
        this.saving.set(false);
        this.snackBar.open(err?.error?.message ?? 'Lưu thứ tự thất bại.', 'Đóng', { duration: 4000 });
      },
    });
  }
}
