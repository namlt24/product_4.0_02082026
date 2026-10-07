import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  conditionOperatorLabel,
  ConditionOperator,
  ConditionRule,
  FieldMappingSourceType,
  newConditionLeaf,
  newConditionQuantifier,
  newConditionSubGroup,
  operatorNeedsExpectedValue,
} from '../../models/endpoint.model';

export interface ConditionFlagOption {
  flagCode: string;
  displayName?: string | null;
}

type TreePath = number[];

const VALUE_SOURCE_LABELS: Record<string, string> = {
  REQUEST_BODY: 'Body của client',
  STEP_RESPONSE: 'Response step trước',
  QUERY_PARAM: 'Query param của client',
  HEADER: 'Header của client',
  SYSTEM_FLAG: 'Cờ hệ thống',
};

const NUMERIC_OPERATORS: ConditionOperator[] = ['GREATER_THAN', 'GREATER_THAN_OR_EQUAL', 'LESS_THAN', 'LESS_THAN_OR_EQUAL'];

/** Danh sach node cua 1 "nhom" tai duong dan listPath: [gi] = nhom dau cap, [gi, ni, ...] = nhom con cua node. */
function listAtMut(root: ConditionRule[][], listPath: TreePath): ConditionRule[] {
  let list: ConditionRule[] = root[listPath[0]];
  for (let k = 1; k < listPath.length; k++) {
    list = list[listPath[k]].children!;
  }
  return list;
}

/** Xoa node tai path; nhom (hoac nhom con) het node thi xoa luon nhom do. */
function removeMut(root: ConditionRule[][], path: TreePath): void {
  if (path.length === 1) {
    root.splice(path[0], 1);
    return;
  }
  const parentPath = path.slice(0, -1);
  const list = listAtMut(root, parentPath);
  list.splice(path[path.length - 1], 1);
  if (list.length === 0) {
    removeMut(root, parentPath);
  }
}

