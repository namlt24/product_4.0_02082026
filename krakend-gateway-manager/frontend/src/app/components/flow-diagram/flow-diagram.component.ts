import { CommonModule } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BackendStep, conditionOperatorLabel } from '../../models/endpoint.model';
import {
  FlowEdge,
  FlowEdgeKind,
  FlowGraph,
  buildFlow,
  edgePath,
  summarizeGroups,
  toMermaid,
} from '../../pages/endpoint-flow/endpoint-flow.model';

export interface FlowSource {
  steps: BackendStep[];
}

const EDGE_COLORS: Record<FlowEdgeKind, string> = {
  switch: '#7c3aed',
  default: '#64748b',
  true: '#16a34a',
  false: '#dc2626',
  error: '#d97706',
  next: '#94a3b8',
};

const NODE_FILL: Record<string, string> = {
  start: '#dbeafe',
  switch: '#ede9fe',
  branch: '#dcfce7',
  end: '#f1f5f9',
  step: '#ffffff',
};

/**
 * So do luong (chi doc) dung chung: trang /flow va panel nhung trong canvas.
 * Nhan cau hinh step qua input {@code source} - canvas truyen ban nhap hien tai (chua luu).
 */
@Component({
  selector: 'app-flow-diagram',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div class="flow-tools">
      <button mat-stroked-button type="button" (click)="zoom(-0.1)" [disabled]="scale() <= 0.5"><mat-icon>zoom_out</mat-icon></button>
      <span class="scale">{{ (scale() * 100) | number: '1.0-0' }}%</span>
      <button mat-stroked-button type="button" (click)="zoom(0.1)" [disabled]="scale() >= 1.5"><mat-icon>zoom_in</mat-icon></button>
      <button mat-stroked-button type="button" (click)="scale.set(1)">Đặt lại</button>
      <button mat-stroked-button type="button" (click)="showMermaid.set(!showMermaid())"><mat-icon>code</mat-icon> Mermaid</button>
    </div>

    @if (showMermaid()) {
      <section class="mermaid-box">
        <div class="mermaid-head">
          <span>Dán vào GitHub (khối code mermaid) hoặc mermaid.live</span>
          <button mat-stroked-button type="button" (click)="copyMermaid()"><mat-icon>content_copy</mat-icon> Sao chép</button>
        </div>
        <pre>{{ mermaid() }}</pre>
      </section>
    }

    @if (graph(); as g) {
      <div class="flow-body">
        <div class="canvas-wrap">
          <svg [attr.width]="g.width * scale()" [attr.height]="g.height * scale()" [attr.viewBox]="'0 0 ' + g.width + ' ' + g.height">
            <defs>
              @for (k of edgeKinds; track k) {
                <marker [attr.id]="'arr-' + k" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
                  <path d="M 0 0 L 10 5 L 0 10 z" [attr.fill]="color(k)" />
                </marker>
              }
            </defs>

            @for (e of g.edges; track $index; let i = $index) {
              @if (geom(g, e, i); as p) {
                <path [attr.d]="p.d" fill="none" [attr.stroke]="color(e.kind)" stroke-width="2"
                      [attr.stroke-dasharray]="e.kind === 'next' ? '4 4' : null"
                      [attr.marker-end]="'url(#arr-' + e.kind + ')'" />
                <text [attr.x]="p.lx" [attr.y]="p.ly" text-anchor="middle" class="edge-label" [attr.fill]="color(e.kind)">{{ e.label }}</text>
              }
            }

            @for (n of g.nodes; track n.step.stepOrder) {
              <g class="node" (click)="selected.set(n.step.stepOrder)" [class.selected]="selected() === n.step.stepOrder">
                <rect [attr.x]="n.x" [attr.y]="n.y" width="220" height="78" rx="10"
                      [attr.fill]="fill(n.kind)" [attr.stroke]="selected() === n.step.stepOrder ? '#1d4ed8' : '#cbd5e1'" stroke-width="2" />
                <text [attr.x]="n.x + 12" [attr.y]="n.y + 24" class="node-title">{{ n.step.stepOrder }} · {{ n.step.name }}</text>
                <text [attr.x]="n.x + 12" [attr.y]="n.y + 44" class="node-sub">{{ n.step.method }} {{ n.step.urlPattern }}</text>
                <text [attr.x]="n.x + 12" [attr.y]="n.y + 64" class="node-kind">{{ kindLabel(n.kind, n.step) }}</text>
              </g>
            }
          </svg>
        </div>

        <aside class="detail">
          @if (selectedNode(); as n) {
            <h2>Step {{ n.step.stepOrder }} · {{ n.step.name }}</h2>
            <p><b>Gọi:</b> {{ n.step.method }} {{ n.step.urlPattern }}</p>
            <p><b>Upstream:</b> {{ n.step.upstreamServiceName || n.step.upstreamServiceId }}</p>

            @if ((n.step.switchBranches?.length ?? 0) > 0) {
              <h3>Rẽ nhiều nhánh</h3>
              @for (b of n.step.switchBranches; track $index; let bi = $index) {
                <p class="branch"><b>Nhánh {{ bi + 1 }}</b> → Step {{ b.nextStepOrder ?? '—' }}<br/>
                  <span class="cond">{{ summary(b.groups) }}</span></p>
              }
              <p class="branch"><b>Mặc định</b> → Step {{ n.step.nextStepOrderIfFalse ?? 'kết thúc' }}</p>
            } @else if (n.step.conditionOperator) {
              <h3>Điều kiện 2 nhánh</h3>
              <p class="cond">{{ summary(n.step.conditionGroups) || (n.step.conditionSourceType + '.' + n.step.conditionSourceField + ' ' + opLabel(n.step.conditionOperator)) }}</p>
              <p><b>Đúng</b> → Step {{ n.step.nextStepOrderIfTrue ?? 'kết thúc' }} · <b>Sai</b> → Step {{ n.step.nextStepOrderIfFalse ?? 'kết thúc' }}</p>
            }

            @if (n.step.falseErrorCode) {
              <p><b>Sai / không khớp</b> → báo lỗi <code>{{ n.step.falseErrorCode }}</code>: {{ n.step.falseErrorMessage }}</p>
            }
            @if (n.step.onErrorStepOrder != null) {
              <p><b>Khi lỗi</b> → Step {{ n.step.onErrorStepOrder }}</p>
            }
            @if (n.step.compensationUpstreamServiceId) {
              <p><b>Bù trừ:</b> {{ n.step.compensationMethod }} {{ n.step.compensationUrlPattern }}</p>
            }
            @if (n.step.parallelGroup != null) {
              <p><b>Nhóm song song:</b> {{ n.step.parallelGroup }}</p>
            }
          } @else {
            <p class="hint">Bấm vào một step trong sơ đồ để xem chi tiết điều kiện và đường đi.</p>
          }
        </aside>
      </div>
    } @else {
      <p class="hint">Chưa có step nào để vẽ sơ đồ.</p>
    }
  `,
  styles: [`
    .flow-tools { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
    .scale { min-width: 48px; text-align: center; font-size: 13px; }
    .flow-body { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 14px; }
    .canvas-wrap { overflow: auto; border: 1px solid #e2e8f0; border-radius: 10px; background: #fafafa; max-height: 560px; }
    .edge-label { font-size: 12px; font-weight: 600; paint-order: stroke; stroke: #fafafa; stroke-width: 4px; }
    .node { cursor: pointer; }
    .node-title { font-size: 14px; font-weight: 600; fill: #0f172a; }
    .node-sub { font-size: 12px; fill: #475569; font-family: monospace; }
    .node-kind { font-size: 11px; fill: #64748b; }
    .node.selected rect { filter: drop-shadow(0 0 4px rgba(29, 78, 216, 0.5)); }
    .detail { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 14px; max-height: 560px; overflow: auto; font-size: 13px; }
    .detail h2 { font-size: 16px; margin: 0 0 8px; }
    .detail h3 { font-size: 14px; margin: 12px 0 6px; }
    .cond { font-family: monospace; font-size: 12px; opacity: 0.85; }
    .branch { margin: 6px 0; }
    .hint { opacity: 0.7; }
    .mermaid-box { border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px; margin-bottom: 10px; background: #fff; }
    .mermaid-head { display: flex; justify-content: space-between; align-items: center; font-size: 13px; margin-bottom: 6px; }
    .mermaid-box pre { white-space: pre-wrap; font-size: 12px; margin: 0; max-height: 220px; overflow: auto; }
  `],
})
export class FlowDiagramComponent {
  private snack = inject(MatSnackBar);

  readonly source = input<FlowSource | null>(null);

  readonly scale = signal(1);
  readonly selected = signal<number | null>(null);
  readonly showMermaid = signal(false);
  readonly edgeKinds: FlowEdgeKind[] = ['switch', 'default', 'true', 'false', 'error', 'next'];

  readonly graph = computed<FlowGraph | null>(() => {
    const s = this.source();
    return s && s.steps.length ? buildFlow(s) : null;
  });
  readonly mermaid = computed(() => (this.graph() ? toMermaid(this.graph()!) : ''));
  readonly selectedNode = computed(() => this.graph()?.nodes.find((n) => n.step.stepOrder === this.selected()) ?? null);

  zoom(delta: number): void {
    this.scale.set(Math.round((this.scale() + delta) * 10) / 10);
  }

  color(kind: FlowEdgeKind): string {
    return EDGE_COLORS[kind];
  }

  fill(kind: string): string {
    return NODE_FILL[kind] ?? '#ffffff';
  }

  kindLabel(kind: string, step: BackendStep): string {
    switch (kind) {
      case 'start': return 'Bắt đầu';
      case 'switch': return `Switch · ${step.switchBranches?.length ?? 0} nhánh`;
      case 'branch': return 'Rẽ 2 nhánh';
      case 'end': return 'Kết thúc';
      default: return 'Bước';
    }
  }

  geom(g: FlowGraph, e: FlowEdge, index: number): { d: string; lx: number; ly: number } | null {
    const from = g.nodes.find((n) => n.step.stepOrder === e.from);
    const to = g.nodes.find((n) => n.step.stepOrder === e.to);
    if (!from || !to) return null;
    return edgePath(from, to, index);
  }

  summary(groups: unknown): string {
    return summarizeGroups(groups as never);
  }

  opLabel(op: string): string {
    return conditionOperatorLabel(op);
  }

  copyMermaid(): void {
    navigator.clipboard.writeText(this.mermaid()).then(
      () => this.snack.open('Đã sao chép Mermaid.', 'Đóng', { duration: 2000 }),
      () => this.snack.open('Không sao chép được, hãy chọn và copy thủ công.', 'Đóng', { duration: 3000 }),
    );
  }
}
