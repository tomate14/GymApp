import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { IonButton, IonCard, IonCardContent, IonSpinner } from '@ionic/angular/standalone';
import { MobileGymClass } from '../../models/mobile.model';
import { MobileAppService } from '../../services/mobile-app.service';

@Component({
  selector: 'app-classes-page',
  standalone: true,
  imports: [CommonModule, IonCard, IonCardContent, IonButton, IonSpinner],
  templateUrl: './classes.page.html',
  styleUrl: './classes.page.css'
})
export class ClassesPageComponent implements OnInit {
  classes: MobileGymClass[] = [];
  selectedDate = new Date().toISOString().slice(0, 10);
  errorMessage: string | null = null;
  isLoading = true;

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

  subscribe(item: MobileGymClass): void {
    if (!item.canSubscribe) {
      return;
    }

    this.mobileAppService.subscribeToClass(item.id, this.selectedDate).subscribe({
      next: (updated) => {
        this.classes = this.classes.map((current) => current.id === updated.id ? updated : current);
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo registrar la suscripcion.';
      }
    });
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
      return 'La dictas vos';
    }
    if (item.subscribed) {
      return 'Ya suscripto';
    }
    if (item.availableSpots === 0) {
      return 'Sin cupos';
    }
    return 'Suscribirme';
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
}