@Component({
  selector: 'app-condition-tree-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTooltipModule,
  ],
  template: `
    <ng-template #listTpl let-list let-path="path">
      @for (node of list; track $index; let ni = $index) {
        <ng-container *ngTemplateOutlet="nodeTpl; context: { $implicit: node, path: childPath(path, ni) }"></ng-container>
      }
      <div class="list-actions">
        <button mat-button type="button" (click)="addLeaf(path)"><mat-icon>add</mat-icon> Thêm điều kiện</button>
        <button mat-button type="button" (click)="addSubGroup(path)"><mat-icon>account_tree</mat-icon> Thêm nhóm con</button>
        <button mat-button type="button" (click)="addQuantifier(path)"><mat-icon>repeat</mat-icon> Lặp mảng</button>
      </div>
    </ng-template>

    <ng-template #nodeTpl let-node let-path="path">
      @if (node.quantifier) {
        <div class="sub-group quant-group">
          <div class="node-header">
            <span class="node-label">Lặp mảng</span>
            <mat-form-field appearance="outline" class="source-field">
              <mat-label>Nguồn mảng</mat-label>
              <mat-select [ngModel]="node.sourceType" (ngModelChange)="node.sourceType = $event; commit()">
                @for (s of arraySourceTypes; track s) {
                  <mat-option [value]="s">{{ sourceLabel(s) }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            @if (node.sourceType === 'STEP_RESPONSE') {
              <mat-form-field appearance="outline" class="step-field">
                <mat-label>Step nguồn</mat-label>
                <mat-select [ngModel]="node.sourceStepOrder" (ngModelChange)="node.sourceStepOrder = $event; commit()">
                  @for (o of stepOrders; track o) {
                    <mat-option [value]="o">Step {{ o }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            }
            <mat-form-field appearance="outline" class="grow">
              <mat-label>Field mảng</mat-label>
              <input matInput [ngModel]="node.sourceField" (ngModelChange)="node.sourceField = $event; commit()" placeholder="vd: items" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="op-field">
              <mat-label>Kiểu lặp</mat-label>
              <mat-select [ngModel]="node.quantifier" (ngModelChange)="setQuantifier(node, $event); commit()">
                <mat-option value="ANY">Có phần tử nào thoả (ANY)</mat-option>
                <mat-option value="ALL">Mọi phần tử thoả (ALL)</mat-option>
                <mat-option value="COUNT">Đếm phần tử thoả (COUNT)</mat-option>
              </mat-select>
            </mat-form-field>
            @if (node.quantifier === 'COUNT') {
              <mat-form-field appearance="outline" class="op-field">
                <mat-label>Số phần tử thoả</mat-label>
                <mat-select [ngModel]="node.countOperator" (ngModelChange)="node.countOperator = $event; commit()">
                  @for (op of countOperators; track op) {
                    <mat-option [value]="op">{{ labelOf(op) }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" class="value-field">
                <mat-label>Ngưỡng</mat-label>
                <input matInput type="number" [ngModel]="node.countValue" (ngModelChange)="node.countValue = $event; commit()" />
              </mat-form-field>
            }
            <mat-form-field appearance="outline" class="logic-field">
              <mat-label>Nối các điều kiện</mat-label>
              <mat-select [ngModel]="node.logic || 'AND'" (ngModelChange)="node.logic = $event; commit()">
                <mat-option value="AND">VÀ (tất cả)</mat-option>
                <mat-option value="OR">HOẶC (một trong các)</mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-icon-button type="button" color="warn" (click)="removeNode(path)" matTooltip="Xóa lặp mảng">
              <mat-icon>delete</mat-icon>
            </button>
          </div>
          <p class="element-hint">Điều kiện cho TỪNG phần tử: field "Body của client" ở đây đọc từ chính phần tử.</p>
          <ng-container *ngTemplateOutlet="listTpl; context: { $implicit: node.children, path: path }"></ng-container>
        </div>
      } @else if (node.children) {
        <div class="sub-group">
          <div class="node-header">
            <span class="node-label">Nhóm con — điều kiện nối bằng</span>
            <mat-form-field appearance="outline" class="logic-field">
              <mat-select [ngModel]="node.logic || 'AND'" (ngModelChange)="node.logic = $event; commit()">
                <mat-option value="AND">VÀ (tất cả)</mat-option>
                <mat-option value="OR">HOẶC (một trong các)</mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-icon-button type="button" color="warn" (click)="removeNode(path)" matTooltip="Xóa nhóm con">
              <mat-icon>delete</mat-icon>
            </button>
          </div>
          <ng-container *ngTemplateOutlet="listTpl; context: { $implicit: node.children, path: path }"></ng-container>
        </div>
      } @else {
        <div class="leaf-row">
          <mat-form-field appearance="outline" class="source-field">
            <mat-label>Nguồn</mat-label>
            <mat-select [ngModel]="node.sourceType" (ngModelChange)="onSourceChange(node, $event); commit()">
              @for (s of sourceTypes; track s) {
                <mat-option [value]="s">{{ sourceLabel(s) }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          @if (node.sourceType === 'SYSTEM_FLAG') {
            <mat-form-field appearance="outline" class="grow">
              <mat-label>Cờ hệ thống</mat-label>
              <mat-select [ngModel]="node.sourceField" (ngModelChange)="node.sourceField = $event; commit()">
                @for (f of flags; track f.flagCode) {
                  <mat-option [value]="f.flagCode">{{ f.flagCode }}{{ f.displayName && f.displayName !== f.flagCode ? ' — ' + f.displayName : '' }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline" class="op-field">
              <mat-label>Trạng thái</mat-label>
              <mat-select [ngModel]="node.expectedValue === 'false' ? 'false' : 'true'" (ngModelChange)="node.operator = 'EQUALS'; node.expectedValue = $event; commit()">
                <mat-option value="true">BẬT</mat-option>
                <mat-option value="false">TẮT</mat-option>
              </mat-select>
            </mat-form-field>
          } @else {
            @if (node.sourceType === 'STEP_RESPONSE' && !isCompare(node)) {
              <mat-form-field appearance="outline" class="step-field">
                <mat-label>Step nguồn</mat-label>
                <mat-select [ngModel]="node.sourceStepOrder" (ngModelChange)="node.sourceStepOrder = $event; commit()">
                  @for (o of stepOrders; track o) {
                    <mat-option [value]="o">Step {{ o }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            }
            <mat-form-field appearance="outline" class="grow">
              <mat-label>Field</mat-label>
              <input matInput [ngModel]="node.sourceField" (ngModelChange)="node.sourceField = $event; commit()" placeholder="vd: permissionCodes" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="op-field">
              <mat-label>Toán tử</mat-label>
              <mat-select [ngModel]="node.operator" (ngModelChange)="node.operator = $event; commit()">
                @for (op of operators; track op) {
                  <mat-option [value]="op">{{ labelOf(op) }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            @if (!isCompare(node) && needsValue(node)) {
              <mat-form-field appearance="outline" class="value-field">
                <mat-label>Giá trị</mat-label>
                <input matInput
                       [type]="isNumeric(node.operator) ? 'number' : 'text'"
                       [ngModel]="node.expectedValue"
                       (ngModelChange)="node.expectedValue = $event; commit()" />
              </mat-form-field>
            }
          }

          @if (node.sourceType !== 'SYSTEM_FLAG') {
            <mat-slide-toggle [ngModel]="isCompare(node)" (ngModelChange)="toggleCompare(node, $event); commit()" matTooltip="So sánh với một field khác thay vì giá trị cố định">
              So sánh với field
            </mat-slide-toggle>
          }

          @if (isCompare(node)) {
            <mat-form-field appearance="outline" class="source-field">
              <mat-label>Nguồn vế phải</mat-label>
              <mat-select [ngModel]="node.compareSourceType" (ngModelChange)="node.compareSourceType = $event; commit()">
                @for (s of compareSourceTypes; track s) {
                  <mat-option [value]="s">{{ sourceLabel(s) }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            @if (node.compareSourceType === 'STEP_RESPONSE') {
              <mat-form-field appearance="outline" class="step-field">
                <mat-label>Step vế phải</mat-label>
                <mat-select [ngModel]="node.compareSourceStepOrder" (ngModelChange)="node.compareSourceStepOrder = $event; commit()">
                  @for (o of stepOrders; track o) {
                    <mat-option [value]="o">Step {{ o }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            }
            <mat-form-field appearance="outline" class="grow">
              <mat-label>Field vế phải</mat-label>
              <input matInput [ngModel]="node.compareSourceField" (ngModelChange)="node.compareSourceField = $event; commit()" />
            </mat-form-field>
          }

          <button mat-icon-button type="button" color="warn" (click)="removeNode(path)" matTooltip="Xóa điều kiện">
            <mat-icon>close</mat-icon>
          </button>
        </div>
      }
    </ng-template>

    <div class="tree">
      @for (group of value; track $index; let gi = $index) {
        <div class="group-box">
          <div class="node-header">
            <span class="node-label">Nhóm {{ gi + 1 }} — tất cả phải đúng</span>
            <button mat-icon-button type="button" color="warn" (click)="removeNode([gi])" matTooltip="Xóa nhóm này">
              <mat-icon>delete</mat-icon>
            </button>
          </div>
          <ng-container *ngTemplateOutlet="listTpl; context: { $implicit: group, path: [gi] }"></ng-container>
        </div>
        @if (gi < value.length - 1) {
          <p class="or-divider">— HOẶC —</p>
        }
      }
      <button mat-stroked-button type="button" class="add-group" (click)="addGroup()">
        <mat-icon>add</mat-icon> Thêm nhóm HOẶC
      </button>
    </div>
  `,
  styles: [`
    .tree { display: flex; flex-direction: column; gap: 8px; }
    .group-box { border: 1px solid var(--mat-sys-outline-variant, #c4c7c5); border-radius: 8px; padding: 10px; }
    .sub-group { border-left: 3px solid var(--mat-sys-primary, #6750a4); margin: 6px 0 6px 6px; padding: 6px 8px; }
    .node-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .node-label { font-weight: 600; font-size: 13px; }
    .logic-field { width: 220px; }
    .leaf-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin: 4px 0; }
    .source-field { width: 200px; }
    .step-field { width: 130px; }
    .op-field { width: 180px; }
    .value-field { width: 160px; }
    .grow { flex: 1; min-width: 160px; }
    .list-actions { display: flex; gap: 4px; flex-wrap: wrap; }
    .or-divider { text-align: center; font-size: 12px; opacity: 0.7; margin: 2px 0; }
    .add-group { margin-top: 4px; }
  `],
})
export class ConditionTreeEditorComponent {
  @Input() value: ConditionRule[][] = [];
  @Input() stepOrders: number[] = [];
  @Input() flags: ConditionFlagOption[] = [];
  @Input() operators: ConditionOperator[] = [];
  @Output() valueChange = new EventEmitter<ConditionRule[][]>();

