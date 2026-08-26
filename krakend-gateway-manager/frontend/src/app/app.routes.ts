import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'endpoints', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'endpoints',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-list/endpoint-list.component').then((m) => m.EndpointListComponent),
  },
  {
    path: 'endpoints/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-form/endpoint-form.component').then((m) => m.EndpointFormComponent),
  },
  {
    path: 'endpoints/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-form/endpoint-form.component').then((m) => m.EndpointFormComponent),
  },
  {
    path: 'endpoints/:id/mapping-order',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/mapping-order/mapping-order.component').then((m) => m.MappingOrderComponent),
  },
  {
    path: 'endpoints/new/canvas',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-canvas/endpoint-canvas.component').then((m) => m.EndpointCanvasComponent),
  },
  {
    path: 'endpoints/:id/canvas',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-canvas/endpoint-canvas.component').then((m) => m.EndpointCanvasComponent),
  },
  {
    path: 'endpoints/:id/versions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-versions/endpoint-versions.component').then((m) => m.EndpointVersionsComponent),
  },
  {
    path: 'endpoints/:id/api',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/endpoint-api-details/endpoint-api-details.component').then((m) => m.EndpointApiDetailsComponent),
  },
  {
    path: 'dependency-graph',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dependency-graph/dependency-graph.component').then((m) => m.DependencyGraphComponent),
  },
  {
    path: 'upstreams',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/upstream-services/upstream-services.component').then((m) => m.UpstreamServicesComponent),
  },
  {
    path: 'upstreams/health',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/upstream-health/upstream-health.component').then((m) => m.UpstreamHealthComponent),
  },
  { path: '**', redirectTo: 'endpoints' },
];
