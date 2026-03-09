import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    const currentPassword = this.currentPassword.trim();
    const newPassword = this.newPassword.trim();
    const confirmPassword = this.confirmPassword.trim();

    if (currentPassword === '' || newPassword === '' || confirmPassword === '') {
      this.errorMessage = 'Completa todos los campos.';
      return;
    }
    if (newPassword.length < 6) {
      this.errorMessage = 'La nueva contrasena debe tener al menos 6 caracteres.';
      return;
    }
    if (newPassword !== confirmPassword) {
      this.errorMessage = 'La confirmacion no coincide con la nueva contrasena.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = response.message;
        setTimeout(() => {
          void this.router.navigate(['/dashboard']);
        }, 400);
      },
      error: (err: { error?: { message?: string } }) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? 'No se pudo cambiar la contrasena.';
      }
    });
  }
}
