import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HopAuditEvent, LogSearchFilter, RequestAuditEvent } from '../../models/log-search.model';
import { LogSearchApiService } from '../../services/log-search-api.service';

const PAGE_SIZE_OPTIONS = [20, 50, 100];

/**
 * "Tra cứu Log" - đọc index "gwm-requests-..." và "gwm-hops-..." trên Elasticsearch (xem
 * LogSearchController/LogSearchService ở backend). Chỉ ĐỌC, không sửa gì.
 * Mỗi dòng = 1 request client thật gọi vào gateway, bấm mở rộng để xem
 * waterfall từng hop (mỗi BackendStep gọi Upstream thật) - dữ liệu do
 * AuditLogService ghi ra tại request-time, KHÔNG phải giả lập lại.
 */
@Component({
  selector: 'app-log-search',
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
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './log-search.component.html',
  styleUrl: './log-search.component.scss',
})
export class LogSearchComponent implements OnInit {
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly displayedColumns = ['timestamp', 'endpoint', 'status', 'duration', 'expand'];

  // ---- Bo loc (gia tri form - datetime-local dung gio DIA PHUONG, doi sang ISO/UTC khi goi API) ----
  fromLocal = '';
  toLocal = '';
  status: '' | 'SUCCESS' | 'ERROR' = '';
  endpointPath = '';
  bodyContains = '';

  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly items = signal<RequestAuditEvent[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  /** requestId dang mo rong xem hop (null = khong dong nao mo). */
  readonly expandedRequestId = signal<string | null>(null);
  readonly hopsLoading = signal(false);
  readonly hopsError = signal<string | null>(null);
  private readonly hopsCache = new Map<string, HopAuditEvent[]>();
  readonly currentHops = signal<HopAuditEvent[]>([]);

  constructor(private readonly api: LogSearchApiService) {}

  ngOnInit(): void {
    // Mac dinh: 24h gan nhat - du dung cho nhu cau "vua goi xong, tra cuu ngay"
    // ma khong bat buoc nguoi dung phai tu chinh khoang thoi gian truoc.
    const now = new Date();
    const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    this.fromLocal = toDatetimeLocalString(yesterday);
    this.toLocal = toDatetimeLocalString(now);
    this.runSearch();
  }

  private currentFilter(): LogSearchFilter {
    return {
      from: this.fromLocal ? new Date(this.fromLocal).toISOString() : null,
      to: this.toLocal ? new Date(this.toLocal).toISOString() : null,
      status: this.status || null,
      endpointPath: this.endpointPath.trim() || null,
      bodyContains: this.bodyContains.trim() || null,
    };
  }

  runSearch(): void {
    this.page.set(0);
    this.fetch();
  }

  resetFilters(): void {
    const now = new Date();
    const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    this.fromLocal = toDatetimeLocalString(yesterday);
    this.toLocal = toDatetimeLocalString(now);
    this.status = '';
    this.endpointPath = '';
    this.bodyContains = '';
    this.runSearch();
  }

  private fetch(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.expandedRequestId.set(null);
    this.api.search(this.currentFilter(), this.page(), this.size()).subscribe({
      next: (result) => {
        this.items.set(result.items);
        this.total.set(result.total);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.loadError.set(err?.error?.message ?? 'Không tra cứu được log - kiểm tra Elasticsearch có đang chạy không.');
      },
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
    if (t === 0) return '0 kết quả';
    const start = this.page() * this.size() + 1;
    const end = Math.min(start + this.size() - 1, t);
    return `${start}–${end} / ${t}`;
  }

  toggleExpand(row: RequestAuditEvent): void {
    if (this.expandedRequestId() === row.requestId) {
      this.expandedRequestId.set(null);
      return;
    }
    this.expandedRequestId.set(row.requestId);
    const cached = this.hopsCache.get(row.requestId);
    if (cached) {
      this.currentHops.set(cached);
      this.hopsError.set(null);
      return;
    }
    this.hopsLoading.set(true);
    this.hopsError.set(null);
    this.currentHops.set([]);
    this.api.getHops(row.requestId).subscribe({
      next: (hops) => {
        this.hopsCache.set(row.requestId, hops);
        this.currentHops.set(hops);
        this.hopsLoading.set(false);
      },
      error: (err) => {
        this.hopsLoading.set(false);
        this.hopsError.set(err?.error?.message ?? 'Không tải được chi tiết hop.');
      },
    });
  }

  formatTimestamp(iso: string): string {
    return new Date(iso).toLocaleString('vi-VN');
  }
}

/** "2026-08-26T10:30" (gio dia phuong, khong co giay/timezone) - dung lam value cho input[type=datetime-local]. */
function toDatetimeLocalString(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
