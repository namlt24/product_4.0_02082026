import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { DependencyGraph, EndpointConfig, GraphNode } from '../../models/endpoint.model';
import { EndpointApiService } from '../../services/endpoint-api.service';

/** Man hinh danh sach Endpoint: tim kiem, xoa, validate cau hinh (canh bao vong lap). */
@Component({
  selector: 'app-endpoint-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatCardModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './endpoint-list.component.html',
  styleUrl: './endpoint-list.component.scss',
})
export class EndpointListComponent implements OnInit {
  readonly displayedColumns = ['method', 'path', 'name', 'type', 'depends', 'steps', 'actions'];
  readonly endpoints = signal<EndpointConfig[]>([]);
  readonly loading = signal(false);
  readonly deploying = signal(false);
  searchTerm = '';

  /** Thong ke nhanh cho thanh tren cung - tinh lai tu dong moi khi danh sach thay doi. */
  readonly compositeCount = computed(
    () => this.endpoints().filter((e) => e.sequential && e.steps.length > 1).length,
  );
  readonly totalSteps = computed(() => this.endpoints().reduce((sum, e) => sum + e.steps.length, 0));

  /** Du lieu so do phu thuoc - dung de hien badge "Duoc dung boi N" / "Goi toi N" tren tung dong. */
  readonly graph = signal<DependencyGraph | null>(null);

  private readonly searchSubject = new Subject<string>();

  constructor(
    private readonly api: EndpointApiService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged()).subscribe((term) => this.fetch(term));
  }

  ngOnInit(): void {
    this.fetch();
    this.fetchGraph();
  }

  private fetchGraph(): void {
    this.api.getDependencyGraph().subscribe({
      next: (g) => this.graph.set(g),
      error: () => this.graph.set(null),
    });
  }

  /** Tra ve node trong so do phu thuoc ung voi 1 endpoint - dung tren template de hien badge. */
  graphNodeOf(ep: EndpointConfig): GraphNode | undefined {
    return this.graph()?.nodes.find((n) => n.id === ep.id);
  }

  onSearchChange(term: string): void {
    this.searchSubject.next(term);
  }

  private fetch(term = ''): void {
    this.loading.set(true);
    this.api.list(term).subscribe({
      next: (data) => {
        this.endpoints.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Khong tai duoc danh sach endpoint.', 'Dong', { duration: 3000 });
      },
    });
  }

  edit(ep: EndpointConfig): void {
    this.router.navigate(['/endpoints', ep.id, 'edit']);
  }

  remove(ep: EndpointConfig): void {
    if (!ep.id) return;

    const usedBy = this.graphNodeOf(ep)?.usedByCount ?? 0;
    const extraWarning =
      usedBy > 0
        ? `\n\nCẢNH BÁO: endpoint này đang được ${usedBy} endpoint khác gọi ngược vào (xem "Sơ đồ phụ thuộc"). Xoá có thể làm hỏng cấu hình của các endpoint đó.`
        : '';
    if (!confirm(`Xoá endpoint "${ep.path}"?${extraWarning}`)) return;

    this.api.delete(ep.id).subscribe({
      next: () => {
        this.snackBar.open('Đã xoá endpoint.', 'Đóng', { duration: 2000 });
        this.fetch(this.searchTerm);
        this.fetchGraph();
      },
      error: () => this.snackBar.open('Xoá thất bại.', 'Đóng', { duration: 3000 }),
    });
  }

  /**
   * Validate toan bo cau hinh (canh bao vong lap phu thuoc) + nap lai cache dinh
   * tuyen trong-process. Khac voi truoc day: MOI thay doi qua Luu/Xoa da co
   * hieu luc ngay tuc thi - nut nay chi con la buoc kiem tra thu cong tuy chon.
   */
  validateAndReload(): void {
    if (!confirm('Validate toan bo cau hinh (kiem tra vong lap phu thuoc) va nap lai cache dinh tuyen?')) return;
    this.deploying.set(true);
    this.api.deploy().subscribe({
      next: (result) => {
        this.deploying.set(false);
        this.snackBar.open(result.message, 'Đóng', { duration: 4000 });
        this.fetchGraph();
      },
      error: (err) => {
        this.deploying.set(false);
        const msg = err?.error?.message ?? 'Deploy that bai.';
        this.snackBar.open(msg, 'Dong', { duration: 5000 });
      },
    });
  }
}
