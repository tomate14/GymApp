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
    { label: 'Dashboard', path: 'dashboard', shortLabel: 'D' },
    { label: 'Agenda', path: 'agenda-clases', shortLabel: 'Ag' },
    { label: 'Usuarios', path: 'usuarios', shortLabel: 'U' },
    { label: 'Planes', path: 'planes', shortLabel: 'P' },
    { label: 'Actividades', path: 'actividades', shortLabel: 'A' },
    { label: 'Clases', path: 'clases', shortLabel: 'C' },
    { label: 'Rutinas', path: 'rutinas', shortLabel: 'R' },
    { label: 'Ejercicios', path: 'ejercicios', shortLabel: 'E' },
    { label: 'Maquinas', path: 'maquinas', shortLabel: 'M' }
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

  @HostListener('window:resize')
  onResize(): void {
    this.updateViewport();
  }

  get sectionTitle(): string {
    const current = this.menuItems.find((item) => this.router.url.includes(`/${item.path}`));
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
