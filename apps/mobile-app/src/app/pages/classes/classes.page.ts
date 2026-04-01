import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { IonButton, IonCard, IonCardContent, IonSpinner, IonToast } from '@ionic/angular/standalone';
import { MobileGymClass } from '../../models/mobile.model';
import { MobileAppService } from '../../services/mobile-app.service';

@Component({
  selector: 'app-classes-page',
  standalone: true,
  imports: [CommonModule, IonCard, IonCardContent, IonButton, IonSpinner, IonToast],
  templateUrl: './classes.page.html',
  styleUrl: './classes.page.css'
})
export class ClassesPageComponent implements OnInit {
  classes: MobileGymClass[] = [];
  activeTab: 'mine' | 'available' = 'mine';
  selectedDate = new Date().toISOString().slice(0, 10);
  showOnlyEnabledAvailable = false;
  errorMessage: string | null = null;
  isLoading = true;
  toastMessage: string | null = null;
  toastOpen = false;

  constructor(private readonly mobileAppService: MobileAppService) {}

  ngOnInit(): void {
    this.loadClasses();
  }

  goToNextDay(): void {
    const next = new Date(`${this.selectedDate}T00:00:00`);
    next.setDate(next.getDate() + 1);
    this.selectedDate = next.toISOString().slice(0, 10);
    this.loadClasses();
  }

  goToPreviousDay(): void {
    const current = new Date(`${this.selectedDate}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    current.setDate(current.getDate() - 1);
    if (current < today) {
      return;
    }
    this.selectedDate = current.toISOString().slice(0, 10);
    this.loadClasses();
  }

  selectTab(tab: 'mine' | 'available'): void {
    this.activeTab = tab;
  }

  subscribe(item: MobileGymClass): void {
    if (!item.canSubscribe) {
      return;
    }

    this.mobileAppService.subscribeToClass(item.id, this.selectedDate).subscribe({
      next: (updated) => {
        this.classes = this.classes.map((current) => current.id === updated.id ? updated : current);
      },
      error: (err: { error?: { message?: string } }) => {
        this.presentToast(err.error?.message ?? 'No se pudo registrar la suscripcion.');
      }
    });
  }

  dismissToast(): void {
    this.toastOpen = false;
  }

  formatSelectedDate(): string {
    return new Intl.DateTimeFormat('es-AR', { weekday: 'long', day: 'numeric', month: 'long' }).format(
      new Date(`${this.selectedDate}T00:00:00`)
    );
  }

  scheduleLabel(item: MobileGymClass): string {
    if (item.allDay) {
      return 'Todo el dia';
    }
    return `${item.startTime?.slice(0, 5)} - ${item.endTime?.slice(0, 5)}`;
  }

  subscriptionLabel(item: MobileGymClass): string {
    if (item.ownClass) {
      return 'La das vos';
    }
    if (item.subscribed) {
      return 'Ya anotado';
    }
    if (item.availableSpots === 0) {
      return 'Sin cupos';
    }
    return 'Anotarme';
  }

  statusLabel(item: MobileGymClass): string {
    if (item.subscribed) {
      return 'Ya anotado';
    }
    if (item.ownClass) {
      return 'La das vos';
    }
    if (item.canSubscribe) {
      return 'Disponible';
    }
    return 'No disponible';
  }

  toggleAvailableFilter(): void {
    this.showOnlyEnabledAvailable = !this.showOnlyEnabledAvailable;
  }

  get myClasses(): MobileGymClass[] {
    return this.classes.filter((item) => item.subscribed);
  }

  get availableClasses(): MobileGymClass[] {
    return this.classes.filter((item) => {
      if (item.subscribed || this.hasStarted(item)) {
        return false;
      }
      if (this.showOnlyEnabledAvailable && !item.canSubscribe) {
        return false;
      }
      return true;
    });
  }

  canGoToPreviousDay(): boolean {
    const current = new Date(`${this.selectedDate}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return current > today;
  }

  private hasStarted(item: MobileGymClass): boolean {
    const now = new Date();
    const selected = new Date(`${this.selectedDate}T00:00:00`);
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    if (selected.getTime() !== today.getTime()) {
      return false;
    }
    if (item.allDay) {
      return true;
    }
    if (!item.startTime) {
      return false;
    }
    const [hours, minutes] = item.startTime.slice(0, 5).split(':').map(Number);
    const classStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hours, minutes, 0, 0);
    return classStart <= now;
  }

  private loadClasses(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.mobileAppService.getClasses(this.selectedDate).subscribe({
      next: (classes) => {
        this.classes = classes;
        this.isLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudieron cargar las clases.';
        this.isLoading = false;
      }
    });
  }

  private presentToast(message: string): void {
    this.toastMessage = message;
    this.toastOpen = true;
  }
}
