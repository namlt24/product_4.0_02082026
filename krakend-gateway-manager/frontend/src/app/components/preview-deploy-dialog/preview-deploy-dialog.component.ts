import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { JsonPreviewComponent } from '../json-preview/json-preview.component';

export interface PreviewDeployDialogData {
  json: unknown;
  warnings: string[];
}

/**
 * Dialog xac nhan truoc khi Deploy: hien thi toan bo krakend.json se duoc ghi
 * ra + canh bao (neu co), nguoi dung phai bam "Xac nhan Deploy" moi thuc su
 * ghi file va reload KrakenD.
 */
@Component({
  selector: 'app-preview-deploy-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, JsonPreviewComponent],
  template: `
    <div class="dialog-header">
      <div class="dialog-icon">
        <mat-icon>rocket_launch</mat-icon>
      </div>
      <div>
        <h2 mat-dialog-title>Xác nhận Deploy cấu hình</h2>
        <p class="dialog-subtitle">Ghi đè <code>krakend.json</code> và reload container KrakenD</p>
      </div>
    </div>

    <mat-dialog-content>
      <p class="dialog-note">
        Downtime dự kiến &lt; 2 giây (container restart, KrakenD stateless). Vui lòng kiểm tra
        lại JSON bên dưới trước khi xác nhận.
      </p>
      <app-json-preview [json]="data.json" [warnings]="data.warnings" />
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button color="primary" (click)="confirm()">
        <mat-icon>cloud_upload</mat-icon>
        Xác nhận Deploy
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-header {
        display: flex;
        align-items: flex-start;
        gap: 14px;
        padding: 20px 24px 0;
      }

      .dialog-icon {
        width: 40px;
        height: 40px;
        border-radius: 11px;
        background: var(--gwm-primary-light);
        color: var(--gwm-primary-dark);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      h2[mat-dialog-title] {
        margin: 0 !important;
        font-size: 17px !important;
      }

      .dialog-subtitle {
        margin: 2px 0 0;
        font-size: 12.5px;
        color: var(--gwm-text-secondary);

        code {
          background: var(--gwm-bg);
          padding: 1px 5px;
          border-radius: 4px;
        }
      }

      .dialog-note {
        font-size: 13px;
        color: var(--gwm-text-secondary);
        margin: 12px 0 16px;
        line-height: 1.5;
      }

      mat-dialog-actions {
        border-top: 1px solid var(--gwm-border);
        padding: 14px 24px !important;
      }
    `,
  ],
})
export class PreviewDeployDialogComponent {
  constructor(
    private readonly dialogRef: MatDialogRef<PreviewDeployDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: PreviewDeployDialogData,
  ) {}

  confirm(): void {
    this.dialogRef.close(true);
  }
}
