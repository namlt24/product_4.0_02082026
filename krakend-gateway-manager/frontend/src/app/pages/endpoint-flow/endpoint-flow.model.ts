import { BackendStep, ConditionRule, conditionOperatorLabel } from '../../models/endpoint.model';

export type FlowEdgeKind = 'switch' | 'default' | 'true' | 'false' | 'error' | 'next';

export interface FlowEdge {
  from: number;
  to: number;
  kind: FlowEdgeKind;
  label: string;
  /** Tom tat dieu kien (hien tren tooltip/chi tiet). */
  detail: string;
}

export interface FlowNode {
  step: BackendStep;
  layer: number;
  x: number;
  y: number;
  /** Loai hien thi: switch, rẽ 2 nhánh, bắt đầu, kết thúc, hoặc bước thường. */
  kind: 'start' | 'switch' | 'branch' | 'end' | 'step';
}

export interface FlowGraph {
  nodes: FlowNode[];
  edges: FlowEdge[];
  width: number;
  height: number;
}

const NODE_W = 220;
const NODE_H = 78;
const GAP_X = 60;
const GAP_Y = 110;
const PAD = 40;

const SOURCE_LABELS: Record<string, string> = {
  REQUEST_BODY: 'body',
  STEP_RESPONSE: 'response',
  QUERY_PARAM: 'query',
  HEADER: 'header',
  SYSTEM_FLAG: 'cờ',
};

function sourceText(sourceType: string | null | undefined, field: string | null | undefined, stepOrder?: number | null): string {
  const base = sourceType ? SOURCE_LABELS[sourceType] ?? sourceType : '';
  if (sourceType === 'STEP_RESPONSE' && stepOrder != null) {
    return `step${stepOrder}.${field ?? ''}`;
  }
  if (sourceType === 'SYSTEM_FLAG') {
    return `cờ ${field ?? ''}`;
  }
  return `${base}.${field ?? ''}`;
}

/** Tom tat 1 nut dieu kien thanh chuoi doc duoc (vd "body.tier = gold"). */
export function summarizeRule(rule: ConditionRule): string {
  if (rule.children) {
    if (rule.quantifier) {
      const inner = rule.children.map(summarizeRule).join(` ${rule.logic === 'OR' ? 'HOẶC' : 'VÀ'} `);
      const head = rule.quantifier === 'COUNT'
        ? `đếm(${sourceText(rule.sourceType, rule.sourceField, rule.sourceStepOrder)}) ${conditionOperatorLabel(rule.countOperator)} ${rule.countValue ?? ''}`
        : `${rule.quantifier}(${sourceText(rule.sourceType, rule.sourceField, rule.sourceStepOrder)})`;
      return `${head}: [${inner}]`;
    }
    return '(' + rule.children.map(summarizeRule).join(` ${rule.logic === 'OR' ? 'HOẶC' : 'VÀ'} `) + ')';
  }
  const left = sourceText(rule.sourceType, rule.sourceField, rule.sourceStepOrder);
  const op = conditionOperatorLabel(rule.operator);
  if (rule.compareSourceField) {
    return `${left} ${op} ${sourceText(rule.compareSourceType, rule.compareSourceField, rule.compareSourceStepOrder)}`;
  }
  return rule.expectedValue != null && rule.expectedValue !== '' ? `${left} ${op} ${rule.expectedValue}` : `${left} ${op}`;
}

/** Tom tat cac nhom: nhom ngoai HOAC, nhom trong VA. */
export function summarizeGroups(groups: ConditionRule[][] | null | undefined): string {
  if (!groups || groups.length === 0) {
    return '';
  }
  return groups
    .map((g) => g.map(summarizeRule).join(' VÀ '))
    .join(' HOẶC ');
}

function isBranchStep(step: BackendStep): boolean {
  return !!step.conditionOperator || (step.switchBranches?.length ?? 0) > 0;
}

