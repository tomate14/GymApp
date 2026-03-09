import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const loginGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    if (authService.requiresPasswordChange()) {
      return router.createUrlTree(['/cambiar-password']);
    }
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
