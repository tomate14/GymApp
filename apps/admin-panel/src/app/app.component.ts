import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AdminMenuItem } from './class/menu-item.model';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  readonly menuItems: AdminMenuItem[] = [
    { key: 'dashboard', label: 'Dashboard', path: 'dashboard', shortLabel: 'D' },
    { key: 'agenda', label: 'Agenda', path: 'agenda-clases', shortLabel: 'Ag' },
    { key: 'users', label: 'Usuarios', path: 'usuarios', shortLabel: 'U' },
    { key: 'plans', label: 'Planes', path: 'planes', shortLabel: 'P' },
    { key: 'activities', label: 'Actividades', path: 'actividades', shortLabel: 'A' },
    { key: 'classes', label: 'Clases', path: 'clases', shortLabel: 'C' },
    { key: 'routines', label: 'Rutinas', path: 'rutinas', shortLabel: 'R' },
    { key: 'exercises', label: 'Ejercicios', path: 'ejercicios', shortLabel: 'E' },
    { key: 'machines', label: 'Maquinas', path: 'maquinas', shortLabel: 'M' },
    { key: 'roles_permissions', label: 'Roles y permisos', path: 'roles-permisos', shortLabel: 'RP' }
  ];

  sidebarCollapsed = false;
  mobileMenuOpen = false;
  isMobileView = false;

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.updateViewport();
  }

  get visibleMenuItems(): AdminMenuItem[] {
    return this.menuItems.filter((item) => this.authService.canAccessMenu(item.key));
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateViewport();
  }

  get sectionTitle(): string {
    const current = this.visibleMenuItems.find((item) => this.router.url.includes(`/${item.path}`));
    return current?.label ?? 'Dashboard';
  }

  toggleMenu(): void {
    if (this.isMobileView) {
      this.mobileMenuOpen = !this.mobileMenuOpen;
      return;
    }

    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  closeMobileMenu(): void {
    if (this.isMobileView) {
      this.mobileMenuOpen = false;
    }
  }

  onMenuItemClick(): void {
    this.closeMobileMenu();
  }

  isAuthStandaloneRoute(): boolean {
    return this.router.url.startsWith('/login') || this.router.url.startsWith('/cambiar-password');
  }

  logout(): void {
    this.authService.logout();
  }

  private updateViewport(): void {
    this.isMobileView = window.innerWidth < 992;

    if (!this.isMobileView) {
      this.mobileMenuOpen = false;
    }
  }
}
