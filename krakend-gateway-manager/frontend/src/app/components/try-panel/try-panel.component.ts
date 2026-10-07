import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TryResult } from '../../models/endpoint.model';

interface KeyValueRow {
  key: string;
  value: string;
}

export interface TryRunRequest {
  pathVariables: Record<string, string>;
  queryParams: Record<string, string>;
  body: string | null;
  /** V5 - Ghi đe giá trị cờ CHI trong lần chạy thử này (flagOverrides bên backend). */
  flagOverrides: Record<string, boolean>;
}

/**
 * Component thuan hien thi cho "Thu ngay"/"Thu nhanh" - KHONG tu goi API, chi
 * nhan `pathTemplate` (tu tach token {xxx} thanh o nhap) + `running`/`outcome`
 * qua @Input, phat su kien `run` qua @Output - noi goi (endpoint-api-details
 * cho endpoint DA LUU, endpoint-canvas cho draft CHUA LUU) tu quyet dinh goi
 * tryEndpoint() hay tryAdhoc() roi tra ket qua vao lai qua `outcome`. Dung 1
 * component chung de KHONG lap lai markup waterfall tung step (copy y het
 * cach trinh bay hop dang co o log-search.component.html) o 2 noi.
 */
@Component({
  selector: 'app-try-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './try-panel.component.html',
  styleUrl: './try-panel.component.scss',
})
export class TryPanelComponent implements OnChanges {
  @Input({ required: true }) pathTemplate = '';
  /** Ten cac query param client can truyen - xem extractQueryParamNames() trong endpoint.model.ts. */
  @Input() queryParamNames: string[] = [];
  /** V5 - Ma cờ endpoint dùng (extractFlagCodes) - sinh 1 toggle ghi đe cho từng cờ trong lần chạy thử. */
  @Input() flagCodes: string[] = [];
  @Input() running = false;
  @Input() outcome: TryResult | null = null;
  @Output() run = new EventEmitter<TryRunRequest>();

  pathParamRows: KeyValueRow[] = [];
  queryParamRows: KeyValueRow[] = [];
  body = '';
  /** Giá trị ghi đe cờ trong lần chạy thử - mặc định giữ nguyên theo toggle (mặc định BẬT cho dễ thử luồng chính). */
  flagOverrideRows: { code: string; value: boolean }[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pathTemplate']) {
      this.pathParamRows = this.extractPathTokens(this.pathTemplate).map((key) => ({ key, value: '' }));
    }
    if (changes['queryParamNames']) {
      this.queryParamRows = this.queryParamNames.map((key) => ({ key, value: '' }));
    }
    if (changes['flagCodes']) {
      // Giu gia tri toggle da chon neu cờ vẫn còn trong danh sách moi (caller co the bắn lại list).
      this.flagOverrideRows = this.flagCodes.map((code) => ({
        code,
        value: this.flagOverrideRows.find((r) => r.code === code)?.value ?? true,
      }));
    }
  }

  /** Token dang {x} trong path - dung Y HET regex CompositeOrchestratorEngine.resolvePath() ben backend dung de khop that. */
  private extractPathTokens(path: string): string[] {
    const matches = path.match(/\{([a-zA-Z0-9_]+)}/g) ?? [];
    return matches.map((m) => m.slice(1, -1));
  }

  /** V5 - Dong trace "Rule cờ khớp" (stepOrder=-1, stepName="RULE") - hien banner rieng tren waterfall. */
  ruleInfoHops(): string[] {
    return (this.outcome?.hops ?? [])
      .filter((h) => h.stepOrder < 0 && h.matchedFlagRuleInfo)
      .map((h) => h.matchedFlagRuleInfo as string);
  }

  submit(): void {
    const pathVariables: Record<string, string> = {};
    for (const row of this.pathParamRows) {
      pathVariables[row.key] = row.value;
    }
    const queryParams: Record<string, string> = {};
    for (const row of this.queryParamRows) {
      queryParams[row.key] = row.value;
    }
    const flagOverrides: Record<string, boolean> = {};
    for (const row of this.flagOverrideRows) {
      flagOverrides[row.code] = row.value;
    }
    this.run.emit({ pathVariables, queryParams, body: this.body.trim() || null, flagOverrides });
  }

  json(value: unknown): string {
    return JSON.stringify(value, null, 2);
  }
}
