import { Injectable } from '@angular/core';
import { Activity, ActivityDraft } from '../class/activity.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class ActivitiesService {
  private activities: Activity[] = [
    { id: 1, name: 'Musculacion', description: 'Sala libre con seguimiento mensual' },
    { id: 2, name: 'Crossfit', description: 'Entrenamiento funcional de alta intensidad' },
    { id: 3, name: 'Pilates', description: 'Clases grupales de movilidad y control corporal' },
    { id: 4, name: 'Spinning', description: 'Trabajo cardiovascular guiado en bicicleta fija' },
    { id: 5, name: 'Funcional', description: 'Circuitos de fuerza, coordinacion y resistencia' },
    { id: 6, name: 'Yoga', description: 'Practica de movilidad, respiracion y flexibilidad' }
  ];

  getAll(): Activity[] {
    return [...this.activities];
  }

  upsert(draft: ActivityDraft, editingId: number | null): Activity {
    const payload: Activity = {
      id: editingId ?? nextNumericId(this.activities),
      ...draft
    };

    if (editingId === null) {
      this.activities = [payload, ...this.activities];
      return payload;
    }

    this.activities = this.activities.map((activity) => (activity.id === editingId ? payload : activity));
    return payload;
  }

  remove(activityId: number): void {
    this.activities = this.activities.filter((activity) => activity.id !== activityId);
  }

  nameById(activityId: number | null): string {
    if (activityId === null) {
      return 'Sin actividad';
    }

    return this.activities.find((activity) => activity.id === activityId)?.name ?? 'Sin actividad';
  }
}
