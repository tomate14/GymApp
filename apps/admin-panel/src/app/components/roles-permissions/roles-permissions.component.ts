import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { PageResponse, createEmptyPageResponse } from '../../class/page.model';
import { createEmptyRoleDraft, Role, RoleDraft, RoleMenuItem } from '../../class/role.model';
import { User } from '../../class/user.model';
import { RolesService } from '../../services/roles.service';
import { UsersService } from '../../services/users.service';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';

@Component({
  selector: 'app-roles-permissions',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent],
  templateUrl: './roles-permissions.component.html',
  styleUrl: './roles-permissions.component.css'
})
export class RolesPermissionsComponent implements OnInit {
  showForm = false;
  roleDraft: RoleDraft = createEmptyRoleDraft();
  roles: Role[] = [];
  menuItems: RoleMenuItem[] = [];
  users: User[] = [];
  editingRoleId: number | null = null;
  deleteCandidate: Role | null = null;
  roleChangeCandidate: User | null = null;
  pendingRoleId: number | null = null;
  isLoading = false;
  isSubmitting = false;
  isAssigning = false;
  apiError: string | null = null;
  assignmentError: string | null = null;
  successMessage: string | null = null;
  userSearch = '';
  assignmentCurrentPage = 1;
  assignmentPageSize = 8;
  assignmentPage: PageResponse<User> = createEmptyPageResponse<User>();

  readonly roleOptionsPlaceholder = 'Seleccionar rol';
  readonly assignmentPageSizeOptions = [5, 8, 10, 15];
  assignmentSelections: Record<number, number | ''> = {};

