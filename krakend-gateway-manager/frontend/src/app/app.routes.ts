import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'endpoints', pathMatch: 'full' },
  {
    path: 'endpoints',
    loadComponent: () =>
      import('./pages/endpoint-list/endpoint-list.component').then((m) => m.EndpointListComponent),
  },
  {
    path: 'endpoints/new',
    loadComponent: () =>
      import('./pages/endpoint-form/endpoint-form.component').then((m) => m.EndpointFormComponent),
  },
  {
    path: 'endpoints/:id/edit',
    loadComponent: () =>
      import('./pages/endpoint-form/endpoint-form.component').then((m) => m.EndpointFormComponent),
  },
  {
    path: 'dependency-graph',
    loadComponent: () =>
      import('./pages/dependency-graph/dependency-graph.component').then((m) => m.DependencyGraphComponent),
  },
  {
    path: 'upstreams',
    loadComponent: () =>
      import('./pages/upstream-services/upstream-services.component').then((m) => m.UpstreamServicesComponent),
  },
  { path: '**', redirectTo: 'endpoints' },
];
