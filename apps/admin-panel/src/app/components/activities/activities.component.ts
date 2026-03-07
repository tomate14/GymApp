import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Activity, ActivityDraft, createEmptyActivityDraft } from '../../class/activity.model';
import { GymClass } from '../../class/gym-class.model';
import { Exercise } from '../../class/exercise.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ActivitiesService, ActivityPageFilters } from '../../services/activities.service';
import { ClassesService } from '../../services/classes.service';
import { ExercisesService } from '../../services/exercises.service';

type ActivityFilters = {
  name: string;
  description: string;
};

@Component({
  selector: 'app-activities',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './activities.component.html',
  styleUrl: './activities.component.css'
})
export class ActivitiesComponent implements OnInit {
  activitiesPage: PageResponse<Activity> = createEmptyPageResponse<Activity>();
  classRefs: GymClass[] = [];
  exerciseRefs: Exercise[] = [];

  activityDraft: ActivityDraft = createEmptyActivityDraft();
  editingActivityId: number | null = null;
  deleteCandidate: Activity | null = null;
  showForm = false;
  isLoading = false;
  apiError: string | null = null;

  filters: ActivityFilters = {
    name: '',
    description: ''
  };

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly activitiesService: ActivitiesService,
    private readonly classesService: ClassesService,
    private readonly exercisesService: ExercisesService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get filteredActivities(): Activity[] {
    return this.activitiesPage.content;
  }

  get totalPages(): number {
    return Math.max(1, this.activitiesPage.totalPages || 1);
  }

  get paginatedActivities(): Activity[] {
    return this.activitiesPage.content;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  saveActivity(): void {
    if (!this.activityDraft.name || !this.activityDraft.description) {
      return;
    }

    this.activitiesService.upsert(this.activityDraft, this.editingActivityId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la actividad en el backend.';
      }
    });
  }

  editActivity(activity: Activity): void {
    this.showForm = true;
    this.editingActivityId = activity.id;
    this.activityDraft = { ...activity };
  }

  cancelEdit(): void {
    this.editingActivityId = null;
    this.activityDraft = createEmptyActivityDraft();
  }

  askDelete(activity: Activity): void {
    this.deleteCandidate = activity;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const activityId = this.deleteCandidate.id;
    this.activitiesService.remove(activityId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingActivityId === activityId) {
          this.cancelEdit();
        }

        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar la actividad en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  classCount(activityId: number): number {
    return this.classRefs.filter((item) => item.activityId === activityId).length;
  }

  exerciseCount(activityId: number): number {
    return this.exerciseRefs.filter((item) => item.activityId === activityId).length;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadActivitiesPage();
  }

  clearFilters(): void {
    this.filters = { name: '', description: '' };
    this.currentPage = 1;
    this.loadActivitiesPage();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadActivitiesPage();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadActivitiesPage();
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      activitiesPage: this.activitiesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize),
      classRefs: this.classesService.findAll(),
      exerciseRefs: this.exercisesService.findAll()
    }).subscribe({
      next: (result) => {
        this.activitiesPage = result.activitiesPage;
        this.classRefs = result.classRefs;
        this.exerciseRefs = result.exerciseRefs;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener actividades del backend.';
        this.activitiesPage = createEmptyPageResponse<Activity>();
        this.classRefs = this.classesService.getAll();
        this.exerciseRefs = this.exercisesService.getAll();
        this.isLoading = false;
      }
    });
  }

  private loadActivitiesPage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.activitiesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize).subscribe({
      next: (pageResult) => {
        this.activitiesPage = pageResult;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener actividades del backend.';
        this.activitiesPage = createEmptyPageResponse<Activity>();
        this.isLoading = false;
      }
    });
  }

  private toPageFilters(): ActivityPageFilters {
    return {
      name: this.filters.name,
      description: this.filters.description
    };
  }
}
