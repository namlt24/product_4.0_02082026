import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { emptyUpstreamService, UpstreamService } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * Man hinh dang ky Upstream Service (backend that) - dung 1 lan, tai su dung
 * o nhieu BackendStep. Day la noi dat host/timeout/circuit-breaker/cache cho
 * tung backend, thay vi go tay tren tung step nhu truoc day.
 */
@Component({
  selector: 'app-upstream-services',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatCardModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './upstream-services.component.html',
  styleUrl: './upstream-services.component.scss',
})
export class UpstreamServicesComponent implements OnInit {
  readonly displayedColumns = ['name', 'baseHost', 'timeouts', 'resilience', 'cache', 'actions'];
  readonly upstreams = signal<UpstreamService[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly formOpen = signal(false);
  readonly editingId = signal<string | null>(null);

  form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: EndpointApiService,
    private readonly snackBar: MatSnackBar,
  ) {
    this.form = this.buildForm(emptyUpstreamService());
  }

  ngOnInit(): void {
    this.fetch();
  }

  private buildForm(u: UpstreamService): FormGroup {
    return this.fb.group({
      name: [u.name, Validators.required],
      description: [u.description ?? ''],
      baseHost: [u.baseHost, [Validators.required, Validators.pattern(/^https?:\/\//)]],
      connectTimeoutMs: [u.connectTimeoutMs, [Validators.required, Validators.min(100)]],
      readTimeoutMs: [u.readTimeoutMs, [Validators.required, Validators.min(100)]],
      circuitBreakerEnabled: [u.circuitBreakerEnabled],
      failureRateThreshold: [u.failureRateThreshold, [Validators.min(1), Validators.max(100)]],
      retryEnabled: [u.retryEnabled],
      cacheEnabled: [u.cacheEnabled],
      cacheTtlSeconds: [u.cacheTtlSeconds, [Validators.min(1)]],
    });
  }

  private fetch(): void {
    this.loading.set(true);
    this.api.listUpstreams().subscribe({
      next: (list) => {
        this.upstreams.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Khong tai duoc danh sach Upstream Service.', 'Dong', { duration: 3000 });
      },
    });
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form = this.buildForm(emptyUpstreamService());
    this.formOpen.set(true);
  }

  openEditForm(u: UpstreamService): void {
    this.editingId.set(u.id ?? null);
    this.form = this.buildForm(u);
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Vui long kiem tra lai cac truong bat buoc.', 'Dong', { duration: 3000 });
      return;
    }
    const payload = this.form.getRawValue() as UpstreamService;
    this.saving.set(true);
    const id = this.editingId();
    const request$ = id ? this.api.updateUpstream(id, payload) : this.api.createUpstream(payload);
    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.snackBar.open('Da luu Upstream Service.', 'Dong', { duration: 2500 });
        this.fetch();
      },
      error: (err) => {
        this.saving.set(false);
        this.snackBar.open(err?.error?.message ?? 'Luu Upstream Service that bai.', 'Dong', { duration: 4000 });
      },
    });
  }

  remove(u: UpstreamService): void {
    if (!u.id) return;
    if (!confirm(`Xoá Upstream Service "${u.name}"? Các BackendStep đang tham chiếu Upstream này sẽ lỗi nếu chưa đổi.`)) {
      return;
    }
    this.api.deleteUpstream(u.id).subscribe({
      next: () => {
        this.snackBar.open('Đã xoá Upstream Service.', 'Đóng', { duration: 2000 });
        this.fetch();
      },
      error: (err) => this.snackBar.open(err?.error?.message ?? 'Xoá thất bại.', 'Đóng', { duration: 4000 }),
    });
  }
}
