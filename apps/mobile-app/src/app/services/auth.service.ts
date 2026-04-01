import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ChangePasswordRequest, LoginCredentials, LoginResponse } from '../models/auth.model';

const API_BASE_URL = 'http://localhost:8080/api/v1';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loginEndpoint = `${API_BASE_URL}/auth/login`;
  private readonly changePasswordEndpoint = `${API_BASE_URL}/auth/change-password`;
  private readonly tokenStorageKey = 'fitexa_mobile_token';
  private readonly mustChangeStorageKey = 'fitexa_mobile_must_change_password';
  private readonly onboardingPendingUserKey = 'fitexa_mobile_onboarding_pending_user';
  private readonly onboardingSeenPrefix = 'fitexa_mobile_onboarding_seen_';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(credentials: LoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginEndpoint, credentials).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenStorageKey, response.token);
        localStorage.setItem(this.mustChangeStorageKey, String(response.mustChangePassword));
        localStorage.setItem(this.onboardingPendingUserKey, String(response.userId));
      })
    );
  }

  changePassword(payload: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.changePasswordEndpoint, payload).pipe(
      tap(() => localStorage.setItem(this.mustChangeStorageKey, 'false'))
    );
  }

  logout(): void {
    this.clearSession();
    void this.router.navigate(['/login']);
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.mustChangeStorageKey);
    localStorage.removeItem(this.onboardingPendingUserKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    const payload = this.parseTokenPayload(token);
    return !!payload && typeof payload.exp === 'number' && Date.now() < payload.exp * 1000;
  }

  requiresPasswordChange(): boolean {
    return localStorage.getItem(this.mustChangeStorageKey) === 'true';
  }

  consumePendingOnboarding(userId: number): boolean {
    const pendingUserId = localStorage.getItem(this.onboardingPendingUserKey);
    const seenKey = `${this.onboardingSeenPrefix}${userId}`;

    if (pendingUserId !== String(userId)) {
      return false;
    }

    localStorage.removeItem(this.onboardingPendingUserKey);
    if (localStorage.getItem(seenKey) === 'true') {
      return false;
    }

    localStorage.setItem(seenKey, 'true');
    return true;
  }

  private parseTokenPayload(token: string): { exp?: number } | null {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64)) as { exp?: number };
    } catch {
      return null;
    }
  }
}
