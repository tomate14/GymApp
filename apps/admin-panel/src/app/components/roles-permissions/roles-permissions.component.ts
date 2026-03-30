import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
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
  isLoading = false;
  isSubmitting = false;
  isAssigning = false;
  apiError: string | null = null;
  assignmentError: string | null = null;
  userSearch = '';

  readonly roleOptionsPlaceholder = 'Seleccionar rol';
  assignmentSelections: Record<number, number | ''> = {};

  constructor(
    private readonly rolesService: RolesService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  get filteredUsers(): User[] {
    const term = this.userSearch.trim().toLowerCase();
    if (term === '') {
      return [...this.users].sort((a, b) => `${a.firstName} ${a.lastName}`.localeCompare(`${b.firstName} ${b.lastName}`));
    }

    return this.users
      .filter((user) => `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase().includes(term))
      .sort((a, b) => `${a.firstName} ${a.lastName}`.localeCompare(`${b.firstName} ${b.lastName}`));
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
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

  assignRole(user: User): void {
    const selectedRoleId = this.assignmentSelections[user.id];
    if (selectedRoleId === '' || selectedRoleId === undefined) {
      return;
    }

    this.isAssigning = true;
    this.assignmentError = null;

    this.usersService.assignRole(user.id, Number(selectedRoleId)).subscribe({
      next: (updated) => {
        this.isAssigning = false;
        this.users = this.users.map((item) => item.id === updated.id ? updated : item);
        this.assignmentSelections[user.id] = updated.roleId ?? '';
      },
      error: () => {
        this.isAssigning = false;
        this.assignmentError = 'No se pudo asignar el rol al usuario.';
      }
    });
  }

  menuLabel(role: Role, menuKey: RoleMenuItem['key']): boolean {
    return role.menuItems.includes(menuKey);
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;
    this.assignmentError = null;

    forkJoin({
      roles: this.rolesService.findAll(),
      menuItems: this.rolesService.findMenuItems(),
      users: this.usersService.findAll()
    }).subscribe({
      next: (result) => {
        this.roles = result.roles.sort((a, b) => a.name.localeCompare(b.name));
        this.menuItems = result.menuItems;
        this.users = result.users;
        this.assignmentSelections = result.users.reduce<Record<number, number | ''>>((acc, user) => {
          acc[user.id] = user.roleId ?? '';
          return acc;
        }, {});
        this.isLoading = false;
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

  private resetForm(): void {
    this.editingRoleId = null;
    this.roleDraft = createEmptyRoleDraft();
  }
}
