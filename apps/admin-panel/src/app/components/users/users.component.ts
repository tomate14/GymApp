import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Activity } from '../../class/activity.model';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Plan } from '../../class/plan.model';
import { Role } from '../../class/role.model';
import { UserPaymentMovement } from '../../class/user-payment-movement.model';
import { createEmptyUserDraft, PaymentStatus, User, UserDraft } from '../../class/user.model';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { PaymentsPopupComponent } from '../../popups/payments-popup/payments-popup.component';
import { ActivitiesService } from '../../services/activities.service';
import { PlansService } from '../../services/plans.service';
import { RolesService } from '../../services/roles.service';
import { UserPageFilters, UsersService } from '../../services/users.service';

type UserTableFilters = {
  name: string;
  dni: string;
  roleId: number | null;
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
  activities: Activity[] = [];
  plans: Plan[] = [];
  roles: Role[] = [];
  activitiesDropdownOpen = false;
  showForm = false;
  userDraft: UserDraft = createEmptyUserDraft();
  editingUserId: number | null = null;
  deleteCandidate: User | null = null;
  renewCandidate: User | null = null;
  isLoading = false;
  apiError: string | null = null;
  selectedPaymentsUser: User | null = null;
  paymentMovements: UserPaymentMovement[] = [];
  isLoadingPayments = false;
  paymentsError: string | null = null;

  filters: UserTableFilters = {
    name: '',
    dni: '',
    roleId: null,
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
    private readonly activitiesService: ActivitiesService,
    private readonly plansService: PlansService,
    private readonly rolesService: RolesService
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
    this.syncDerivedProfileFromRole();

    if (!this.userDraft.firstName || !this.userDraft.lastName || !this.userDraft.dni || !this.userDraft.email) {
      return;
    }

    if (this.userDraft.userType === 'alumno' && this.userDraft.planId === null) {
      return;
    }
    if (this.userDraft.userType === 'alumno' && this.userDraft.activityIds.length === 0) {
      return;
    }
    if (this.userDraft.roleId === null) {
      return;
    }

    if (this.userDraft.userType !== 'alumno') {
      this.userDraft.planId = null;
      this.userDraft.activityIds = [];
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
    this.activitiesDropdownOpen = false;
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  cancelEdit(): void {
    this.editingUserId = null;
    this.userDraft = createEmptyUserDraft();
    this.activitiesDropdownOpen = false;
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

  askRenewQuota(user: User): void {
    if (user.userType !== 'alumno' || user.planId === null) {
      return;
    }

    this.renewCandidate = user;
  }

  confirmRenewQuota(): void {
    if (!this.renewCandidate) {
      return;
    }

    const user = this.renewCandidate;
    this.renewCandidate = null;

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

  cancelRenewQuota(): void {
    this.renewCandidate = null;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadUsersPage();
  }

  clearFilters(): void {
    this.filters = {
      name: '',
      dni: '',
      roleId: null,
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

  roleName(roleId: number | null): string {
    if (roleId === null) {
      return 'Sin rol';
    }
    return this.roles.find((role) => role.id === roleId)?.name ?? 'Sin rol';
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

  onRoleChange(): void {
    this.syncDerivedProfileFromRole();
    if (this.userDraft.userType !== 'alumno') {
      this.userDraft.planId = null;
      this.userDraft.activityIds = [];
      this.activitiesDropdownOpen = false;
      return;
    }

    if (this.userDraft.planId === null) {
      this.userDraft.planId = this.defaultPlanId();
    }
  }

  toggleActivitiesDropdown(): void {
    if (!this.isStudentRole(this.userDraft.roleId)) {
      return;
    }
    this.activitiesDropdownOpen = !this.activitiesDropdownOpen;
  }

  isActivitySelected(activityId: number): boolean {
    return this.userDraft.activityIds.includes(activityId);
  }

  toggleActivity(activityId: number): void {
    if (this.isActivitySelected(activityId)) {
      this.userDraft.activityIds = this.userDraft.activityIds.filter((item) => item !== activityId);
      return;
    }

    this.userDraft.activityIds = [...this.userDraft.activityIds, activityId];
  }

  selectedActivitiesLabel(): string {
    if (this.userDraft.activityIds.length === 0) {
      return 'Seleccionar actividades';
    }

    const names = this.activities
      .filter((activity) => this.userDraft.activityIds.includes(activity.id))
      .map((activity) => activity.name);

    if (names.length <= 2) {
      return names.join(', ');
    }

    return `${names[0]}, ${names[1]} y ${names.length - 2} mas`;
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      activities: this.activitiesService.findAll(),
      plans: this.plansService.findAll(),
      roles: this.rolesService.findAll(),
      usersPage: this.usersService.findPage(this.toPageFilters(), this.currentPage - 1, this.pageSize)
    }).subscribe({
      next: (result) => {
        this.activities = result.activities.sort((a, b) => a.name.localeCompare(b.name));
        this.plans = result.plans;
        this.roles = result.roles.sort((a, b) => a.name.localeCompare(b.name));
        this.usersPage = result.usersPage;
        this.isLoading = false;

        if (this.userDraft.roleId === null) {
          this.userDraft.roleId = this.defaultRoleId();
        }
        this.syncDerivedProfileFromRole();
        if (this.userDraft.userType === 'alumno' && this.userDraft.planId === null) {
          this.userDraft.planId = this.defaultPlanId();
        }
      },
      error: () => {
        this.apiError = 'No se pudieron obtener datos iniciales del backend.';
        this.activities = this.activitiesService.getAll();
        this.plans = this.plansService.getAll();
        this.usersPage = createEmptyPageResponse<User>();
        this.isLoading = false;
      }
    });
  }

  private resetDraftDefaults(): void {
    this.userDraft.registrationDate = this.todayIsoDate();
    this.userDraft.roleId = this.defaultRoleId();
    this.syncDerivedProfileFromRole();
    this.userDraft.planId = this.defaultPlanId();
    this.userDraft.activityIds = [];
  }

  private defaultRoleId(): number | null {
    const studentRole = this.roles.find((role) => role.name.toLowerCase() === 'alumno');
    return studentRole?.id ?? this.roles[0]?.id ?? null;
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
      userType: undefined,
      roleId: this.filters.roleId,
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

  isStudentRole(roleId: number | null): boolean {
    if (roleId === null) {
      return false;
    }
    return this.roles.find((role) => role.id === roleId)?.name.toLowerCase() === 'alumno';
  }

  private syncDerivedProfileFromRole(): void {
    if (this.isStudentRole(this.userDraft.roleId)) {
      this.userDraft.userType = 'alumno';
      return;
    }

    const roleName = this.roleName(this.userDraft.roleId).toLowerCase();
    if (roleName === 'administrador') {
      this.userDraft.userType = 'admin';
      return;
    }

    this.userDraft.userType = 'profesor';
  }
}
