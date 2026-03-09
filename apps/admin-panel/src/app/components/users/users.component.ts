import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Plan } from '../../class/plan.model';
import { UserPaymentMovement } from '../../class/user-payment-movement.model';
import { createEmptyUserDraft, PaymentStatus, User, UserDraft, UserType } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { PaymentsPopupComponent } from '../../popups/payments-popup/payments-popup.component';
import { PlansService } from '../../services/plans.service';
import { UserPageFilters, UsersService } from '../../services/users.service';

type UserTableFilters = {
  name: string;
  dni: string;
  userType: UserType | 'all';
  paymentStatus: PaymentStatus | 'all';
  registrationFrom: string;
  registrationTo: string;
  renewalFrom: string;
  renewalTo: string;
};

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent, PaymentsPopupComponent],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  usersPage: PageResponse<User> = createEmptyPageResponse<User>();
  plans: Plan[] = [];
  showForm = false;
  userDraft: UserDraft = createEmptyUserDraft();
  editingUserId: number | null = null;
  deleteCandidate: User | null = null;
  isLoading = false;
  apiError: string | null = null;
  selectedPaymentsUser: User | null = null;
  paymentMovements: UserPaymentMovement[] = [];
  isLoadingPayments = false;
  paymentsError: string | null = null;

  filters: UserTableFilters = {
    name: '',
    dni: '',
    userType: 'all',
    paymentStatus: 'all',
    registrationFrom: '',
    registrationTo: '',
    renewalFrom: '',
    renewalTo: ''
  };

  currentPage = 1;
  pageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly usersService: UsersService,
    private readonly plansService: PlansService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.resetDraftDefaults();
  }

  get filteredUsers(): User[] {
    return this.usersPage.content;
  }

  get totalPages(): number {
    return Math.max(1, this.usersPage.totalPages || 1);
  }

  get paginatedUsers(): User[] {
    return this.usersPage.content;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  saveUser(): void {
    if (!this.userDraft.firstName || !this.userDraft.lastName || !this.userDraft.dni || !this.userDraft.email) {
      return;
    }

    if (this.userDraft.userType === 'alumno' && this.userDraft.planId === null) {
      return;
    }

    if (this.userDraft.userType === 'profesor') {
      this.userDraft.planId = null;
    }

    const today = this.todayIsoDate();
    if (this.userDraft.registrationDate === '') {
      this.userDraft.registrationDate = today;
    }

    const request$ = this.usersService.upsert(this.userDraft, this.editingUserId);

    request$.subscribe({
      next: () => {
        this.cancelEdit();
        this.loadUsersPage();
      },
      error: () => {
        this.apiError = 'No se pudo guardar el usuario en el backend.';
      }
    });
  }

  editUser(user: User): void {
    this.showForm = true;
    this.editingUserId = user.id;
    this.userDraft = { ...user, password: '' };
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  cancelEdit(): void {
    this.editingUserId = null;
    this.userDraft = createEmptyUserDraft();
    this.resetDraftDefaults();
  }

  askDelete(user: User): void {
    this.deleteCandidate = user;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    const userId = this.deleteCandidate.id;
    this.usersService.remove(userId).subscribe({
      next: () => {
        this.deleteCandidate = null;

        if (this.editingUserId === userId) {
          this.cancelEdit();
        }

        this.loadUsersPage();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar el usuario en el backend.';
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadUsersPage();
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      dni: '',
      userType: 'all',
      paymentStatus: 'all',
      registrationFrom: '',
      registrationTo: '',
      renewalFrom: '',
      renewalTo: ''
    };
    this.currentPage = 1;
    this.loadUsersPage();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadUsersPage();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadUsersPage();
  }

  paymentStatusLabel(status: PaymentStatus): string {
    return status === 'al-dia' ? 'Al dia' : 'Adeuda';
  }

  planName(planId: number | null): string {
    if (planId === null) {
      return 'Sin plan';
    }

    return this.plansService.getById(planId)?.name ?? 'Sin plan';
  }

  planAmount(planId: number | null): number {
    if (planId === null) {
      return 0;
    }

    return this.plansService.getById(planId)?.price ?? 0;
  }

  renewQuota(user: User): void {
    if (user.userType !== 'alumno') {
      return;
    }

    this.usersService.renewSubscription(user.id).subscribe({
      next: () => {
        this.loadUsersPage();
        if (this.selectedPaymentsUser?.id === user.id) {
          this.loadPaymentMovements(user.id);
        }
      },
      error: () => {
        this.apiError = 'No se pudo renovar la cuota en el backend.';
      }
    });
  }

  togglePayments(user: User): void {
    if (user.userType !== 'alumno') {
      return;
    }
    if (this.selectedPaymentsUser?.id === user.id) {
      this.closePayments();
      return;
    }
    this.selectedPaymentsUser = user;
    this.loadPaymentMovements(user.id);
  }

  closePayments(): void {
    this.selectedPaymentsUser = null;
    this.paymentMovements = [];
    this.paymentsError = null;
    this.isLoadingPayments = false;
  }

  onUserTypeChange(): void {
    if (this.userDraft.userType === 'profesor') {
      this.userDraft.planId = null;
      return;
    }

    if (this.userDraft.planId === null) {
      this.userDraft.planId = this.defaultPlanId();
    }
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      plans: this.plansService.findAll(),
      usersPage: this.usersService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize)
    }).subscribe({
      next: (result) => {
        this.plans = result.plans;
        this.usersPage = result.usersPage;
        this.isLoading = false;

        if (this.userDraft.userType === 'alumno' && this.userDraft.planId === null) {
          this.userDraft.planId = this.defaultPlanId();
        }
      },
      error: () => {
        this.apiError = 'No se pudieron obtener datos iniciales del backend.';
        this.plans = this.plansService.getAll();
        this.usersPage = createEmptyPageResponse<User>();
        this.isLoading = false;
      }
    });
  }

  private resetDraftDefaults(): void {
    this.userDraft.registrationDate = this.todayIsoDate();
    this.userDraft.planId = this.defaultPlanId();
  }

  private defaultPlanId(): number | null {
    return this.plansService.getActive()[0]?.id ?? this.plans[0]?.id ?? null;
  }

  private loadUsersFromApi(): void {
    this.isLoading = true;
    this.apiError = null;

    this.usersService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize).subscribe({
      next: (pageResult) => {
        this.usersPage = pageResult;
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudo obtener usuarios del backend.';
        this.usersPage = createEmptyPageResponse<User>();
        this.isLoading = false;
      }
    });
  }

  private loadUsersPage(): void {
    this.loadUsersFromApi();
  }

  private loadPaymentMovements(userId: number): void {
    this.isLoadingPayments = true;
    this.paymentsError = null;
    this.paymentMovements = [];

    this.usersService.findPayments(userId).subscribe({
      next: (items) => {
        this.paymentMovements = items;
        this.isLoadingPayments = false;
      },
      error: () => {
        this.paymentsError = 'No se pudieron obtener los movimientos de pago del alumno.';
        this.isLoadingPayments = false;
      }
    });
  }

  private toPageFilters(): UserPageFilters {
    return {
      name: this.filters.name,
      dni: this.filters.dni,
      userType: this.filters.userType,
      paymentStatus: this.filters.paymentStatus,
      registrationFrom: this.filters.registrationFrom,
      registrationTo: this.filters.registrationTo,
      renewalFrom: this.filters.renewalFrom,
      renewalTo: this.filters.renewalTo
    };
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
