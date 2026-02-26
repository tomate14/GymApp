import { Injectable } from '@angular/core';
import { Exercise, ExerciseDraft } from '../class/exercise.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class ExercisesService {
  private exercises: Exercise[] = [
    {
      id: 1,
      name: 'Sentadilla en Smith',
      activityId: 1,
      videoUrl: 'https://www.youtube.com/watch?v=SW_C1A-rejs',
      machineId: 1
    },
    {
      id: 2,
      name: 'Burpee con salto',
      activityId: 2,
      videoUrl: 'https://www.youtube.com/watch?v=TU8QYVW0gDU',
      machineId: null
    },
    {
      id: 3,
      name: 'Remo en polea baja',
      activityId: 1,
      videoUrl: 'https://www.youtube.com/watch?v=GZbfZ033f74',
      machineId: 2
    },
    {
      id: 4,
      name: 'Prensa de piernas',
      activityId: 1,
      videoUrl: 'https://www.youtube.com/watch?v=IZxyjW7MPJQ',
      machineId: 3
    },
    {
      id: 5,
      name: 'Plancha frontal',
      activityId: 5,
      videoUrl: 'https://www.youtube.com/watch?v=pSHjTRCQxIw',
      machineId: null
    },
    {
      id: 6,
      name: 'Sprint en bicicleta',
      activityId: 4,
      videoUrl: 'https://www.youtube.com/watch?v=RjQf3Q9M9jE',
      machineId: null
    },
    {
      id: 7,
      name: 'Bird-dog controlado',
      activityId: 6,
      videoUrl: 'https://www.youtube.com/watch?v=wiFNA3sqjCA',
      machineId: null
    }
  ];

  getAll(): Exercise[] {
    return [...this.exercises];
  }

  upsert(draft: ExerciseDraft, editingId: number | null): Exercise {
    const payload: Exercise = {
      id: editingId ?? nextNumericId(this.exercises),
      ...draft
    };

    if (editingId === null) {
      this.exercises = [payload, ...this.exercises];
      return payload;
    }

    this.exercises = this.exercises.map((exercise) => (exercise.id === editingId ? payload : exercise));
    return payload;
  }

  remove(exerciseId: number): void {
    this.exercises = this.exercises.filter((exercise) => exercise.id !== exerciseId);
  }

  clearActivityReferences(activityId: number): void {
    this.exercises = this.exercises.map((exercise) =>
      exercise.activityId === activityId ? { ...exercise, activityId: null } : exercise
    );
  }

  clearMachineReferences(machineId: number): void {
    this.exercises = this.exercises.map((exercise) =>
      exercise.machineId === machineId ? { ...exercise, machineId: null } : exercise
    );
  }

  nameById(exerciseId: number): string | null {
    return this.exercises.find((exercise) => exercise.id === exerciseId)?.name ?? null;
  }
}
