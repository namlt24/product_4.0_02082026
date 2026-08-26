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
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EndpointConfig } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

interface KeyValueRow {
  key: string;
  value: string;
}

/**
 * "Thu ngay" + "OpenAPI spec" (P1) - trang RIENG, doc lap voi form/canvas. Goi
 * qua Control Plane (POST /api/endpoints/{id}/try) - dung CHINH composite
 * engine ma Data Plane that dung, khong phai gia lap rieng.
 *
 * KHONG co o form nhap "Query params": engine hien CHUA doc query param cua
 * chinh client lam nguon FieldMapping nao ca (xem ExecutionContext o backend -
 * field queryParams khong co getter, hoan toan khong duoc dung) - dua vao UI
 * 1 truong luon vo tac dung se gay hieu nham, nen bo hoan toan thay vi hien
 * "cho co".
 */
@Component({
  selector: 'app-endpoint-api-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
  ],
  templateUrl: './endpoint-api-details.component.html',
  styleUrl: './endpoint-api-details.component.scss',
})
export class EndpointApiDetailsComponent implements OnInit {
  readonly loading = signal(true);
  readonly endpoint = signal<EndpointConfig | null>(null);

  readonly pathParamRows = signal<KeyValueRow[]>([]);
  readonly requestBody = signal('');

  readonly trying = signal(false);
  readonly tryResult = signal<string | null>(null);
  readonly tryError = signal<string | null>(null);
  readonly tryStatus = signal<number | null>(null);

  readonly loadingSpec = signal(true);
  readonly specJson = signal<string | null>(null);
  readonly specError = signal<string | null>(null);
  readonly copied = signal(false);

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
      next: (ep) => {
        this.endpoint.set(ep);
        this.pathParamRows.set(this.extractPathTokens(ep.path).map((key) => ({ key, value: '' })));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Không tải được endpoint.', 'Đóng', { duration: 3000 });
      },
    });

    this.loadingSpec.set(true);
    this.api.getOpenApiSpec(this.endpointId).subscribe({
      next: (spec) => {
        this.specJson.set(JSON.stringify(spec, null, 2));
        this.loadingSpec.set(false);
      },
      error: (err) => {
        this.loadingSpec.set(false);
        this.specError.set(err?.error?.message ?? 'Không sinh được OpenAPI spec.');
      },
    });
  }

  /** Token dang {x} trong path - dung Y HET regex CompositeOrchestratorEngine.resolvePath() ben backend dung de khop that. */
  private extractPathTokens(path: string): string[] {
    const matches = path.match(/\{([a-zA-Z0-9_]+)}/g) ?? [];
    return matches.map((m) => m.slice(1, -1));
  }

  runTry(): void {
    this.trying.set(true);
    this.tryResult.set(null);
    this.tryError.set(null);
    this.tryStatus.set(null);

    const pathVariables: Record<string, string> = {};
    for (const row of this.pathParamRows()) {
      pathVariables[row.key] = row.value;
    }

    this.api
      .tryEndpoint(this.endpointId, {
        pathVariables,
        queryParams: {},
        body: this.requestBody().trim() || null,
      })
      .subscribe({
        next: (result) => {
          this.trying.set(false);
          this.tryStatus.set(200);
          this.tryResult.set(JSON.stringify(result, null, 2));
        },
        error: (err) => {
          this.trying.set(false);
          this.tryStatus.set(err?.status ?? null);
          this.tryError.set(JSON.stringify(err?.error ?? { message: 'Không rõ lỗi.' }, null, 2));
        },
      });
  }

  copySpec(): void {
    const spec = this.specJson();
    if (!spec) return;
    navigator.clipboard.writeText(spec).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }

  downloadSpec(): void {
    const spec = this.specJson();
    const ep = this.endpoint();
    if (!spec || !ep) return;
    const blob = new Blob([spec], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `openapi-${ep.path.replace(/[^a-zA-Z0-9]+/g, '-')}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
