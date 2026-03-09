import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Exercise } from '../../class/exercise.model';
import { createEmptyMachineDraft, Machine, MachineDraft } from '../../class/machine.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ExercisesService } from '../../services/exercises.service';
import { MachinePageFilters, MachinesService } from '../../services/machines.service';

type MachineFilters = {
  name: string;
  onlyInUse: boolean;
};

@Component({
  selector: 'app-machines',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './machines.component.html',
  styleUrl: './machines.component.css'
})
export class MachinesComponent implements OnInit {
  machinesPage: PageResponse<Machine> = createEmptyPageResponse<Machine>();
  exercises: Exercise[] = [];
  machineDraft: MachineDraft = createEmptyMachineDraft();
  selectedPhoto: File | null = null;
  machinePhotoPreviewUrl = '';
  editingMachineId: number | null = null;
  deleteCandidate: Machine | null = null;
  showForm = false;
  isLoading = false;
  apiError: string | null = null;

  filters: MachineFilters = {
    name: '',
    onlyInUse: false
  };

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly machinesService: MachinesService,
    private readonly exercisesService: ExercisesService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get filteredMachines(): Machine[] {
    return this.machinesPage.content;
  }

  get totalPages(): number {
    return Math.max(1, this.machinesPage.totalPages || 1);
  }

  get paginatedMachines(): Machine[] {
    return this.machinesPage.content;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  saveMachine(): void {
    if (!this.machineDraft.name || (this.editingMachineId === null && !this.selectedPhoto)) {
      return;
    }

    this.machinesService.upsert(this.machineDraft, this.selectedPhoto, this.editingMachineId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la maquina en el backend.';
      }
    });
  }

  editMachine(machine: Machine): void {
    this.showForm = true;
    this.editingMachineId = machine.id;
    this.machineDraft = { ...machine };
    this.selectedPhoto = null;
    this.machinePhotoPreviewUrl = machine.photoUrl;
  }

  cancelEdit(): void {
    this.editingMachineId = null;
    this.machineDraft = createEmptyMachineDraft();
    this.selectedPhoto = null;
    this.machinePhotoPreviewUrl = '';
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedPhoto = file;
    this.machinePhotoPreviewUrl = file ? URL.createObjectURL(file) : this.machineDraft.photoUrl;
  }

  askDelete(machine: Machine): void {
    this.deleteCandidate = machine;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const machineId = this.deleteCandidate.id;
    this.machinesService.remove(machineId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingMachineId === machineId) {
          this.cancelEdit();
        }

        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar la maquina en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  exerciseUsageCount(machineId: number): number {
    return this.exercises.filter((exercise) => exercise.machineId === machineId).length;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadMachinesPage();
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      onlyInUse: false
    };
    this.currentPage = 1;
    this.loadMachinesPage();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadMachinesPage();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadMachinesPage();
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      machinesPage: this.machinesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize),
      exercises: this.exercisesService.findAll()
    }).subscribe({
      next: (result) => {
        this.machinesPage = result.machinesPage;
        this.exercises = result.exercises;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener maquinas del backend.';
        this.machinesPage = createEmptyPageResponse<Machine>();
        this.exercises = this.exercisesService.getAll();
        this.isLoading = false;
      }
    });
  }

  private loadMachinesPage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.machinesService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize).subscribe({
      next: (pageResult) => {
        this.machinesPage = pageResult;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener maquinas del backend.';
        this.machinesPage = createEmptyPageResponse<Machine>();
        this.isLoading = false;
      }
    });
  }

  private toPageFilters(): MachinePageFilters {
    return {
      name: this.filters.name,
      onlyInUse: this.filters.onlyInUse
    };
  }
}
