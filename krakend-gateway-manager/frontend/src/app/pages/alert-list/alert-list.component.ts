import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { interval, Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { AlertResponse } from '../../models/alert.model';
import { AlertApiService, AlertEmailRecipient } from '../../services/alert-api.service';

const PAGE_SIZE_OPTIONS = [20, 50, 100];

/**
 * "Canh bao" - danh sach van de Data Plane phat hien (CB OPEN, ty le loi cao,
 * bulkhead day) do AlertDetector o DP push ve CP luu GWM_ALERT (xem
 * AlertService/AlertController). Polling 10s de thay van de moi ma khong can
 * F5 - khong dung WebSocket vi V1 khong co ha tang push tu FE.
 */
@Component({
  selector: 'app-alert-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './alert-list.component.html',
  styleUrl: './alert-list.component.scss',
})
export class AlertListComponent implements OnInit, OnDestroy {
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly displayedColumns = ['severity', 'type', 'target', 'status', 'occurrences', 'firstSeen', 'lastSeen', 'actions'];

  status: '' | 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED' = 'ACTIVE';
  targetName = '';

  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly items = signal<AlertResponse[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  readonly summary = signal<{ activeCount: number; activeCriticalCount: number; activeWarningCount: number } | null>(null);

  /** Row dang mo chi tiet (khong goi API rieng - detail da co trong danh sach). */
  readonly expandedId = signal<string | null>(null);

  // ---- Tab nguoi nhan email canh bao (GWM_ALERT_EMAIL - DDL V4) ----
  readonly tab = signal<'alerts' | 'recipients'>('alerts');
  readonly recipients = signal<AlertEmailRecipient[]>([]);
  readonly recipientLoading = signal(false);
  readonly newRecipientEmail = signal('');

  private readonly destroy$ = new Subject<void>();
  private readonly snackbarDuration = 3500;

  constructor(
    private readonly api: AlertApiService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.fetch();
    // Polling 10s - takeUntilDestroyed thay bang Subject thu cong (dung voi
    // destroy$ o ngOnDestroy) de khong roi request khi da roi trang.
    interval(10000)
      .pipe(
        switchMap(() => this.api.search(this.status || null, this.targetName.trim() || null, this.page(), this.size())),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (result) => {
          this.items.set(result.items);
          this.total.set(result.total);
        },
        // Loi polling im lang (khong hien banner lap lai 6 lan/phut) - banner chi
        // hien cho fetch thu cong (fetch() co set loadError).
        error: () => undefined,
      });
    this.pollSummary();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  runSearch(): void {
    this.page.set(0);
    this.fetch();
  }

  switchTab(tab: 'alerts' | 'recipients'): void {
    this.tab.set(tab);
    if (tab === 'recipients') this.fetchRecipients();
  }

  fetchRecipients(): void {
    this.recipientLoading.set(true);
    this.api.listRecipients().subscribe({
      next: (list) => {
        this.recipients.set(list);
        this.recipientLoading.set(false);
      },
      error: (err) => {
        this.recipientLoading.set(false);
        this.snackBar.open(err?.error?.message ?? 'Không tải được danh sách người nhận mail.', 'Đóng', { duration: this.snackbarDuration });
      },
    });
  }

  addRecipient(): void {
    const email = this.newRecipientEmail().trim();
    if (!email) return;
    this.api.addRecipient(email).subscribe({
      next: () => {
        this.newRecipientEmail.set('');
        this.snackBar.open('Đã thêm người nhận mail cảnh báo.', 'Đóng', { duration: this.snackbarDuration });
        this.fetchRecipients();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không thêm được người nhận mail.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  deleteRecipient(row: AlertEmailRecipient): void {
    if (!confirm(`Xóa người nhận "${row.email}"?`)) return;
    this.api.deleteRecipient(row.id).subscribe({
      next: () => {
        this.snackBar.open('Đã xóa người nhận mail cảnh báo.', 'Đóng', { duration: this.snackbarDuration });
        this.fetchRecipients();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không xóa được người nhận mail.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  resetFilters(): void {
    this.status = 'ACTIVE';
    this.targetName = '';
    this.runSearch();
  }

  private fetch(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.api.search(this.status || null, this.targetName.trim() || null, this.page(), this.size()).subscribe({
      next: (result) => {
        this.items.set(result.items);
        this.total.set(result.total);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.loadError.set(err?.error?.message ?? 'Không tải được danh sách cảnh báo.');
      },
    });
  }

  private pollSummary(): void {
    interval(10000)
      .pipe(
        switchMap(() => this.api.summary()),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (s) => this.summary.set(s),
        error: () => undefined,
      });
  }

  changePage(delta: number): void {
    const next = this.page() + delta;
    if (next < 0 || next * this.size() >= this.total()) return;
    this.page.set(next);
    this.fetch();
  }

  onPageSizeChange(newSize: number): void {
    this.size.set(newSize);
    this.page.set(0);
    this.fetch();
  }

  get rangeLabel(): string {
    const t = this.total();
    if (t === 0) return '0 cảnh báo';
    const start = this.page() * this.size() + 1;
    const end = Math.min(start + this.size() - 1, t);
    return `${start}–${end} / ${t}`;
  }

  toggleExpand(row: AlertResponse): void {
    this.expandedId.set(this.expandedId() === row.id ? null : row.id);
  }

  acknowledge(row: AlertResponse): void {
    this.api.acknowledge(row.id).subscribe({
      next: () => {
        this.snackBar.open('Đã ghi nhận cảnh báo (acknowledge).', 'Đóng', { duration: this.snackbarDuration });
        this.fetch();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không acknowledge được cảnh báo.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  resolve(row: AlertResponse): void {
    if (!confirm(`Resolve cảnh báo "${row.title}"? Chỉ làm khi bạn đã xác nhận vấn đề thực sự hết (DP sẽ tự resolve nếu hết thật).`)) {
      return;
    }
    this.api.resolve(row.id).subscribe({
      next: () => {
        this.snackBar.open('Đã resolve cảnh báo.', 'Đóng', { duration: this.snackbarDuration });
        this.fetch();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không resolve được cảnh báo.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  severityLabel(severity: string): string {
    return severity === 'CRITICAL' ? 'Nghiêm trọng' : 'Cảnh báo';
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Đang hoạt động';
      case 'ACKNOWLEDGED':
        return 'Đã ghi nhận';
      case 'RESOLVED':
        return 'Đã kết thúc';
      default:
        return status;
    }
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'CIRCUIT_OPEN':
        return 'Circuit breaker mở';
      case 'ERROR_RATE_HIGH':
        return 'Tỷ lệ lỗi cao (upstream)';
      case 'BULKHEAD_SATURATED':
        return 'Bulkhead đầy';
      case 'ENDPOINT_ERROR_RATE':
        return 'Tỷ lệ lỗi cao (endpoint)';
      default:
        return type;
    }
  }

  resolvedReasonLabel(reason: string | null): string {
    switch (reason) {
      case 'DP_RESOLVED':
        return 'DP xác nhận vấn đề hết';
      case 'STALENESS':
        return 'Hết 30 phút không có tin mới (tự kết thúc)';
      case 'MANUAL':
        return 'Người dùng resolve thủ công';
      default:
        return reason ?? '';
    }
  }

  formatTimestamp(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleString('vi-VN');
  }
}
