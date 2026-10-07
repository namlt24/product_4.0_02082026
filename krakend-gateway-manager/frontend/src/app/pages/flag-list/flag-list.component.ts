import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { GwmFlag, GwmFlagHistory } from '../../models/flag.model';
import { FlagApiService } from '../../services/flag-api.service';

/**
 * Trang "Cờ nghiệp vụ" - quản lý tập trung các cờ boolean của đội (GWM_FLAG).
 * Cờ được gắn vào endpoint qua Rule ("khop tổ hợp cờ -> bắt đầu từ step / chặn"),
 * toggle BẬT/TẮT ở đây là thao tác chính khi chạy thật - DP sync giá trị mới
 * trong chu kỳ sync 15s (xem RemoteConfigSyncService o backend).
 */
@Component({
  selector: 'app-flag-list',
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
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './flag-list.component.html',
  styleUrl: './flag-list.component.scss',
})
export class FlagListComponent implements OnInit {
  readonly displayedColumns = ['flagCode', 'displayName', 'enabled', 'updatedAt', 'actions'];

  readonly loading = signal(false);
  readonly items = signal<GwmFlag[]>([]);

  // ---- Dialog tao/sua cờ ----
  readonly dialogOpen = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly formFlagCode = signal('');
  readonly formDisplayName = signal('');
  readonly formDescription = signal('');
  readonly formEnabled = signal(false);
  readonly saving = signal(false);

  /** Row dang mo lich su doi gia tri (mo rong tu row - GWM_FLAG_HISTORY). */
  readonly expandedId = signal<string | null>(null);
  readonly historyLoading = signal(false);
  readonly historyCache = signal<Map<string, GwmFlagHistory[]>>(new Map());

  private readonly snackbarDuration = 3500;

  constructor(
    private readonly api: FlagApiService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snackBar.open(err?.error?.message ?? 'Không tải được danh sách cờ.', 'Đóng', { duration: this.snackbarDuration });
      },
    });
  }

  // ---- Tao / sua ----

  openCreateDialog(): void {
    this.editingId.set(null);
    this.formFlagCode.set('');
    this.formDisplayName.set('');
    this.formDescription.set('');
    this.formEnabled.set(false);
    this.dialogOpen.set(true);
  }

  openEditDialog(row: GwmFlag): void {
    this.editingId.set(row.id);
    this.formFlagCode.set(row.flagCode);
    this.formDisplayName.set(row.displayName);
    this.formDescription.set(row.description ?? '');
    this.formEnabled.set(row.enabled);
    this.dialogOpen.set(true);
  }

  closeDialog(): void {
    this.dialogOpen.set(false);
  }

  save(): void {
    if (!this.formFlagCode().trim() || !this.formDisplayName().trim()) return;
    this.saving.set(true);
    if (this.editingId()) {
      this.api.update(this.editingId()!, this.formDisplayName().trim(), this.formDescription().trim()).subscribe({
        next: () => {
          this.saving.set(false);
          this.dialogOpen.set(false);
          this.snackBar.open('Đã cập nhật cờ.', 'Đóng', { duration: this.snackbarDuration });
          this.fetch();
        },
        error: (err) => this.saveFailed(err),
      });
    } else {
      this.api.create(this.formFlagCode().trim(), this.formDisplayName().trim(), this.formDescription().trim(), this.formEnabled()).subscribe({
        next: () => {
          this.saving.set(false);
          this.dialogOpen.set(false);
          this.snackBar.open('Đã tạo cờ mới.', 'Đóng', { duration: this.snackbarDuration });
          this.fetch();
        },
        error: (err) => this.saveFailed(err),
      });
    }
  }

  private saveFailed(err: { error?: { message?: string } }): void {
    this.saving.set(false);
    this.snackBar.open(err?.error?.message ?? 'Không lưu được cờ.', 'Đóng', { duration: this.snackbarDuration });
  }

  // ---- Toggle + xoa + lich su ----

  toggle(row: GwmFlag): void {
    this.api.toggle(row.id).subscribe({
      next: (updated) => {
        this.snackBar.open(`Cờ "${row.flagCode}" đã ${updated.enabled ? 'BẬT' : 'TẮT'}.`, 'Đóng', { duration: this.snackbarDuration });
        this.fetch();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không toggle được cờ.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  delete(row: GwmFlag): void {
    if (!confirm(`Xóa cờ "${row.flagCode}"? Cờ đang được endpoint nào dùng sẽ KHÔNG xóa được (backend trả GW-FLAG-IN-USE).`)) {
      return;
    }
    this.api.delete(row.id).subscribe({
      next: () => {
        this.snackBar.open('Đã xóa cờ.', 'Đóng', { duration: this.snackbarDuration });
        this.fetch();
      },
      error: (err) =>
        this.snackBar.open(err?.error?.message ?? 'Không xóa được cờ.', 'Đóng', { duration: this.snackbarDuration }),
    });
  }

  toggleExpand(row: GwmFlag): void {
    const next = this.expandedId() === row.id ? null : row.id;
    this.expandedId.set(next);
    if (next && !this.historyCache().has(row.id)) {
      this.loadHistory(row.id);
    }
  }

  historyOf(row: GwmFlag): GwmFlagHistory[] {
    return this.historyCache().get(row.id) ?? [];
  }

  private loadHistory(id: string): void {
    this.historyLoading.set(true);
    this.api.history(id).subscribe({
      next: (list) => {
        const map = new Map(this.historyCache());
        map.set(id, list);
        this.historyCache.set(map);
        this.historyLoading.set(false);
      },
      error: () => this.historyLoading.set(false),
    });
  }

  formatTimestamp(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleString('vi-VN');
  }
}
