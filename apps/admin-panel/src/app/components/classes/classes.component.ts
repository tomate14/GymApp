import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Activity } from '../../class/activity.model';
import { createEmptyClassDraft, GymClass, GymClassDraft } from '../../class/gym-class.model';
import { User } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ActivitiesService } from '../../services/activities.service';
import { ClassesService } from '../../services/classes.service';
import { UsersService } from '../../services/users.service';
import { ensureMinNumber } from '../../utils/id.util';

type ClassFilters = {
  className: string;
  activityId: number | 0;
  teacherId: number | 0;
  dateFrom: string;
  dateTo: string;
};

@Component({
  selector: 'app-classes',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './classes.component.html',
  styleUrl: './classes.component.css'
})
export class ClassesComponent implements OnInit {
  classes: GymClass[] = [];
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
    dateFrom: '',
    dateTo: ''
  };

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
    return this.classes.filter((item) => {
      const matchesName =
        this.filters.className.trim() === '' ||
        item.className.toLowerCase().includes(this.filters.className.trim().toLowerCase());
      const matchesActivity = this.filters.activityId === 0 || item.activityId === this.filters.activityId;
      const matchesTeacher = this.filters.teacherId === 0 || item.teacherId === this.filters.teacherId;
      const matchesFrom = this.filters.dateFrom === '' || item.date >= this.filters.dateFrom;
      const matchesTo = this.filters.dateTo === '' || item.date <= this.filters.dateTo;

      return matchesName && matchesActivity && matchesTeacher && matchesFrom && matchesTo;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredClasses.length / this.pageSize));
  }

  get paginatedClasses(): GymClass[] {
    const safePage = Math.min(this.currentPage, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredClasses.slice(start, start + this.pageSize);
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
    if (!this.classDraft.className || !this.classDraft.date || !this.classDraft.time) {
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
        this.onFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la clase en el backend.';
      }
    });
  }

  editClass(item: GymClass): void {
    this.showForm = true;
    this.editingClassId = item.id;
    this.classDraft = { ...item };
    this.classNoLimit = item.maxCapacity === null;
  }

  cancelEdit(): void {
    this.editingClassId = null;
    this.classNoLimit = false;
    this.classDraft = createEmptyClassDraft();
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
        this.onFilterChange();
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
  }

  clearFilters(): void {
    this.filters = {
      className: '',
      activityId: 0,
      teacherId: 0,
      dateFrom: '',
      dateTo: ''
    };
    this.currentPage = 1;
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      classes: this.classesService.findAll(),
      activities: this.activitiesService.findAll(),
      teachers: this.usersService.findAll('profesor')
    }).subscribe({
      next: (result) => {
        this.classes = result.classes;
        this.activities = result.activities;
        this.teachers = result.teachers;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener clases del backend.';
        this.classes = this.classesService.getAll();
        this.activities = this.activitiesService.getAll();
        this.teachers = this.usersService.getByType('profesor');
        this.isLoading = false;
      }
    });
  }
}
