import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AdminMenuKey } from '../class/role.model';
import { AuthService } from '../services/auth.service';

export const menuPermissionGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const moduleKey = route.data?.['moduleKey'] as AdminMenuKey | undefined;

  if (!moduleKey) {
    return true;
  }

  if (authService.canAccessMenu(moduleKey)) {
    return true;
  }

  return router.createUrlTree([authService.getDefaultRoute()]);
};
