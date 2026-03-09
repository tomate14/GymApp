import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { IonBadge, IonButton, IonCard, IonCardContent, IonIcon, IonSpinner } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { playCircleOutline } from 'ionicons/icons';
import { MobileRoutine } from '../../models/mobile.model';
import { MobileAppService } from '../../services/mobile-app.service';

@Component({
  selector: 'app-routines-page',
  standalone: true,
  imports: [CommonModule, IonCard, IonCardContent, IonBadge, IonButton, IonIcon, IonSpinner],
  templateUrl: './routines.page.html',
  styleUrl: './routines.page.css'
})
export class RoutinesPageComponent implements OnInit {
  routines: MobileRoutine[] = [];
  errorMessage: string | null = null;
  isLoading = true;
  expandedRoutineId: number | null = null;

  private readonly previewImages: Array<{ keywords: string[]; url: string }> = [
    {
      keywords: ['pecho', 'press', 'bench'],
      url: 'https://images.unsplash.com/photo-1579758629938-03607ccdbaba?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['espalda', 'remo', 'pulldown', 'dominadas'],
      url: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['pierna', 'leg', 'sentadilla', 'prensa'],
      url: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['cardio', 'cinta', 'run', 'bike'],
      url: 'https://images.unsplash.com/photo-1599058917765-a780eda07a3e?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['hombro', 'shoulder'],
      url: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['brazo', 'biceps', 'triceps', 'curl'],
      url: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=900&q=80'
    },
    {
      keywords: ['core', 'abs', 'abdominal'],
      url: 'https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?auto=format&fit=crop&w=900&q=80'
    }
  ];

  constructor(private readonly mobileAppService: MobileAppService) {
    addIcons({ playCircleOutline });
  }

  ngOnInit(): void {
    this.mobileAppService.getRoutines().subscribe({
      next: (routines) => {
        this.routines = routines;
        this.isLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudieron cargar tus rutinas.';
        this.isLoading = false;
      }
    });
  }

  toggleRoutine(id: number): void {
    this.expandedRoutineId = this.expandedRoutineId === id ? null : id;
  }

  formatDays(days: string[]): string {
    return days.map((day) => day.slice(0, 3).toUpperCase()).join(' / ');
  }

  repsLabel(exercise: MobileRoutine['exercises'][number]): string {
    if (exercise.reps !== null && exercise.reps !== undefined) {
      return `${exercise.reps} reps`;
    }

    return `${exercise.minReps ?? '-'}-${exercise.maxReps ?? '-'} reps`;
  }

  weightLabel(exercise: MobileRoutine['exercises'][number]): string {
    if (exercise.weight !== null && exercise.weight !== undefined) {
      return `${exercise.weight} kg`;
    }

    const hasMin = exercise.minWeight !== null && exercise.minWeight !== undefined;
    const hasMax = exercise.maxWeight !== null && exercise.maxWeight !== undefined;

    if (hasMin && hasMax) {
      return `${exercise.minWeight}-${exercise.maxWeight} kg`;
    }
    if (hasMin) {
      return `${exercise.minWeight}+ kg`;
    }
    if (hasMax) {
      return `hasta ${exercise.maxWeight} kg`;
    }
    return 'Peso libre';
  }

  exerciseBackground(exercise: MobileRoutine['exercises'][number]): string {
    const normalized = this.normalizeText(`${exercise.activityName} ${exercise.exerciseName}`);
    const match = this.previewImages.find((item) => item.keywords.some((keyword) => normalized.includes(keyword)));
    const imageUrl = match?.url ?? 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=900&q=80';

    return `linear-gradient(90deg, rgba(0, 0, 0, 0.88) 0%, rgba(0, 0, 0, 0.64) 38%, rgba(0, 0, 0, 0.28) 72%, rgba(0, 0, 0, 0.08) 100%), url('${imageUrl}')`;
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }
}
