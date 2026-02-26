import { Injectable } from '@angular/core';
import { GymClass, GymClassDraft } from '../class/gym-class.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class ClassesService {
  private classes: GymClass[] = [
    {
      id: 1,
      className: 'WOD Morning',
      activityId: 2,
      maxCapacity: 16,
      enrolled: 12,
      teacherId: 1,
      date: '2026-03-02',
      time: '08:00'
    },
    {
      id: 2,
      className: 'Sala Musculacion Turno Tarde',
      activityId: 1,
      maxCapacity: null,
      enrolled: 34,
      teacherId: null,
      date: '2026-03-02',
      time: '18:00'
    },
    {
      id: 3,
      className: 'Pilates suelo intermedio',
      activityId: 3,
      maxCapacity: 20,
      enrolled: 14,
      teacherId: 3,
      date: '2026-03-03',
      time: '09:30'
    },
    {
      id: 4,
      className: 'Spinning HIIT',
      activityId: 4,
      maxCapacity: 18,
      enrolled: 17,
      teacherId: 5,
      date: '2026-03-03',
      time: '19:00'
    },
    {
      id: 5,
      className: 'Funcional express',
      activityId: 5,
      maxCapacity: 22,
      enrolled: 19,
      teacherId: 3,
      date: '2026-03-04',
      time: '07:15'
    },
    {
      id: 6,
      className: 'Yoga movilidad',
      activityId: 6,
      maxCapacity: 25,
      enrolled: 11,
      teacherId: 5,
      date: '2026-03-04',
      time: '20:00'
    }
  ];

  getAll(): GymClass[] {
    return [...this.classes];
  }

  upsert(draft: GymClassDraft, editingId: number | null): GymClass {
    const payload: GymClass = {
      id: editingId ?? nextNumericId(this.classes),
      ...draft
    };

    if (editingId === null) {
      this.classes = [payload, ...this.classes];
      return payload;
    }

    this.classes = this.classes.map((item) => (item.id === editingId ? payload : item));
    return payload;
  }

  remove(classId: number): void {
    this.classes = this.classes.filter((item) => item.id !== classId);
  }

  clearTeacherReferences(userId: number): void {
    this.classes = this.classes.map((item) => (item.teacherId === userId ? { ...item, teacherId: null } : item));
  }

  clearActivityReferences(activityId: number): void {
    this.classes = this.classes.map((item) => (item.activityId === activityId ? { ...item, activityId: null } : item));
  }
}
