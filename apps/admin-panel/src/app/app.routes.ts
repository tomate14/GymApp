import { Routes } from '@angular/router';
import { ActivitiesComponent } from './components/activities/activities.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { ClassScheduleComponent } from './components/class-schedule/class-schedule.component';
import { ClassesComponent } from './components/classes/classes.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ExercisesComponent } from './components/exercises/exercises.component';
import { LoginComponent } from './components/login/login.component';
import { MachinesComponent } from './components/machines/machines.component';
import { PlansComponent } from './components/plans/plans.component';
import { RoutinesComponent } from './components/routines/routines.component';
import { UsersComponent } from './components/users/users.component';
import { authGuard } from './guards/auth.guard';
import { forcePasswordChangeGuard } from './guards/force-password-change.guard';
import { loginGuard } from './guards/login.guard';

export const APP_ROUTES: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'cambiar-password', component: ChangePasswordComponent, canActivate: [forcePasswordChangeGuard] },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'agenda-clases', component: ClassScheduleComponent },
      { path: 'usuarios', component: UsersComponent },
      { path: 'planes', component: PlansComponent },
      { path: 'actividades', component: ActivitiesComponent },
      { path: 'clases', component: ClassesComponent },
      { path: 'rutinas', component: RoutinesComponent },
      { path: 'ejercicios', component: ExercisesComponent },
      { path: 'maquinas', component: MachinesComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
