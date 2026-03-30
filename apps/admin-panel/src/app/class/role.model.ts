export type AdminMenuKey =
  | 'dashboard'
  | 'agenda'
  | 'users'
  | 'plans'
  | 'activities'
  | 'classes'
  | 'routines'
  | 'exercises'
  | 'machines'
  | 'roles_permissions';

export interface RoleMenuItem {
  key: AdminMenuKey;
  label: string;
  path: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  systemRole: boolean;
  assignedUsersCount: number;
  menuItems: AdminMenuKey[];
}

export interface RoleDraft {
  name: string;
  description: string;
  menuItems: AdminMenuKey[];
}

export const createEmptyRoleDraft = (): RoleDraft => ({
  name: '',
  description: '',
  menuItems: []
});
