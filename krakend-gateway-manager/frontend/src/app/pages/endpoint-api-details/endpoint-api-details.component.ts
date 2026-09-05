import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EndpointConfig, extractQueryParamNames, TryResult } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';
import { TryPanelComponent, TryRunRequest } from '../../components/try-panel/try-panel.component';

/**
 * "Thu ngay" + "OpenAPI spec" (P1) - trang RIENG, doc lap voi form/canvas. Goi
 * qua Control Plane (POST /api/endpoints/{id}/try) - dung CHINH composite
 * engine ma Data Plane that dung, khong phai gia lap rieng.
 *
 * O nhap Query param duoc TryPanelComponent tu sinh dua tren
 * extractQueryParamNames(endpoint.mappings) - dung cho step nao co FieldMapping
 * sourceType=QUERY_PARAM (engine CO doc, xem CompositeOrchestratorEngine -
 * truoc day component nay tung ghi nham la engine khong ho tro nen bo hoan
 * toan o nhap, da sua).
 */
@Component({
  selector: 'app-endpoint-api-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TryPanelComponent,
  ],
  templateUrl: './endpoint-api-details.component.html',
  styleUrl: './endpoint-api-details.component.scss',
})
export class EndpointApiDetailsComponent implements OnInit {
  readonly loading = signal(true);
  readonly endpoint = signal<EndpointConfig | null>(null);

  readonly trying = signal(false);
  readonly outcome = signal<TryResult | null>(null);

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

  queryParamNames(): string[] {
    return extractQueryParamNames(this.endpoint()?.mappings ?? []);
  }

  /**
   * Backend gio LUON tra HTTP 200 (envelope TryResultDto - xem EndpointTryService)
   * ke ca khi orchestration that bai, nen nhanh `error:` o day chi con fire khi
   * GOI SAI API that su (mat mang, sai API key, endpointId khong ton tai...).
   */
  onRun(req: TryRunRequest): void {
    this.trying.set(true);
    this.outcome.set(null);
    this.api.tryEndpoint(this.endpointId, { pathVariables: req.pathVariables, queryParams: req.queryParams, body: req.body }).subscribe({
      next: (result) => {
        this.trying.set(false);
        this.outcome.set(result);
      },
      error: (err) => {
        this.trying.set(false);
        this.outcome.set({
          success: false,
          result: null,
          errorCode: err?.status ? `HTTP_${err.status}` : null,
          errorMessage: err?.error?.message ?? 'Không gọi được endpoint.',
          hops: [],
        });
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
