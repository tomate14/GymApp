import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Activity } from '../../class/activity.model';
import { createEmptyClassDraft, GymClass, GymClassDraft, WeekDay } from '../../class/gym-class.model';
import { User } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ActivitiesService } from '../../services/activities.service';
import { ClassesService, GymClassPageFilters } from '../../services/classes.service';
import { UsersService } from '../../services/users.service';
import { ensureMinNumber } from '../../utils/id.util';

type ClassFilters = {
  className: string;
  activityId: number | 0;
  teacherId: number | 0;
  weekDay: WeekDay | '';
};

@Component({
  selector: 'app-classes',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './classes.component.html',
  styleUrl: './classes.component.css'
})
export class ClassesComponent implements OnInit {
  classesPage: PageResponse<GymClass> = createEmptyPageResponse<GymClass>();
  activities: Activity[] = [];
  teachers: User[] = [];

  classDraft: GymClassDraft = createEmptyClassDraft();
  editingClassId: number | null = null;
  classNoLimit = false;
  deleteCandidate: GymClass | null = null;
  showForm = false;
  isLoading = false;
  apiError: string | null = null;

  filters: ClassFilters = {
    className: '',
    activityId: 0,
    teacherId: 0,
    weekDay: ''
  };
  readonly weekDayOptions: Array<{ value: WeekDay; label: string }> = [
    { value: 'lunes', label: 'Lunes' },
    { value: 'martes', label: 'Martes' },
    { value: 'miercoles', label: 'Miercoles' },
    { value: 'jueves', label: 'Jueves' },
    { value: 'viernes', label: 'Viernes' },
    { value: 'sabado', label: 'Sabado' },
    { value: 'domingo', label: 'Domingo' }
  ];

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly classesService: ClassesService,
    private readonly activitiesService: ActivitiesService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get filteredClasses(): GymClass[] {
    return this.classesPage.content;
  }

  get totalPages(): number {
    return Math.max(1, this.classesPage.totalPages || 1);
  }

  get paginatedClasses(): GymClass[] {
    return this.classesPage.content;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  onNoLimitChanged(): void {
    if (this.classNoLimit) {
      this.classDraft.maxCapacity = null;
      return;
    }

    this.classDraft.maxCapacity = this.classDraft.maxCapacity ?? 20;
  }

  saveClass(): void {
    if (!this.classDraft.className || this.classDraft.weekDays.length === 0) {
      return;
    }
    if (!this.classDraft.allDay && (!this.classDraft.startTime || !this.classDraft.endTime)) {
      return;
    }
    if (!this.classDraft.allDay && this.classDraft.startTime >= this.classDraft.endTime) {
      return;
    }

    const maxCapacity = this.classNoLimit ? null : ensureMinNumber(this.classDraft.maxCapacity, 1);

    const payload: GymClassDraft = {
      ...this.classDraft,
      maxCapacity
    };

    this.classesService.upsert(payload, this.editingClassId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la clase en el backend.';
      }
    });
  }

  editClass(item: GymClass): void {
    this.showForm = true;
    this.editingClassId = item.id;
    this.classDraft = {
      ...item,
      weekDays: [...item.weekDays]
    };
    this.classNoLimit = item.maxCapacity === null;
  }

  cancelEdit(): void {
    this.editingClassId = null;
    this.classNoLimit = false;
    this.classDraft = createEmptyClassDraft();
  }

  onAllDayChanged(): void {
    if (!this.classDraft.allDay) {
      return;
    }
    this.classDraft.startTime = '';
    this.classDraft.endTime = '';
  }

  askDelete(item: GymClass): void {
    this.deleteCandidate = item;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const classId = this.deleteCandidate.id;
    this.classesService.remove(classId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingClassId === classId) {
          this.cancelEdit();
        }

        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar la clase en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  activityName(activityId: number | null): string {
    return this.activitiesService.nameById(activityId);
  }

  teacherName(userId: number | null): string {
    return this.usersService.fullName(userId);
  }

  capacityText(item: GymClass): string {
    if (item.maxCapacity === null) {
      return `${item.enrolled} / sin limite`;
    }

    return `${item.enrolled} / ${item.maxCapacity}`;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadClassesPage();
  }

  clearFilters(): void {
    this.filters = {
      className: '',
      activityId: 0,
      teacherId: 0,
      weekDay: ''
    };
    this.currentPage = 1;
    this.loadClassesPage();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadClassesPage();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadClassesPage();
  }

  isDaySelected(day: WeekDay): boolean {
    return this.classDraft.weekDays.includes(day);
  }

  toggleDay(day: WeekDay, checked: boolean): void {
    const selected = new Set(this.classDraft.weekDays);
    if (checked) {
      selected.add(day);
    } else {
      selected.delete(day);
    }
    this.classDraft.weekDays = this.weekDayOptions
      .map((option) => option.value)
      .filter((value) => selected.has(value));
  }

  weekDaysLabel(days: WeekDay[] | null | undefined): string {
    if (!days || days.length === 0) {
      return '-';
    }
    const labels = this.weekDayOptions
      .filter((option) => days.includes(option.value))
      .map((option) => option.label);
    return labels.join(', ');
  }

  scheduleText(item: GymClass): string {
    if (item.allDay) {
      return 'Todo el dia';
    }
    return `${item.startTime} - ${item.endTime}`;
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      classesPage: this.classesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize),
      activities: this.activitiesService.findAll(),
      teachers: this.usersService.findAll('profesor')
    }).subscribe({
      next: (result) => {
        this.classesPage = result.classesPage;
        this.activities = result.activities;
        this.teachers = result.teachers;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener clases del backend.';
        this.classesPage = createEmptyPageResponse<GymClass>();
        this.activities = this.activitiesService.getAll();
        this.teachers = this.usersService.getByType('profesor');
        this.isLoading = false;
      }
    });
  }

  private loadClassesPage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.classesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize).subscribe({
      next: (pageResult) => {
        this.classesPage = pageResult;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener clases del backend.';
        this.classesPage = createEmptyPageResponse<GymClass>();
        this.isLoading = false;
      }
    });
  }

  private toPageFilters(): GymClassPageFilters {
    return {
      className: this.filters.className,
      activityId: this.filters.activityId,
      teacherId: this.filters.teacherId,
      weekDay: this.filters.weekDay
    };
  }
}
