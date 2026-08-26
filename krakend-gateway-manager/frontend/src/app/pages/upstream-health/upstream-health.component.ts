import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { UpstreamHealth } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * "Dashboard sức khoẻ Upstream" (P1) - snapshot trạng thái circuit breaker
 * (Resilience4j, trong bộ nhớ - mất khi restart app) + hit-rate cache Redis
 * (đếm trong-process, xem UpstreamHttpExecutor) của từng Upstream đã đăng ký.
 * Chỉ ĐỌC - không sửa gì ở đây, quay lại trang Upstream Services để sửa cấu hình.
 */
@Component({
  selector: 'app-upstream-health',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, MatTooltipModule],
  templateUrl: './upstream-health.component.html',
  styleUrl: './upstream-health.component.scss',
})
export class UpstreamHealthComponent implements OnInit {
  readonly loading = signal(true);
  readonly items = signal<UpstreamHealth[]>([]);

  constructor(private readonly api: EndpointApiService) {}

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    this.loading.set(true);
    this.api.getUpstreamHealth().subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  stateLabel(state: string): string {
    switch (state) {
      case 'CLOSED':
        return 'Bình thường';
      case 'OPEN':
        return 'Đang chặn (OPEN)';
      case 'HALF_OPEN':
        return 'Đang thử lại (HALF_OPEN)';
      case 'FORCED_OPEN':
        return 'Bị ép mở (FORCED_OPEN)';
      case 'DISABLED':
        return 'Đã tắt circuit breaker';
      default:
        return 'Chưa có lượt gọi nào';
    }
  }

  cacheHitRateLabel(h: UpstreamHealth): string {
    if (h.cacheHitRate < 0) return 'Chưa có dữ liệu';
    return Math.round(h.cacheHitRate * 100) + '%';
  }
}
