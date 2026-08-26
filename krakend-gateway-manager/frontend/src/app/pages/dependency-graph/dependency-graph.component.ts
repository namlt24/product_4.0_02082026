import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, computed, OnInit, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { DependencyGraph, GraphEdge, GraphNode } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

const NODE_W = 210;
const NODE_H = 64;
const COL_GAP = 26;
const ROW_GAP = 58;
const CARD_PADDING = 18;

interface PositionedNode {
  node: GraphNode;
  x: number;
  y: number;
}

interface PositionedEdge {
  edge: GraphEdge;
  path: string;
  danger: boolean;
}

interface ComponentLayout {
  componentId: number;
  nodeCount: number;
  matches: boolean;
  positionedNodes: PositionedNode[];
  positionedEdges: PositionedEdge[];
  width: number;
  height: number;
}

/**
 * Ve so do phu thuoc giua cac endpoint.
 *
 * THIET KE DE SCALE TOI HANG NGAN ENDPOINT (khong phai 1 so do khong lo gop
 * chung - xem thao luan "1000 API to co nao"):
 * - Tach theo "connected component" (backend tinh san componentId) - moi cum
 *   endpoint co lien quan truc/gian tiep voi nhau duoc ve rieng thanh 1 khoi
 *   nho, KHONG gop toan bo he thong vao 1 layout duy nhat.
 * - Endpoint KHONG co quan he phu thuoc nao (da so trong 1 he thong lon) bi
 *   AN MAC DINH khoi so do, chi hien trong 1 BANG CO PHAN TRANG + tim kiem
 *   rieng (MatTableDataSource) khi nguoi dung bat toggle - khong render hang
 *   nghin box vo nghia len canvas.
 * - Co o tim kiem loc ca cum lien thong lan bang node co lap theo ten/path.
 */
@Component({
  selector: 'app-dependency-graph',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
  ],
  templateUrl: './dependency-graph.component.html',
  styleUrl: './dependency-graph.component.scss',
})
export class DependencyGraphComponent implements OnInit, AfterViewInit {
  readonly loading = signal(true);
  readonly graph = signal<DependencyGraph | null>(null);
  readonly searchTerm = signal('');
  readonly showIsolated = signal(false);

  readonly isolatedColumns = ['method', 'path', 'name', 'actions'];
  readonly isolatedDataSource = new MatTableDataSource<GraphNode>([]);

  @ViewChild(MatPaginator) private paginator?: MatPaginator;

  /** Tach danh sach node thanh 2 nhom: cum co quan he (>=1 canh) va node hoan toan co lap. */
  private readonly grouped = computed(() => {
    const g = this.graph();
    const byComponent = new Map<number, GraphNode[]>();
    for (const n of g?.nodes ?? []) {
      const arr = byComponent.get(n.componentId) ?? [];
      arr.push(n);
      byComponent.set(n.componentId, arr);
    }

    const connected: GraphNode[][] = [];
    const isolated: GraphNode[] = [];
    for (const group of byComponent.values()) {
      if (group.length > 1 || group.some((n) => !n.isolated)) {
        connected.push(group);
      } else {
        isolated.push(...group);
      }
    }
    connected.sort((a, b) => b.length - a.length);
    return { connected, isolated };
  });

  readonly isolatedCount = computed(() => this.grouped().isolated.length);
  readonly connectedNodeCount = computed(() =>
    this.grouped().connected.reduce((sum, g) => sum + g.length, 0),
  );
  readonly componentCount = computed(() => this.grouped().connected.length);

  /** Layout tung cum RIENG BIET - moi cum 1 SVG nho, khong gop chung 1 canvas khong lo. */
  readonly componentLayouts = computed<ComponentLayout[]>(() => {
    const g = this.graph();
    if (!g) return [];
    const term = this.searchTerm().trim().toLowerCase();

    return this.grouped().connected.map((nodes) => {
      const ids = new Set(nodes.map((n) => n.id));
      const edges = g.edges.filter((e) => ids.has(e.fromEndpointId) && ids.has(e.toEndpointId));
      const layout = this.layoutComponent(nodes, edges);
      const matches =
        !term || nodes.some((n) => n.name.toLowerCase().includes(term) || n.path.toLowerCase().includes(term));
      return {
        componentId: nodes[0].componentId,
        nodeCount: nodes.length,
        matches,
        ...layout,
      };
    });
  });

