import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Plan } from '../../class/plan.model';
import { createEmptyUserDraft, PaymentStatus, User, UserDraft, UserType } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ClassesService } from '../../services/classes.service';
import { PlansService } from '../../services/plans.service';
import { RoutinesService } from '../../services/routines.service';
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
    private readonly classesService: ClassesService,
    private readonly plansService: PlansService,
    private readonly routinesService: RoutinesService
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

    this.usersService.upsert(this.userDraft, this.editingUserId);
    this.cancelEdit();
    this.loadData();
    this.onFilterChange();
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
    this.usersService.remove(userId);
    this.classesService.clearTeacherReferences(userId);
    this.routinesService.clearUserReferences(userId);
    this.deleteCandidate = null;

    if (this.editingUserId === userId) {
      this.cancelEdit();
    }

    this.loadData();
    this.onFilterChange();
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

    this.usersService.renewSubscription(user.id);
    this.loadData();
    this.onFilterChange();
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
    this.users = this.usersService.getAll();
    this.plans = this.plansService.getAll();
  }

  private resetDraftDefaults(): void {
    this.userDraft.registrationDate = this.todayIsoDate();
    this.userDraft.planId = this.defaultPlanId();
  }

  private defaultPlanId(): number | null {
    return this.plansService.getActive()[0]?.id ?? this.plans[0]?.id ?? null;
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
