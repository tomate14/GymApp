import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Activity } from '../../class/activity.model';
import { createEmptyExerciseDraft, Exercise, ExerciseDraft } from '../../class/exercise.model';
import { Machine } from '../../class/machine.model';
import { RoutineTemplate } from '../../class/routine.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ActivitiesService } from '../../services/activities.service';
import { ExercisesService } from '../../services/exercises.service';
import { MachinesService } from '../../services/machines.service';
import { RoutinesService } from '../../services/routines.service';

type ExerciseFilters = {
  name: string;
  activityId: number | 0;
  machineId: number | 0;
};

@Component({
  selector: 'app-exercises',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './exercises.component.html',
  styleUrl: './exercises.component.css'
})
export class ExercisesComponent implements OnInit {
  exercises: Exercise[] = [];
  activities: Activity[] = [];
  machines: Machine[] = [];
  routineTemplates: RoutineTemplate[] = [];

  exerciseDraft: ExerciseDraft = createEmptyExerciseDraft();
  editingExerciseId: number | null = null;
  deleteCandidate: Exercise | null = null;
  showForm = false;
  isLoading = false;
  apiError: string | null = null;

  filters: ExerciseFilters = {
    name: '',
    activityId: 0,
    machineId: 0
  };

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly exercisesService: ExercisesService,
    private readonly activitiesService: ActivitiesService,
    private readonly machinesService: MachinesService,
    private readonly routinesService: RoutinesService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get filteredExercises(): Exercise[] {
    return this.exercises.filter((exercise) => {
      const matchesName =
        this.filters.name.trim() === '' || exercise.name.toLowerCase().includes(this.filters.name.trim().toLowerCase());
      const matchesActivity = this.filters.activityId === 0 || exercise.activityId === this.filters.activityId;
      const matchesMachine = this.filters.machineId === 0 || exercise.machineId === this.filters.machineId;
      return matchesName && matchesActivity && matchesMachine;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredExercises.length / this.pageSize));
  }

  get paginatedExercises(): Exercise[] {
    const safePage = Math.min(this.currentPage, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredExercises.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  saveExercise(): void {
    if (!this.exerciseDraft.name || !this.exerciseDraft.videoUrl) {
      return;
    }

    this.exercisesService.upsert(this.exerciseDraft, this.editingExerciseId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadData();
        this.onFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo guardar el ejercicio en el backend.';
      }
    });
  }

  editExercise(exercise: Exercise): void {
    this.showForm = true;
    this.editingExerciseId = exercise.id;
    this.exerciseDraft = { ...exercise };
  }

  cancelEdit(): void {
    this.editingExerciseId = null;
    this.exerciseDraft = createEmptyExerciseDraft();
  }

  askDelete(exercise: Exercise): void {
    this.deleteCandidate = exercise;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const exerciseId = this.deleteCandidate.id;
    this.exercisesService.remove(exerciseId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingExerciseId === exerciseId) {
          this.cancelEdit();
        }

        this.loadData();
        this.onFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar el ejercicio en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  activityName(activityId: number | null): string {
    return this.activitiesService.nameById(activityId);
  }

  machineName(machineId: number | null): string {
    return this.machinesService.nameById(machineId);
  }

  usageCount(exerciseId: number): number {
    return this.routineTemplates.filter((template) =>
      template.exercises.some((exercise) => exercise.exerciseId === exerciseId)
    ).length;
  }

  onFilterChange(): void {
    this.currentPage = 1;
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      activityId: 0,
      machineId: 0
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
      exercises: this.exercisesService.findAll(),
      activities: this.activitiesService.findAll(),
      machines: this.machinesService.findAll(),
      routineTemplates: this.routinesService.findTemplates()
    }).subscribe({
      next: (result) => {
        this.exercises = result.exercises;
        this.activities = result.activities;
        this.machines = result.machines;
        this.routineTemplates = result.routineTemplates;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener ejercicios del backend.';
        this.exercises = this.exercisesService.getAll();
        this.activities = this.activitiesService.getAll();
        this.machines = this.machinesService.getAll();
        this.routineTemplates = this.routinesService.getTemplates();
        this.isLoading = false;
      }
    });
  }
}
