import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Plan } from '../../class/plan.model';
import { createEmptyUserDraft, PaymentStatus, User, UserDraft, UserType } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { PlansService } from '../../services/plans.service';
import { UsersService } from '../../services/users.service';

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
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  plans: Plan[] = [];
  showForm = false;
  userDraft: UserDraft = createEmptyUserDraft();
  editingUserId: number | null = null;
  deleteCandidate: User | null = null;
  isLoading = false;
  apiError: string | null = null;

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
    return this.users.filter((user) => {
      const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
      const matchesName = this.filters.name.trim() === '' || fullName.includes(this.filters.name.trim().toLowerCase());
      const matchesDni = this.filters.dni.trim() === '' || user.dni.includes(this.filters.dni.trim());
      const matchesType = this.filters.userType === 'all' || user.userType === this.filters.userType;
      const matchesPayment = this.filters.paymentStatus === 'all' || user.paymentStatus === this.filters.paymentStatus;
      const matchesRegistrationFrom = this.filters.registrationFrom === '' || user.registrationDate >= this.filters.registrationFrom;
      const matchesRegistrationTo = this.filters.registrationTo === '' || user.registrationDate <= this.filters.registrationTo;
      const matchesRenewalFrom = this.filters.renewalFrom === '' || user.renewalDate >= this.filters.renewalFrom;
      const matchesRenewalTo = this.filters.renewalTo === '' || user.renewalDate <= this.filters.renewalTo;

      return (
        matchesName &&
        matchesDni &&
        matchesType &&
        matchesPayment &&
        matchesRegistrationFrom &&
        matchesRegistrationTo &&
        matchesRenewalFrom &&
        matchesRenewalTo
      );
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredUsers.length / this.pageSize));
  }

  get paginatedUsers(): User[] {
    const safePage = Math.min(this.currentPage, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
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
        this.loadUsersFromApi();
        this.onFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo guardar el usuario en el backend.';
      }
    });
  }

  editUser(user: User): void {
    this.showForm = true;
    this.editingUserId = user.id;
    this.userDraft = { ...user };
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

        this.loadUsersFromApi();
        this.onFilterChange();
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
        this.loadUsersFromApi();
        this.onFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo renovar la cuota en el backend.';
      }
    });
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
      users: this.usersService.findAll()
    }).subscribe({
      next: (result) => {
        this.plans = result.plans;
        this.users = result.users;
        this.usersService.setAll(result.users);
        this.isLoading = false;

        if (this.userDraft.userType === 'alumno' && this.userDraft.planId === null) {
          this.userDraft.planId = this.defaultPlanId();
        }
      },
      error: () => {
        this.apiError = 'No se pudieron obtener datos iniciales del backend.';
        this.plans = this.plansService.getAll();
        this.users = this.usersService.getAll();
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

    this.usersService.findAll().subscribe({
      next: (users) => {
        this.users = users;
        this.usersService.setAll(users);
        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudo obtener usuarios del backend. Mostrando datos locales.';
        this.users = this.usersService.getAll();
        this.isLoading = false;
      }
    });
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
