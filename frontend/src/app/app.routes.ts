import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'work-orders' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: 'work-orders',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/work-orders/work-order-list').then((m) => m.WorkOrderList),
  },
  {
    path: 'work-orders/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/work-orders/work-order-form').then((m) => m.WorkOrderForm),
  },
  {
    path: 'work-orders/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/work-orders/work-order-form').then((m) => m.WorkOrderForm),
  },
  { path: '**', redirectTo: 'work-orders' },
];
