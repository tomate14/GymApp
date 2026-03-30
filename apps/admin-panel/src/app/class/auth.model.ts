import { AdminMenuKey } from './role.model';

export type LoginCredentials = {
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: number;
  fullName: string;
  email: string;
  userType: string;
  roleId: number | null;
  roleName: string | null;
  menuItems: AdminMenuKey[];
  mustChangePassword: boolean;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};
