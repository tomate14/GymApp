import { Routes } from '@angular/router';
import { ActivitiesComponent } from './components/activities/activities.component';
import { ClassesComponent } from './components/classes/classes.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ExercisesComponent } from './components/exercises/exercises.component';
import { MachinesComponent } from './components/machines/machines.component';
import { PlansComponent } from './components/plans/plans.component';
import { RoutinesComponent } from './components/routines/routines.component';
import { UsersComponent } from './components/users/users.component';

export const APP_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'usuarios', component: UsersComponent },
  { path: 'planes', component: PlansComponent },
  { path: 'actividades', component: ActivitiesComponent },
  { path: 'clases', component: ClassesComponent },
  { path: 'rutinas', component: RoutinesComponent },
  { path: 'ejercicios', component: ExercisesComponent },
  { path: 'maquinas', component: MachinesComponent },
  { path: '**', redirectTo: 'dashboard' }
];
