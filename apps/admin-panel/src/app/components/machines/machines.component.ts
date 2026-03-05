import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Exercise } from '../../class/exercise.model';
import { createEmptyMachineDraft, Machine, MachineDraft } from '../../class/machine.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ExercisesService } from '../../services/exercises.service';
import { MachinesService } from '../../services/machines.service';

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
  machines: Machine[] = [];
  exercises: Exercise[] = [];
  machineDraft: MachineDraft = createEmptyMachineDraft();
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
    return this.machines.filter((machine) => {
      const matchesName =
        this.filters.name.trim() === '' || machine.name.toLowerCase().includes(this.filters.name.trim().toLowerCase());
      const usage = this.exerciseUsageCount(machine.id);
      const matchesInUse = !this.filters.onlyInUse || usage > 0;
      return matchesName && matchesInUse;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredMachines.length / this.pageSize));
  }

  get paginatedMachines(): Machine[] {
    const safePage = Math.min(this.currentPage, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredMachines.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  saveMachine(): void {
    if (!this.machineDraft.name || !this.machineDraft.photoUrl) {
      return;
    }

    this.machinesService.upsert(this.machineDraft, this.editingMachineId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadData();
        this.onFilterChange();
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
  }

  cancelEdit(): void {
    this.editingMachineId = null;
    this.machineDraft = createEmptyMachineDraft();
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
        this.onFilterChange();
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
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      onlyInUse: false
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
      machines: this.machinesService.findAll(),
      exercises: this.exercisesService.findAll()
    }).subscribe({
      next: (result) => {
        this.machines = result.machines;
        this.exercises = result.exercises;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener maquinas del backend.';
        this.machines = this.machinesService.getAll();
        this.exercises = this.exercisesService.getAll();
        this.isLoading = false;
      }
    });
  }
}