  readonly sourceTypes: FieldMappingSourceType[] = ['REQUEST_BODY', 'STEP_RESPONSE', 'QUERY_PARAM', 'HEADER', 'SYSTEM_FLAG'];
  readonly compareSourceTypes: FieldMappingSourceType[] = ['REQUEST_BODY', 'STEP_RESPONSE', 'QUERY_PARAM', 'HEADER'];
  readonly arraySourceTypes: FieldMappingSourceType[] = ['REQUEST_BODY', 'STEP_RESPONSE', 'QUERY_PARAM', 'HEADER'];
  readonly countOperators: ConditionOperator[] = ['EQUALS', 'GREATER_THAN', 'GREATER_THAN_OR_EQUAL', 'LESS_THAN', 'LESS_THAN_OR_EQUAL'];

  labelOf(op: string): string {
    return conditionOperatorLabel(op);
  }

  sourceLabel(s: string): string {
    return VALUE_SOURCE_LABELS[s] ?? s;
  }

  /** Che do so sanh 2 field duoc danh dau bang compareSourceType (field rong van la che do so sanh). */
  isCompare(node: ConditionRule): boolean {
    return node.compareSourceType != null;
  }

  needsValue(node: ConditionRule): boolean {
    return operatorNeedsExpectedValue(node.operator);
  }

