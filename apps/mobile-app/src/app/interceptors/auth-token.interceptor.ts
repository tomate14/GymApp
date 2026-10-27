import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();
  const isLoginRequest = req.url.includes('/api/v1/auth/login');

  const request = token && !isLoginRequest
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 402 && error.error?.detail === 'TENANT_SUBSCRIPTION_REQUIRED') {
        window.alert('La suscripcion del gimnasio esta pendiente de pago. Contacta al administrador.');
      }
      if (error instanceof HttpErrorResponse && error.status === 401 && !isLoginRequest) {
        authService.clearSession();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
