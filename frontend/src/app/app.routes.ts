import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
    canActivate: [authGuard],
  },
  {
    path: 'contracts/upload',
    loadComponent: () => import('./features/upload/upload').then((m) => m.Upload),
    canActivate: [authGuard],
  },
  {
    path: 'contracts/:id',
    loadComponent: () =>
      import('./features/contract-detail/contract-detail').then((m) => m.ContractDetail),
    canActivate: [authGuard],
  },
];
