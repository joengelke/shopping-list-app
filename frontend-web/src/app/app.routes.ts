import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login';
import { OverviewComponent } from './features/overview/overview';
import { authGuard } from './core/auth/auth-guard';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full'},
    { path: 'login', component: LoginComponent},
    { path: 'overview', component: OverviewComponent, canActivate:[authGuard]}
];
