import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ChangePasswordRequest, LoginCredentials, LoginResponse } from '../class/auth.model';
import { AdminMenuKey } from '../class/role.model';
import { API_BASE_URL } from '../utils/api.util';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loginEndpoint = `${API_BASE_URL}/auth/login`;
  private readonly changePasswordEndpoint = `${API_BASE_URL}/auth/change-password`;
  private readonly tokenStorageKey = 'fitexa_auth_token';
  private readonly mustChangeStorageKey = 'fitexa_must_change_password';
  private readonly sessionStorageKey = 'fitexa_auth_session';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(credentials: LoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginEndpoint, credentials).pipe(
      tap((response) => {
        this.storeToken(response.token);
        this.storeMustChangePassword(response.mustChangePassword);
        this.storeSession(response);
      })
    );
  }

  changePassword(payload: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.changePasswordEndpoint, payload).pipe(
      tap(() => this.storeMustChangePassword(false))
    );
  }

  logout(): void {
    this.clearSession();
    void this.router.navigate(['/login']);
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.mustChangeStorageKey);
    localStorage.removeItem(this.sessionStorageKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (token === null || token.trim() === '') {
      return false;
    }

    return !this.isTokenExpired(token);
  }

  requiresPasswordChange(): boolean {
    return localStorage.getItem(this.mustChangeStorageKey) === 'true';
  }

  getSession(): LoginResponse | null {
    const raw = localStorage.getItem(this.sessionStorageKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }

  getMenuItems(): AdminMenuKey[] {
    return this.getSession()?.menuItems ?? [];
  }

  canAccessMenu(key: AdminMenuKey): boolean {
    const session = this.getSession();
    if (!session) {
      return false;
    }
    return session.menuItems.includes(key);
  }

  getDefaultRoute(): string {
    const routeMap: Record<AdminMenuKey, string> = {
      dashboard: '/dashboard',
      agenda: '/agenda-clases',
      users: '/usuarios',
      plans: '/planes',
      activities: '/actividades',
      classes: '/clases',
      routines: '/rutinas',
      exercises: '/ejercicios',
      machines: '/maquinas',
      roles_permissions: '/roles-permisos'
    };

    const preferredOrder: AdminMenuKey[] = [
      'dashboard',
      'agenda',
      'users',
      'plans',
      'activities',
      'classes',
      'routines',
      'exercises',
      'machines',
      'roles_permissions'
    ];

    const firstAllowed = preferredOrder.find((key) => this.canAccessMenu(key));
    return firstAllowed ? routeMap[firstAllowed] : '/dashboard';
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
  }

  private storeMustChangePassword(value: boolean): void {
    localStorage.setItem(this.mustChangeStorageKey, String(value));
  }

  private storeSession(session: LoginResponse): void {
    localStorage.setItem(this.sessionStorageKey, JSON.stringify(session));
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.parseTokenPayload(token);
    if (!payload || typeof payload.exp !== 'number') {
      return true;
    }
    const expirationMs = payload.exp * 1000;
    return Date.now() >= expirationMs;
  }

  private parseTokenPayload(token: string): { exp?: number } | null {
    const segments = token.split('.');
    if (segments.length !== 3) {
      return null;
    }

    try {
      const base64 = segments[1].replace(/-/g, '+').replace(/_/g, '/');
      const decoded = atob(base64);
      return JSON.parse(decoded) as { exp?: number };
    } catch {
      return null;
    }
  }
}
