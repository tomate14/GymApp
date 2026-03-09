import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonButton,
  IonCard,
  IonCardContent,
  IonContent,
  IonIcon,
  IonInput
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { keyOutline, mailOutline } from 'ionicons/icons';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, IonContent, IonCard, IonCardContent, IonButton, IonInput, IonIcon],
  templateUrl: './login.page.html',
  styleUrl: './login.page.css'
})
export class LoginPageComponent {
  email = '';
  password = '';
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
    addIcons({ mailOutline, keyOutline });
  }

  submit(): void {
    const email = this.email.trim().toLowerCase();
    const password = this.password.trim();
    if (!email || !password) {
      this.errorMessage = 'Completa email y password.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    this.authService.login({ email, password }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        if (response.mustChangePassword) {
          void this.router.navigate(['/change-password']);
          return;
        }

        const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');
        void this.router.navigateByUrl(redirectTo && redirectTo.startsWith('/') ? redirectTo : '/datos-personales');
      },
      error: (err: { error?: { message?: string } }) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? 'No se pudo iniciar sesion.';
      }
    });
  }
}
