import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { IonButton, IonCard, IonCardContent, IonContent, IonInput } from '@ionic/angular/standalone';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password-page',
  standalone: true,
  imports: [CommonModule, FormsModule, IonContent, IonCard, IonCardContent, IonInput, IonButton],
  templateUrl: './change-password.page.html',
  styleUrl: './change-password.page.css'
})
export class ChangePasswordPageComponent {
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    const currentPassword = this.currentPassword.trim();
    const newPassword = this.newPassword.trim();
    const confirmPassword = this.confirmPassword.trim();

    if (!currentPassword || !newPassword || !confirmPassword) {
      this.errorMessage = 'Completa todos los campos.';
      return;
    }
    if (newPassword.length < 6) {
      this.errorMessage = 'La nueva password debe tener al menos 6 caracteres.';
      return;
    }
    if (newPassword !== confirmPassword) {
      this.errorMessage = 'La confirmacion no coincide.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.isSubmitting = false;
        void this.router.navigate(['/datos-personales']);
      },
      error: (err: { error?: { message?: string } }) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? 'No se pudo actualizar la password.';
      }
    });
  }
}
