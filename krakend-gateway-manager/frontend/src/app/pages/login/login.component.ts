import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { EndpointApiService } from '../../services/endpoint-api.service';

/**
 * Man hinh nhap API key (X-Gateway-Admin-Key) de dung Control Plane (/api/**).
 * Xac thuc bang cach thu goi 1 API that (danh sach endpoint) truoc khi coi la
 * dang nhap thanh cong - tranh luu 1 key sai roi de nguoi dung tu doan qua
 * tung trang (moi request deu 401).
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="login-wrap">
      <mat-card class="login-card">
        <div class="login-header">
          <span class="login-mark"><mat-icon>hub</mat-icon></span>
          <div>
            <div class="login-title">vOrchestra</div>
            <div class="login-subtitle">Nhập API key để vào trang quản trị</div>
          </div>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>API key (X-Gateway-Admin-Key)</mat-label>
            <input matInput type="password" formControlName="apiKey" autocomplete="off" />
          </mat-form-field>

          <button
            mat-flat-button
            color="primary"
            type="submit"
            class="full-width"
            [disabled]="form.invalid || loading()"
          >
            @if (loading()) {
              <mat-spinner diameter="18" class="btn-spinner" />
            }
            Đăng nhập
          </button>
        </form>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .login-wrap {
        /* app.component.ts an han toolbar (60px) tren /login qua @if (!isLoginPage())
           - khong con can tru 60px nhu khi toolbar van chiem cho o cac trang khac. */
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .login-card {
        width: 380px;
        padding: 8px 4px;
      }

      .login-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px 16px 8px;
      }

      .login-mark {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, var(--gwm-brand-red), var(--gwm-brand-red-dark));
        box-shadow: 0 0 0 2px #fff, var(--gwm-shadow-sm);
        color: white;
        flex-shrink: 0;
      }

      .login-title {
        font-weight: 800;
        font-size: 16px;
        color: var(--gwm-text);
      }

      .login-subtitle {
        font-size: 12.5px;
        color: var(--gwm-text-muted);
      }

      form {
        padding: 8px 16px 16px;
        display: flex;
        flex-direction: column;
        gap: 4px;
      }

      .full-width {
        width: 100%;
      }

      .btn-spinner {
        display: inline-block;
        margin-right: 8px;
      }
    `,
  ],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly form = this.fb.group({
    apiKey: ['', [Validators.required]],
  });

  constructor(
    private readonly api: EndpointApiService,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly snackBar: MatSnackBar,
  ) {}

  submit(): void {
    if (this.form.invalid) return;
    const key = (this.form.value.apiKey ?? '').trim();
    if (!key) return;

    this.loading.set(true);
    // Luu tam de request thu ben duoi di kem dung header (interceptor doc key tu AuthService).
    this.auth.setKey(key);
    this.api.list().subscribe({
      next: () => {
        this.loading.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/endpoints';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.loading.set(false);
        this.auth.clearKey();
        this.snackBar.open('API key không đúng.', 'Đóng', { duration: 3000 });
      },
    });
  }
}
