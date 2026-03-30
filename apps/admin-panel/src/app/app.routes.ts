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
import { RolesPermissionsComponent } from './components/roles-permissions/roles-permissions.component';
import { RoutinesComponent } from './components/routines/routines.component';
import { UsersComponent } from './components/users/users.component';
import { authGuard } from './guards/auth.guard';
import { forcePasswordChangeGuard } from './guards/force-password-change.guard';
import { loginGuard } from './guards/login.guard';
import { menuPermissionGuard } from './guards/menu-permission.guard';

export const APP_ROUTES: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'cambiar-password', component: ChangePasswordComponent, canActivate: [forcePasswordChangeGuard] },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'dashboard' } },
      { path: 'agenda-clases', component: ClassScheduleComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'agenda' } },
      { path: 'usuarios', component: UsersComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'users' } },
      { path: 'planes', component: PlansComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'plans' } },
      { path: 'actividades', component: ActivitiesComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'activities' } },
      { path: 'clases', component: ClassesComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'classes' } },
      { path: 'rutinas', component: RoutinesComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'routines' } },
      { path: 'ejercicios', component: ExercisesComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'exercises' } },
      { path: 'maquinas', component: MachinesComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'machines' } },
      { path: 'roles-permisos', component: RolesPermissionsComponent, canActivate: [menuPermissionGuard], data: { moduleKey: 'roles_permissions' } }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