/** Dung do thi tu cau hinh endpoint: canh, tang (layer), toa do. Khong doi du lieu goc. */
export function buildFlow(source: { steps: BackendStep[] }): FlowGraph {
  const steps = [...source.steps].sort((a, b) => a.stepOrder - b.stepOrder);
  const orders = new Set(steps.map((s) => s.stepOrder));
  const edges: FlowEdge[] = [];

  const branchTargets = new Set<number>();
  for (const s of steps) {
    if ((s.switchBranches?.length ?? 0) > 0) {
      s.switchBranches!.forEach((b, i) => {
        if (b.nextStepOrder != null) {
          branchTargets.add(b.nextStepOrder);
          edges.push({ from: s.stepOrder, to: b.nextStepOrder, kind: 'switch', label: `Nhánh ${i + 1}`, detail: summarizeGroups(b.groups) });
        }
      });
      if (s.nextStepOrderIfFalse != null) {
        branchTargets.add(s.nextStepOrderIfFalse);
        edges.push({ from: s.stepOrder, to: s.nextStepOrderIfFalse, kind: 'default', label: 'Mặc định', detail: 'không nhánh nào khớp' });
      }
    } else if (s.conditionOperator) {
      const detail = summarizeGroups(s.conditionGroups) || `${s.conditionSourceType ?? ''}.${s.conditionSourceField ?? ''} ${conditionOperatorLabel(s.conditionOperator)} ${s.conditionExpectedValue ?? ''}`;
      if (s.nextStepOrderIfTrue != null) {
        branchTargets.add(s.nextStepOrderIfTrue);
        edges.push({ from: s.stepOrder, to: s.nextStepOrderIfTrue, kind: 'true', label: 'Đúng', detail });
      }
      if (s.nextStepOrderIfFalse != null) {
        branchTargets.add(s.nextStepOrderIfFalse);
        edges.push({ from: s.stepOrder, to: s.nextStepOrderIfFalse, kind: 'false', label: 'Sai', detail });
      }
    }
    if (s.onErrorStepOrder != null) {
      branchTargets.add(s.onErrorStepOrder);
      edges.push({ from: s.stepOrder, to: s.onErrorStepOrder, kind: 'error', label: 'Lỗi', detail: 'step lỗi (fallback)' });
    }
  }

  // Step tuan tu khong dieu kien va khong la dich cua nhanh: chay tiep step co stepOrder ke tiep (giong engine).
  steps.forEach((s, idx) => {
    if (isBranchStep(s) || branchTargets.has(s.stepOrder)) {
      return;
    }
    const next = steps[idx + 1];
    if (next && orders.has(next.stepOrder)) {
      edges.push({ from: s.stepOrder, to: next.stepOrder, kind: 'next', label: 'tiếp theo', detail: '' });
    }
  });

  // Tang: BFS tu step dau tien; step khong toi duoc dat o tang cuoi.
  const layer = new Map<number, number>();
  const start = steps[0]?.stepOrder;
  if (start != null) {
    layer.set(start, 0);
    const queue = [start];
    while (queue.length) {
      const cur = queue.shift()!;
      const d = layer.get(cur)!;
      for (const e of edges.filter((x) => x.from === cur)) {
        if (!layer.has(e.to)) {
          layer.set(e.to, d + 1);
          queue.push(e.to);
        }
      }
    }
  }
  const maxLayer = Math.max(0, ...layer.values());
  steps.forEach((s) => {
    if (!layer.has(s.stepOrder)) {
      layer.set(s.stepOrder, maxLayer + 1);
    }
  });

  const byLayer = new Map<number, BackendStep[]>();
  steps.forEach((s) => {
    const l = layer.get(s.stepOrder)!;
    byLayer.set(l, [...(byLayer.get(l) ?? []), s]);
  });
  const widest = Math.max(1, ...[...byLayer.values()].map((l) => l.length));
  const width = PAD * 2 + widest * NODE_W + (widest - 1) * GAP_X;
  const layerCount = Math.max(...layer.values()) + 1;
  const height = PAD * 2 + layerCount * NODE_H + (layerCount - 1) * GAP_Y;

  const nodes: FlowNode[] = [];
  for (const [l, list] of [...byLayer.entries()].sort((a, b) => a[0] - b[0])) {
    const rowWidth = list.length * NODE_W + (list.length - 1) * GAP_X;
    const left = (width - rowWidth) / 2;
    list.forEach((s, i) => {
      const hasOut = edges.some((e) => e.from === s.stepOrder);
      const kind: FlowNode['kind'] = s.stepOrder === start
        ? 'start'
        : (s.switchBranches?.length ?? 0) > 0
          ? 'switch'
          : s.conditionOperator
            ? 'branch'
            : hasOut ? 'step' : 'end';
      nodes.push({ step: s, layer: l, x: left + i * (NODE_W + GAP_X), y: PAD + l * (NODE_H + GAP_Y), kind });
    });
  }

  return { nodes, edges, width, height };
}

export function nodeCenter(n: FlowNode): { x: number; y: number } {
  return { x: n.x + NODE_W / 2, y: n.y + NODE_H / 2 };
}

/** Duong cong giua 2 node: tu day node nguon toi dinh node dich (canh quay nguoc di vong sang ben). */
export function edgePath(from: FlowNode, to: FlowNode, index: number): { d: string; lx: number; ly: number } {
  const offset = (index % 4) * 14;
  const x1 = from.x + NODE_W / 2 + (index % 3 - 1) * 12;
  const y1 = from.y + NODE_H;
  const x2 = to.x + NODE_W / 2;
  const y2 = to.y;
  if (to.layer <= from.layer) {
    const sideX = Math.max(from.x, to.x) + NODE_W + 30 + offset;
    return {
      d: `M ${from.x + NODE_W} ${from.y + NODE_H / 2} C ${sideX} ${from.y + NODE_H / 2}, ${sideX} ${to.y + NODE_H / 2}, ${to.x + NODE_W} ${to.y + NODE_H / 2}`,
      lx: sideX,
      ly: (from.y + to.y) / 2 + NODE_H / 2,
    };
  }
  const midY = (y1 + y2) / 2 + offset;
  return {
    d: `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`,
    lx: (x1 + x2) / 2,
    ly: midY,
  };
}

function mermaidText(s: string): string {
  return s.replace(/"/g, "'").replace(/\n/g, ' ');
}

/** Xuat do thi sang Mermaid (dan vao GitHub/mermaid.live). */
export function toMermaid(graph: FlowGraph): string {
  const lines = ['flowchart TD'];
  for (const n of graph.nodes) {
    const s = n.step;
    const title = mermaidText(`${s.stepOrder} · ${s.name}`);
    const sub = mermaidText(`${s.method} ${s.urlPattern}`);
    const text = `"${title}<br/>${sub}"`;
    const id = `s${s.stepOrder}`;
    lines.push(n.kind === 'switch' ? `  ${id}{${text}}` : `  ${id}[${text}]`);
  }
  for (const e of graph.edges) {
    const label = mermaidText(e.detail ? `${e.label}: ${e.detail}` : e.label);
    lines.push(`  s${e.from} -->|"${label}"| s${e.to}`);
  }
  return lines.join('\n');
}
