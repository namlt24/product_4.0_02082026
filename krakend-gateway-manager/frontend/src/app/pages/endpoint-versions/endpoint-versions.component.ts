import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EndpointConfig, EndpointVersionSummary, FieldMapping } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * "Lich su phien ban" (P0-4) - trang RIENG, doc lap voi endpoint-form. Chi lam
 * 2 viec: xem lai 1 phien ban cu (snapshot toan bo Endpoint tai 1 lan save) va
 * Khoi phuc ve dung phien ban do (goi qua CUNG duong validate/cycle-check voi
 * sua tay - xem EndpointService.update() o backend, rollback() chi la 1 nhanh
 * goi khac toi cung ham do). KHONG cho sua truc tiep tu day - xem/khoi phuc
 * xong van quay lai form/canvas binh thuong de sua tiep neu can.
 */
@Component({
  selector: 'app-endpoint-versions',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
  ],
  templateUrl: './endpoint-versions.component.html',
  styleUrl: './endpoint-versions.component.scss',
})
export class EndpointVersionsComponent implements OnInit {
  readonly loading = signal(true);
  readonly rollingBackId = signal<string | null>(null);
  readonly endpoint = signal<EndpointConfig | null>(null);
  readonly versions = signal<EndpointVersionSummary[]>([]);

  /** id cua version dang mo rong xem chi tiet (null = khong co gi mo). */
  readonly expandedVersionId = signal<string | null>(null);
  readonly expandedDetail = signal<EndpointConfig | null>(null);
  readonly loadingDetail = signal(false);

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
      next: (ep) => this.endpoint.set(ep),
      error: () => this.snackBar.open('Không tải được endpoint.', 'Đóng', { duration: 3000 }),
    });
    this.api.listVersions(this.endpointId).subscribe({
      next: (list) => {
        this.versions.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Không tải được lịch sử phiên bản.', 'Đóng', { duration: 3000 });
      },
    });
  }

  /** Phien ban moi nhat (dau danh sach, da @OrderBy versionNumber DESC o backend) - khong cho Khoi phuc lai chinh no. */
  isLatest(v: EndpointVersionSummary): boolean {
    return this.versions()[0]?.id === v.id;
  }

  changeTypeLabel(t: EndpointVersionSummary['changeType']): string {
    switch (t) {
      case 'CREATED':
        return 'Tạo mới';
      case 'ROLLED_BACK':
        return 'Khôi phục';
      default:
        return 'Cập nhật';
    }
  }

  toggleDetail(v: EndpointVersionSummary): void {
    if (this.expandedVersionId() === v.id) {
      this.expandedVersionId.set(null);
      this.expandedDetail.set(null);
      return;
    }
    this.expandedVersionId.set(v.id);
    this.expandedDetail.set(null);
    this.loadingDetail.set(true);
    this.api.getVersion(this.endpointId, v.id).subscribe({
      next: (detail) => {
        this.expandedDetail.set(detail);
        this.loadingDetail.set(false);
      },
      error: () => {
        this.loadingDetail.set(false);
        this.snackBar.open('Không tải được chi tiết phiên bản.', 'Đóng', { duration: 3000 });
      },
    });
  }

  sourceLabel(m: FieldMapping): string {
    switch (m.sourceType) {
      case 'REQUEST_BODY':
        return `Body của client · ${m.sourceField}`;
      case 'QUERY_PARAM':
        return `Query param của client · ${m.sourceField}`;
      case 'STEP_RESPONSE_ARRAY_AGGREGATE':
        return `Gộp mảng Step ${m.sourceStepOrder} · ${m.sourceArrayField}[].${m.sourceElementField}`;
      case 'CONSTANT':
        return `Hằng số cố định · ${m.constantValue}`;
      default:
        return `Response Step ${m.sourceStepOrder} · ${m.sourceField}`;
    }
  }

  targetLabel(m: FieldMapping): string {
    return `Step ${m.targetStepOrder} · ${m.targetType} · ${m.targetParamName}`;
  }

  rollback(v: EndpointVersionSummary): void {
    if (
      !confirm(
        `Khôi phục endpoint về đúng phiên bản #${v.versionNumber} (${this.changeTypeLabel(v.changeType)} lúc ${new Date(v.createdAt).toLocaleString('vi-VN')})?\n\n` +
          `Thao tác này tạo ra 1 phiên bản MỚI (giống hệt nội dung phiên bản #${v.versionNumber}) - có hiệu lực ngay lập tức, không xoá lịch sử các phiên bản khác.`,
      )
    ) {
      return;
    }
    this.rollingBackId.set(v.id);
    this.api.rollbackVersion(this.endpointId, v.id).subscribe({
      next: () => {
        this.rollingBackId.set(null);
        this.snackBar.open(`Đã khôi phục về phiên bản #${v.versionNumber}.`, 'Đóng', { duration: 3000 });
        this.expandedVersionId.set(null);
        this.expandedDetail.set(null);
        this.fetch();
      },
      error: (err) => {
        this.rollingBackId.set(null);
        this.snackBar.open(err?.error?.message ?? 'Khôi phục thất bại.', 'Đóng', { duration: 4000 });
      },
    });
  }
}