  readonly visibleComponentLayouts = computed(() => {
    const term = this.searchTerm().trim();
    const all = this.componentLayouts();
    return term ? all.filter((c) => c.matches) : all;
  });

  constructor(
    private readonly api: EndpointApiService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  ngAfterViewInit(): void {
    if (this.paginator) {
      this.isolatedDataSource.paginator = this.paginator;
    }
  }

  private fetch(): void {
    this.loading.set(true);
    this.api.getDependencyGraph().subscribe({
      next: (g) => {
        this.graph.set(g);
        this.isolatedDataSource.data = this.grouped().isolated;
        this.isolatedDataSource.filterPredicate = (node, filter) =>
          node.name.toLowerCase().includes(filter) || node.path.toLowerCase().includes(filter);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
    this.isolatedDataSource.filter = term.trim().toLowerCase();
  }

  refresh(): void {
    this.fetch();
  }

  openEndpoint(node: GraphNode): void {
    this.router.navigate(['/endpoints', node.id, 'edit']);
  }

  /** Layout theo tang (layer) NHUNG CHI TRONG PHAM VI 1 cum - do rong chi phu thuoc so node cua CUM DO, khong phai toan he thong. */
  private layoutComponent(
    nodes: GraphNode[],
    edges: GraphEdge[],
  ): { positionedNodes: PositionedNode[]; positionedEdges: PositionedEdge[]; width: number; height: number } {
    const layers = Array.from(new Set(nodes.map((n) => n.layer))).sort((a, b) => b - a);

    let maxRowWidth = 0;
    const rowWidths = layers.map((layerValue) => {
      const count = nodes.filter((n) => n.layer === layerValue).length;
      const w = count * NODE_W + (count - 1) * COL_GAP;
      maxRowWidth = Math.max(maxRowWidth, w);
      return w;
    });

    const positionedNodes: PositionedNode[] = [];
    layers.forEach((layerValue, rowIndex) => {
      const nodesInRow = nodes.filter((n) => n.layer === layerValue);
      const startX = CARD_PADDING + (maxRowWidth - rowWidths[rowIndex]) / 2;
      nodesInRow.forEach((node, i) => {
        positionedNodes.push({
          node,
          x: startX + i * (NODE_W + COL_GAP),
          y: CARD_PADDING + rowIndex * (NODE_H + ROW_GAP),
        });
      });
    });

    const byId = new Map(positionedNodes.map((p) => [p.node.id, p]));
    const positionedEdges: PositionedEdge[] = edges
      .map((edge) => {
        const from = byId.get(edge.fromEndpointId);
        const to = byId.get(edge.toEndpointId);
        if (!from || !to) return null;

        const sameRow = from.y === to.y;
        const x1 = from.x + NODE_W / 2;
        const y1 = sameRow ? from.y + NODE_H / 2 : from.y + NODE_H;
        const x2 = to.x + NODE_W / 2;
        const y2 = sameRow ? to.y + NODE_H / 2 : to.y;
        const midY = (y1 + y2) / 2;
        const path = sameRow
          ? `M ${x1} ${y1} C ${x1} ${y1 - 46}, ${x2} ${y2 - 46}, ${x2} ${y2}`
          : `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`;

        return { edge, path, danger: from.node.inCycle && to.node.inCycle } satisfies PositionedEdge;
      })
      .filter((e): e is PositionedEdge => e !== null);

    return {
      positionedNodes,
      positionedEdges,
      width: maxRowWidth + CARD_PADDING * 2,
      height: CARD_PADDING * 2 + layers.length * NODE_H + (layers.length - 1) * ROW_GAP,
    };
  }
}
