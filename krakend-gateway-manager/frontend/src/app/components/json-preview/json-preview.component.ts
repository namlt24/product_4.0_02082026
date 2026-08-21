import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Hien thi JSON duoc format dep + danh sach canh bao nghiep vu (neu co).
 * Dung lai o ca man hinh form (preview 1 endpoint) va dialog Deploy (preview toan bo).
 */
@Component({
  selector: 'app-json-preview',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule, MatSnackBarModule],
  template: `
    <div class="preview-header">
      <span class="preview-title">
        <mat-icon inline="true">data_object</mat-icon>
        krakend.json
      </span>
      <button mat-icon-button matTooltip="Copy JSON" (click)="copy()" [disabled]="!json" class="copy-btn">
        <mat-icon>content_copy</mat-icon>
      </button>
    </div>

    @if (loading) {
      <div class="skeleton-block">
        <div class="skeleton-line" style="width: 60%"></div>
        <div class="skeleton-line" style="width: 85%"></div>
        <div class="skeleton-line" style="width: 40%"></div>
        <div class="skeleton-line" style="width: 70%"></div>
      </div>
    } @else if (warnings.length) {
      <div class="warning-list">
        @for (w of warnings; track w) {
          <div class="warning-item">
            <mat-icon inline="true">warning_amber</mat-icon>
            <span>{{ w }}</span>
          </div>
        }
      </div>
    }

    @if (!loading) {
      <pre class="json-block">{{ json | json }}</pre>
    }
  `,
  styles: [
    `
      :host {
        display: block;
      }

      .preview-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
      }

      .preview-title {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 700;
        font-size: 13px;
        color: var(--gwm-text);
        font-family: var(--gwm-font-mono);

        mat-icon {
          font-size: 17px;
          width: 17px;
          height: 17px;
          color: var(--gwm-primary);
        }
      }

      .copy-btn {
        color: var(--gwm-text-muted);
        width: 32px;
        height: 32px;
        line-height: 32px;

        mat-icon {
          font-size: 17px;
          width: 17px;
          height: 17px;
        }

        &:hover {
          color: var(--gwm-primary);
        }
      }

      .warning-list {
        background: var(--gwm-warning-bg);
        border: 1px solid var(--gwm-warning-border);
        border-radius: var(--gwm-radius-md);
        padding: 10px 12px;
        margin-bottom: 10px;
      }

      .warning-item {
        display: flex;
        gap: 8px;
        align-items: flex-start;
        margin-bottom: 6px;
        font-size: 12.5px;
        color: var(--gwm-warning);
        line-height: 1.4;

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
          margin-top: 1px;
          flex-shrink: 0;
        }

        &:last-child {
          margin-bottom: 0;
        }
      }

      .json-block {
        background: #0f172a;
        color: #e2e8f0;
        padding: 16px;
        border-radius: var(--gwm-radius-md);
        max-height: 560px;
        overflow: auto;
        font-family: var(--gwm-font-mono);
        font-size: 12px;
        line-height: 1.6;
        margin: 0;
      }

      .skeleton-block {
        display: flex;
        flex-direction: column;
        gap: 10px;
        padding: 16px;
        background: var(--gwm-bg);
        border-radius: var(--gwm-radius-md);
      }

      .skeleton-line {
        height: 12px;
        border-radius: 6px;
        background: linear-gradient(90deg, var(--gwm-border) 25%, #eef1f5 37%, var(--gwm-border) 63%);
        background-size: 400% 100%;
        animation: gwm-shimmer 1.4s ease infinite;
      }

      @keyframes gwm-shimmer {
        0% { background-position: 100% 50%; }
        100% { background-position: 0 50%; }
      }
    `,
  ],
})
export class JsonPreviewComponent {
  @Input() json: unknown = null;
  @Input() warnings: string[] = [];
  @Input() loading = false;

  constructor(private readonly snackBar: MatSnackBar) {}

  copy(): void {
    if (!this.json) {
      return;
    }
    navigator.clipboard.writeText(JSON.stringify(this.json, null, 2)).then(() => {
      this.snackBar.open('Đã copy JSON vào clipboard', 'Đóng', { duration: 2000 });
    });
  }
}
