import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { createEmptyPlanDraft, Plan, PlanDraft } from '../../class/plan.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { PlanPageFilters, PlansService } from '../../services/plans.service';
import { UsersService } from '../../services/users.service';

type PlanFilters = {
  name: string;
  status: 'all' | 'active' | 'inactive';
};

@Component({
  selector: 'app-plans',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './plans.component.html',
  styleUrl: './plans.component.css'
})
export class PlansComponent implements OnInit {
  plansPage: PageResponse<Plan> = createEmptyPageResponse<Plan>();
  planDraft: PlanDraft = createEmptyPlanDraft();
  editingPlanId: number | null = null;
  deleteCandidate: Plan | null = null;
  showForm = false;
  isLoading = false;
  apiError: string | null = null;

  filters: PlanFilters = {
    name: '',
    status: 'all'
  };

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly plansService: PlansService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadPlans();
  }

  get filteredPlans(): Plan[] {
    return this.plansPage.content;
  }

  get totalPages(): number {
    return Math.max(1, this.plansPage.totalPages || 1);
  }

  get paginatedPlans(): Plan[] {
    return this.plansPage.content;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  savePlan(): void {
    if (this.planDraft.name.trim() === '' || this.planDraft.price < 0 || this.planDraft.maxDaysPerWeek < 1 || this.planDraft.maxDaysPerWeek > 7) {
      return;
    }

    this.plansService.upsert(this.planDraft, this.editingPlanId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadPlans();
      },
      error: () => {
        this.apiError = 'No se pudo guardar el plan en el backend.';
      }
    });
  }

  editPlan(plan: Plan): void {
    this.showForm = true;
    this.editingPlanId = plan.id;
    this.planDraft = { ...plan };
  }

  cancelEdit(): void {
    this.editingPlanId = null;
    this.planDraft = createEmptyPlanDraft();
  }

  askDelete(plan: Plan): void {
    this.deleteCandidate = plan;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const planId = this.deleteCandidate.id;
    this.plansService.remove(planId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingPlanId === planId) {
          this.cancelEdit();
        }

        this.loadPlans();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar el plan en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  studentsUsing(planId: number): number {
    return this.usersService.getAll().filter((user) => user.userType === 'alumno' && user.planId === planId).length;
  }

  statusLabel(isActive: boolean): string {
    return isActive ? 'Activo' : 'Inactivo';
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadPlansPage();
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      status: 'all'
    };
    this.currentPage = 1;
    this.loadPlansPage();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadPlansPage();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadPlansPage();
  }

  private loadPlans(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      plansPage: this.plansService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize),
      users: this.usersService.findAll()
    }).subscribe({
      next: (result) => {
        this.plansPage = result.plansPage;
        this.usersService.setAll(result.users);
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener planes del backend.';
        this.plansPage = createEmptyPageResponse<Plan>();
        this.isLoading = false;
      }
    });
  }

  private loadPlansPage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.plansService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize).subscribe({
      next: (pageResult) => {
        this.plansPage = pageResult;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener planes del backend.';
        this.plansPage = createEmptyPageResponse<Plan>();
        this.isLoading = false;
      }
    });
  }

  private toPageFilters(): PlanPageFilters {
    return {
      name: this.filters.name,
      status: this.filters.status
    };
  }
}
