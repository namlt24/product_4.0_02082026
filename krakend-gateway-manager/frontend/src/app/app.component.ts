import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './services/auth.service';

/** Shell goc cua ung dung: toolbar sang mau + router-outlet. An toolbar o /login (chua dang nhap, chua co gi de dieu huong toi). */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  template: `
    @if (!isLoginPage()) {
      <header class="app-toolbar">
        <div class="toolbar-inner">
          <a class="brand" routerLink="/endpoints">
            <span class="brand-mark">
              <mat-icon>hub</mat-icon>
            </span>
            <span class="brand-text">
              <span class="brand-name">vOrchestra</span>
              <span class="brand-tag">Dynamic Composite API Orchestrator</span>
            </span>
          </a>

          <nav class="toolbar-nav">
            <a class="nav-link" routerLink="/endpoints" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: false }">
              <mat-icon inline="true">list_alt</mat-icon>
              Endpoints
            </a>
            <a class="nav-link" routerLink="/upstreams" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: false }">
              <mat-icon inline="true">dns</mat-icon>
              Upstream Services
            </a>
            <a class="nav-link" routerLink="/logs" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: false }">
              <mat-icon inline="true">manage_search</mat-icon>
              Tra cứu Log
            </a>
          </nav>

          <span class="spacer"></span>

          <button mat-icon-button matTooltip="Đăng xuất" (click)="logout()">
            <mat-icon>logout</mat-icon>
          </button>
        </div>
      </header>
    }

    <main class="app-content">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      .app-toolbar {
        position: sticky;
        top: 0;
        z-index: 10;
        background: var(--gwm-surface);
        border-bottom: 1px solid var(--gwm-border);
      }

      .toolbar-inner {
        max-width: 1320px;
        margin: 0 auto;
        height: 60px;
        padding: 0 24px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .brand {
        display: flex;
        align-items: center;
        gap: 10px;
        text-decoration: none;
        color: inherit;
        margin-right: 8px;
      }

      .brand-mark {
        width: 34px;
        height: 34px;
        border-radius: 9px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, var(--gwm-brand-red), var(--gwm-brand-red-dark));
        /* Vien trang mong quanh khoi do - cung co ro net "do/trang" thay vi chi
           dua vao 1 icon trang tren nen do. */
        box-shadow: 0 0 0 2px #fff, var(--gwm-shadow-sm);
        color: white;
        flex-shrink: 0;

        mat-icon {
          font-size: 19px;
          width: 19px;
          height: 19px;
        }
      }

      .brand-text {
        display: flex;
        flex-direction: column;
        line-height: 1.15;
      }

      .brand-name {
        font-weight: 800;
        font-size: 15px;
        color: var(--gwm-text);
        letter-spacing: -0.01em;
      }

      .brand-tag {
        font-size: 11px;
        color: var(--gwm-text-muted);
        font-weight: 500;
      }

      .toolbar-nav {
        display: flex;
        align-items: center;
        gap: 2px;
        margin-left: 12px;
      }

      .nav-link {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 12px;
        border-radius: var(--gwm-radius-sm);
        color: var(--gwm-text-secondary);
        text-decoration: none;
        font-size: 13.5px;
        font-weight: 600;
        transition: background-color 0.15s ease, color 0.15s ease;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
        }

        &:hover {
          background: var(--gwm-bg);
          color: var(--gwm-text);
        }

        &.active {
          background: var(--gwm-primary-light);
          color: var(--gwm-primary-dark);
        }
      }

      .spacer {
        flex: 1 1 auto;
      }

      .app-content {
        max-width: 1320px;
        margin: 0 auto;
        padding: 28px 24px 56px;
      }
    `,
  ],
})
export class AppComponent {
  readonly isLoginPage = signal(false);

  constructor(
    private readonly router: Router,
    private readonly auth: AuthService,
  ) {
    this.isLoginPage.set(this.router.url.startsWith('/login'));
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e) => {
      this.isLoginPage.set((e as NavigationEnd).urlAfterRedirects.startsWith('/login'));
    });
  }

  logout(): void {
    this.auth.clearKey();
    this.router.navigate(['/login']);
  }
}
