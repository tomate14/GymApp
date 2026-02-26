import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { createEmptyPlanDraft, Plan, PlanDraft } from '../../class/plan.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { PlansService } from '../../services/plans.service';
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
  plans: Plan[] = [];
  planDraft: PlanDraft = createEmptyPlanDraft();
  editingPlanId: number | null = null;
  deleteCandidate: Plan | null = null;
  showForm = false;

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
    return this.plans.filter((plan) => {
      const matchesName = this.filters.name.trim() === '' || plan.name.toLowerCase().includes(this.filters.name.trim().toLowerCase());
      const matchesStatus =
        this.filters.status === 'all' ||
        (this.filters.status === 'active' && plan.isActive) ||
        (this.filters.status === 'inactive' && !plan.isActive);

      return matchesName && matchesStatus;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredPlans.length / this.pageSize));
  }

  get paginatedPlans(): Plan[] {
    const safePage = Math.min(this.currentPage, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredPlans.slice(start, start + this.pageSize);
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

    this.plansService.upsert(this.planDraft, this.editingPlanId);
    this.cancelEdit();
    this.loadPlans();
    this.onFilterChange();
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
    this.plansService.remove(planId);
    this.usersService.clearPlanReferences(planId);
    this.deleteCandidate = null;

    if (this.editingPlanId === planId) {
      this.cancelEdit();
    }

    this.loadPlans();
    this.onFilterChange();
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
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      status: 'all'
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

  private loadPlans(): void {
    this.plans = this.plansService.getAll();
  }
}
