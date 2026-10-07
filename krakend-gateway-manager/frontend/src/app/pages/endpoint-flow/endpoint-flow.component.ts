import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { EndpointConfig } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';
import { FlowDiagramComponent } from '../../components/flow-diagram/flow-diagram.component';

/** Trang rieng /endpoints/:id/flow - cung noi dung voi panel "Sơ đồ" trong canvas. */
@Component({
  selector: 'app-endpoint-flow',
  standalone: true,
  imports: [RouterLink, MatButtonModule, FlowDiagramComponent],
  template: `
    <div class="flow-page">
      <header class="flow-header">
        <a [routerLink]="['/endpoints']" class="back">← Endpoints</a>
        <div class="title">
          <h1>Sơ đồ luồng</h1>
          @if (endpoint(); as ep) {
            <span class="sub">{{ ep.name }} · {{ ep.method }} {{ ep.path }}</span>
          }
        </div>
        @if (endpoint(); as ep) {
          <div class="actions">
            <a mat-stroked-button [routerLink]="['/endpoints', ep.id, 'canvas']">Mở canvas</a>
            <a mat-stroked-button [routerLink]="['/endpoints', ep.id, 'edit']">Mở form</a>
          </div>
        }
      </header>
      <app-flow-diagram [source]="endpoint()" />
    </div>
  `,
  styles: [`
    .flow-page { padding: 16px 24px; box-sizing: border-box; }
    .flow-header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 12px; }
    .flow-header h1 { margin: 0; font-size: 20px; }
    .sub { font-size: 13px; opacity: 0.75; }
    .actions { margin-left: auto; display: flex; gap: 8px; }
    .back { font-size: 13px; text-decoration: none; }
  `],
})
export class EndpointFlowComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(EndpointApiService);

  readonly endpoint = signal<EndpointConfig | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.get(id).subscribe((ep) => this.endpoint.set(ep));
    }
  }
}
