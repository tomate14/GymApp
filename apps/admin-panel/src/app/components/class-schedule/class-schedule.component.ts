import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Activity } from '../../class/activity.model';
import {
  DashboardClassesFilters,
  DashboardClassRow,
  DashboardPageResponse
} from '../../class/dashboard.model';
import { User } from '../../class/user.model';
import { ActivitiesService } from '../../services/activities.service';
import { DashboardService } from '../../services/dashboard.service';
import { UsersService } from '../../services/users.service';
import { ClassesTableComponent } from '../dashboard/tables/classes-table/classes-table.component';

@Component({
  selector: 'app-class-schedule',
  standalone: true,
  imports: [CommonModule, ClassesTableComponent],
  templateUrl: './class-schedule.component.html',
  styleUrl: './class-schedule.component.css'
})
export class ClassScheduleComponent implements OnInit {
  activities: Activity[] = [];
  teachers: User[] = [];

  classesFilters: DashboardClassesFilters = {
    search: '',
    activityId: 0,
    teacherId: 0
  };

  classesPage = this.emptyPage<DashboardClassRow>();
  classesWeekStart = this.startOfWeek(new Date());
  isLoadingClasses = false;
  apiError: string | null = null;

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly activitiesService: ActivitiesService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.loadClasses();
  }

  onClassesFiltersChange(): void {
    this.loadClasses();
  }

  clearClassesFilters(): void {
    this.classesFilters = {
      search: '',
      activityId: 0,
      teacherId: 0
    };
    this.loadClasses();
  }

  goToPreviousClassesWeek(): void {
    this.classesWeekStart = this.addDays(this.classesWeekStart, -7);
    this.loadClasses();
  }

  goToNextClassesWeek(): void {
    this.classesWeekStart = this.addDays(this.classesWeekStart, 7);
    this.loadClasses();
  }

  get classesWeekStartIso(): string {
    return this.toIsoDate(this.classesWeekStart);
  }

  private loadOptions(): void {
    this.activitiesService.findAll().subscribe({
      next: (activities) => {
        this.activities = activities;
      }
    });
    this.usersService.findAll('profesor').subscribe({
      next: (teachers) => {
        this.teachers = teachers;
      }
    });
  }

  private loadClasses(): void {
    this.isLoadingClasses = true;
    const weekStart = this.classesWeekStartIso;
    const weekEnd = this.toIsoDate(this.addDays(this.classesWeekStart, 6));
    this.dashboardService
      .getClasses({
        search: this.classesFilters.search || undefined,
        activityId: this.classesFilters.activityId || undefined,
        teacherId: this.classesFilters.teacherId || undefined,
        fromDate: weekStart,
        toDate: weekEnd,
        page: 0,
        size: 100
      })
      .subscribe({
        next: (result) => {
          this.classesPage = result;
          this.isLoadingClasses = false;
        },
        error: () => {
          this.apiError = 'No se pudo obtener la agenda de clases.';
          this.isLoadingClasses = false;
        }
      });
  }

  private emptyPage<T>(): DashboardPageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 8,
      totalElements: 0,
      totalPages: 1
    };
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private startOfWeek(date: Date): Date {
    const value = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const day = value.getDay();
    const shift = day === 0 ? -6 : 1 - day;
    value.setDate(value.getDate() + shift);
    return value;
  }

  private addDays(date: Date, days: number): Date {
    const value = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    value.setDate(value.getDate() + days);
    return value;
  }
}