  isNumeric(op: ConditionOperator): boolean {
    return NUMERIC_OPERATORS.includes(op);
  }

  childPath(path: TreePath, index: number): TreePath {
    return [...path, index];
  }

  /** Cap nhat node trong cay hien tai roi phat ra ban sao cho cha (cha khong giu tham chieu). */
  commit(): void {
    this.valueChange.emit(structuredClone(this.value));
  }

  private apply(mutate: (root: ConditionRule[][]) => void): void {
    const next = structuredClone(this.value);
    mutate(next);
    this.value = next;
    this.valueChange.emit(structuredClone(next));
  }

  addGroup(): void {
    this.apply((root) => root.push([newConditionLeaf()]));
  }

  addLeaf(path: TreePath): void {
    this.apply((root) => listAtMut(root, path).push(newConditionLeaf()));
  }

  addQuantifier(path: TreePath): void {
    this.apply((root) => listAtMut(root, path).push(newConditionQuantifier()));
  }

  /** Doi kieu lap: COUNT can toan tu + nguong mac dinh; ANY/ALL bo 2 truong do. */
  setQuantifier(node: ConditionRule, quantifier: string): void {
    node.quantifier = quantifier as ConditionRule['quantifier'];
    if (quantifier === 'COUNT') {
      node.countOperator = node.countOperator ?? 'GREATER_THAN_OR_EQUAL';
      node.countValue = node.countValue ?? '1';
    } else {
      node.countOperator = null;
      node.countValue = null;
    }
  }

  addSubGroup(path: TreePath): void {
    this.apply((root) => listAtMut(root, path).push(newConditionSubGroup()));
  }

  removeNode(path: TreePath): void {
    this.apply((root) => removeMut(root, path));
  }

  onSourceChange(node: ConditionRule, source: FieldMappingSourceType): void {
    node.sourceType = source;
    node.compareSourceField = null;
    node.compareSourceType = null;
    node.compareSourceStepOrder = null;
    if (source === 'SYSTEM_FLAG') {
      node.operator = 'EQUALS';
      node.expectedValue = 'true';
      node.sourceStepOrder = null;
    } else if (source !== 'STEP_RESPONSE') {
      node.sourceStepOrder = null;
    }
  }

  toggleCompare(node: ConditionRule, on: boolean): void {
    if (on) {
      node.compareSourceType = 'REQUEST_BODY';
      node.compareSourceField = '';
      node.expectedValue = null;
    } else {
      node.compareSourceType = null;
      node.compareSourceField = null;
      node.compareSourceStepOrder = null;
    }
  }
}

