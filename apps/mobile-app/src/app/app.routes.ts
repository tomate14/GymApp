import { Routes } from '@angular/router';
import { MobileShellComponent } from './components/mobile-shell/mobile-shell.component';
import { authGuard } from './guards/auth.guard';
import { forcePasswordChangeGuard } from './guards/force-password-change.guard';
import { loginGuard } from './guards/login.guard';
import { ChangePasswordPageComponent } from './pages/change-password/change-password.page';
import { ClassesPageComponent } from './pages/classes/classes.page';
import { LoginPageComponent } from './pages/login/login.page';
import { PaymentsPageComponent } from './pages/payments/payments.page';
import { PersonalRecordsPageComponent } from './pages/personal-records/personal-records.page';
import { ProfilePageComponent } from './pages/profile/profile.page';
import { RoutinesPageComponent } from './pages/routines/routines.page';

export const APP_ROUTES: Routes = [
  { path: 'login', component: LoginPageComponent, canActivate: [loginGuard] },
  { path: 'change-password', component: ChangePasswordPageComponent, canActivate: [forcePasswordChangeGuard] },
  {
    path: '',
    component: MobileShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'datos-personales' },
      { path: 'datos-personales', component: ProfilePageComponent, data: { title: 'Datos personales' } },
      { path: 'historial-de-pagos', component: PaymentsPageComponent, data: { title: 'Historial de pagos' } },
      { path: 'mis-rutinas', component: RoutinesPageComponent, data: { title: 'Mis rutinas' } },
      { path: 'pesos-maximos', component: PersonalRecordsPageComponent, data: { title: 'Pesos maximos' } },
      { path: 'clases', component: ClassesPageComponent, data: { title: 'Clases' } }
    ]
  },
  { path: '**', redirectTo: '' }
];
