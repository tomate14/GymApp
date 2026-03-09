import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ChangePasswordRequest, LoginCredentials, LoginResponse } from '../class/auth.model';
import { API_BASE_URL } from '../utils/api.util';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loginEndpoint = `${API_BASE_URL}/auth/login`;
  private readonly changePasswordEndpoint = `${API_BASE_URL}/auth/change-password`;
  private readonly tokenStorageKey = 'fitexa_auth_token';
  private readonly mustChangeStorageKey = 'fitexa_must_change_password';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(credentials: LoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginEndpoint, credentials).pipe(
      tap((response) => {
        this.storeToken(response.token);
        this.storeMustChangePassword(response.mustChangePassword);
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

  private storeToken(token: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
  }

  private storeMustChangePassword(value: boolean): void {
    localStorage.setItem(this.mustChangeStorageKey, String(value));
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
