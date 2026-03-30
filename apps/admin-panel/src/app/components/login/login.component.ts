import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginCredentials } from '../../class/auth.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials: LoginCredentials = {
    email: '',
    password: ''
  };
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  submit(): void {
    const email = this.credentials.email.trim().toLowerCase();
    const password = this.credentials.password;
    if (email === '' || password.trim() === '') {
      this.errorMessage = 'Completa email y contrasena.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    this.authService.login({ email, password }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        if (response.mustChangePassword) {
          void this.router.navigate(['/cambiar-password']);
          return;
        }

        const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');
        if (redirectTo && redirectTo.startsWith('/')) {
          void this.router.navigateByUrl(redirectTo);
          return;
        }
        void this.router.navigateByUrl(this.authService.getDefaultRoute());
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Credenciales invalidas.';
      }
    });
  }
}
