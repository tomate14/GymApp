import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IonBadge, IonButton, IonCard, IonCardContent, IonInput, IonSpinner } from '@ionic/angular/standalone';
import { MobileProfile, MobileProfileUpdateRequest } from '../../models/mobile.model';
import { AuthService } from '../../services/auth.service';
import { MobileAppService } from '../../services/mobile-app.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, FormsModule, IonCard, IonCardContent, IonBadge, IonButton, IonInput, IonSpinner],
  templateUrl: './profile.page.html',
  styleUrl: './profile.page.css'
})
export class ProfilePageComponent implements OnInit {
  profile: MobileProfile | null = null;
  errorMessage: string | null = null;
  isLoading = true;
  isEditing = false;
  isSaving = false;
  showWelcomeGuide = false;
  draft: MobileProfileUpdateRequest = {
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    birthDate: ''
  };

  constructor(
    private readonly mobileAppService: MobileAppService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.mobileAppService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.syncDraft(profile);
        this.showWelcomeGuide = this.authService.consumePendingOnboarding(profile.id);
        this.isLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo cargar tu perfil.';
        this.isLoading = false;
      }
    });
  }

  startEditing(): void {
    if (!this.profile) {
      return;
    }

    this.syncDraft(this.profile);
    this.isEditing = true;
    this.errorMessage = null;
  }

  cancelEditing(): void {
    if (this.profile) {
      this.syncDraft(this.profile);
    }
    this.isEditing = false;
    this.errorMessage = null;
  }

  saveProfile(): void {
    if (!this.profile) {
      return;
    }

    if (!this.draft.firstName.trim() || !this.draft.lastName.trim() || !this.draft.phone.trim() || !this.draft.address.trim() || !this.draft.birthDate) {
      this.errorMessage = 'Completa todos los datos personales para guardar los cambios.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;

    this.mobileAppService.updateProfile({
      firstName: this.draft.firstName.trim(),
      lastName: this.draft.lastName.trim(),
      phone: this.draft.phone.trim(),
      address: this.draft.address.trim(),
      birthDate: this.draft.birthDate
    }).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.syncDraft(profile);
        this.isSaving = false;
        this.isEditing = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo actualizar tu perfil.';
        this.isSaving = false;
      }
    });
  }

  profilePhotoUrl(profile: MobileProfile): string {
    const seed = encodeURIComponent(`${profile.firstName}-${profile.lastName}-${profile.email}`);
    return `https://api.dicebear.com/7.x/notionists/svg?seed=${seed}&backgroundColor=ffdfbf,ffd5dc,b6e3f4,c0aede,d1d4f9&scale=110`;
  }

  initials(profile: MobileProfile): string {
    return `${profile.firstName.charAt(0)}${profile.lastName.charAt(0)}`.toUpperCase();
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date(`${value}T00:00:00`));
  }

  paymentLabel(profile: MobileProfile): string {
    return profile.paymentStatus === 'al-dia' ? 'Pago al dia' : 'Pago adeudado';
  }

  userTypeLabel(profile: MobileProfile): string {
    return profile.userType === 'profesor' ? 'Profesor' : 'Alumno';
  }

  dismissWelcomeGuide(): void {
    this.showWelcomeGuide = false;
  }

  private syncDraft(profile: MobileProfile): void {
    this.draft = {
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone,
      address: profile.address,
      birthDate: profile.birthDate
    };
  }
}