  constructor(
    private readonly rolesService: RolesService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get assignmentTotalPages(): number {
    return Math.max(1, this.assignmentPage.totalPages || 1);
  }

  get assignmentPageNumbers(): number[] {
    return Array.from({ length: this.assignmentTotalPages }, (_, index) => index + 1);
  }

  get assignmentVisibleRange(): { start: number; end: number; total: number } {
    const total = this.assignmentPage.totalElements;
    if (total === 0) {
      return { start: 0, end: 0, total: 0 };
    }

    const start = (this.assignmentCurrentPage - 1) * this.assignmentPageSize + 1;
    const end = Math.min(start + this.users.length - 1, total);
    return { start, end, total };
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  onUserSearchChange(): void {
    this.assignmentCurrentPage = 1;
    this.loadAssignmentUsers();
  }

  onAssignmentPageSizeChange(): void {
    this.assignmentCurrentPage = 1;
    this.loadAssignmentUsers();
  }

  goToAssignmentPage(page: number): void {
    if (page < 1 || page > this.assignmentTotalPages || page === this.assignmentCurrentPage) {
      return;
    }

    this.assignmentCurrentPage = page;
    this.loadAssignmentUsers();
  }

  isSelected(menuKey: RoleMenuItem['key']): boolean {
    return this.roleDraft.menuItems.includes(menuKey);
  }

  toggleMenuItem(menuKey: RoleMenuItem['key']): void {
    const exists = this.roleDraft.menuItems.includes(menuKey);
    this.roleDraft.menuItems = exists
      ? this.roleDraft.menuItems.filter((item) => item !== menuKey)
      : [...this.roleDraft.menuItems, menuKey];
  }

  saveRole(): void {
    if (this.roleDraft.name.trim() === '' || this.roleDraft.description.trim() === '') {
      return;
    }

    this.isSubmitting = true;
    this.apiError = null;
    this.successMessage = null;
    const isCreating = this.editingRoleId === null;

    const payload: RoleDraft = {
      name: this.roleDraft.name.trim(),
      description: this.roleDraft.description.trim(),
      menuItems: [...this.roleDraft.menuItems]
    };

    const request$ = this.editingRoleId === null
      ? this.rolesService.create(payload)
      : this.rolesService.update(this.editingRoleId, payload);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.resetForm();
        this.successMessage = isCreating ? 'Rol guardado correctamente.' : 'Rol actualizado correctamente.';
        this.loadRoles();
      },
      error: () => {
        this.isSubmitting = false;
        this.apiError = 'No se pudo guardar el rol en el backend.';
      }
    });
  }

  editRole(role: Role): void {
    this.showForm = true;
    this.editingRoleId = role.id;
    this.roleDraft = {
      name: role.name,
      description: role.description,
      menuItems: [...role.menuItems]
    };
  }

  cancelEdit(): void {
    this.resetForm();
  }

  askDelete(role: Role): void {
    this.deleteCandidate = role;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    this.rolesService.remove(this.deleteCandidate.id).subscribe({
      next: () => {
        this.deleteCandidate = null;
        this.successMessage = 'Rol eliminado correctamente.';
        this.loadRoles();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar el rol.';
        this.deleteCandidate = null;
      }
    });
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
  }

  roleLabel(roleId: number | null): string {
    if (roleId === null) {
      return 'Sin rol';
    }

    return this.roles.find((role) => role.id === roleId)?.name ?? 'Sin rol';
  }

  askAssignRole(user: User): void {
    const selectedRoleId = this.assignmentSelections[user.id];
    if (selectedRoleId === '' || selectedRoleId === undefined) {
      return;
    }

    if (Number(selectedRoleId) === user.roleId) {
      return;
    }

    this.roleChangeCandidate = user;
    this.pendingRoleId = Number(selectedRoleId);
  }

  confirmAssignRole(): void {
    if (!this.roleChangeCandidate || this.pendingRoleId === null) {
      return;
    }

    const user = this.roleChangeCandidate;
    const selectedRoleId = this.pendingRoleId;

    this.isAssigning = true;
    this.assignmentError = null;
    this.successMessage = null;

    this.usersService.assignRole(user.id, selectedRoleId).subscribe({
      next: (updated) => {
        this.isAssigning = false;
        this.users = this.users.map((item) => item.id === updated.id ? updated : item);
        this.assignmentPage = {
          ...this.assignmentPage,
          content: this.users
        };
        this.assignmentSelections[user.id] = updated.roleId ?? '';
        this.successMessage = `Rol actualizado para ${updated.firstName} ${updated.lastName}.`;
        this.roleChangeCandidate = null;
        this.pendingRoleId = null;
      },
      error: () => {
        this.isAssigning = false;
        this.assignmentError = 'No se pudo asignar el rol al usuario.';
        this.roleChangeCandidate = null;
        this.pendingRoleId = null;
      }
    });
  }

  cancelAssignRole(): void {
    if (this.roleChangeCandidate) {
      this.assignmentSelections[this.roleChangeCandidate.id] = this.roleChangeCandidate.roleId ?? '';
    }
    this.roleChangeCandidate = null;
    this.pendingRoleId = null;
  }

  pendingRoleLabel(): string {
    if (this.pendingRoleId === null) {
      return 'Sin rol';
    }
    return this.roleLabel(this.pendingRoleId);
  }

  menuLabel(role: Role, menuKey: RoleMenuItem['key']): boolean {
    return role.menuItems.includes(menuKey);
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;
    this.assignmentError = null;
    this.successMessage = null;

    forkJoin({
      roles: this.rolesService.findAll(),
      menuItems: this.rolesService.findMenuItems()
    }).subscribe({
      next: (result) => {
        this.roles = result.roles.sort((a, b) => a.name.localeCompare(b.name));
        this.menuItems = result.menuItems;
        this.isLoading = false;
        this.loadAssignmentUsers();
      },
      error: () => {
        this.apiError = 'No se pudieron cargar roles y usuarios.';
        this.isLoading = false;
      }
    });
  }

  private loadRoles(): void {
    this.rolesService.findAll().subscribe({
      next: (items) => {
        this.roles = items.sort((a, b) => a.name.localeCompare(b.name));
      },
      error: () => {
        this.apiError = 'No se pudieron refrescar los roles.';
      }
    });
  }

  private loadAssignmentUsers(): void {
    this.assignmentError = null;

    this.usersService.findPage(
      {
        name: this.userSearch.trim() === '' ? undefined : this.userSearch.trim(),
        userType: 'all'
      },
      this.assignmentCurrentPage - 1,
      this.assignmentPageSize
    ).subscribe({
      next: (page) => {
        this.assignmentPage = page;
        this.users = page.content;
        this.assignmentSelections = {
          ...this.assignmentSelections,
          ...page.content.reduce<Record<number, number | ''>>((acc, user) => {
            acc[user.id] = user.roleId ?? '';
            return acc;
          }, {})
        };
      },
      error: () => {
        this.assignmentError = 'No se pudieron cargar los usuarios para asignar roles.';
        this.assignmentPage = createEmptyPageResponse<User>();
        this.users = [];
      }
    });
  }

  private resetForm(): void {
    this.editingRoleId = null;
    this.roleDraft = createEmptyRoleDraft();
  }
}
