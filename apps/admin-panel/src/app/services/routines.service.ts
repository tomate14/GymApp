import { Injectable } from '@angular/core';
import {
  RoutineAssignment,
  RoutineAssignmentDraft,
  RoutineTemplate,
  RoutineTemplateDraft
} from '../class/routine.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class RoutinesService {
  private routineTemplates: RoutineTemplate[] = [
    {
      id: 1,
      name: 'Hipertrofia inicial',
      createdAt: '2026-01-12',
      exercises: [
        { exerciseId: 1, exerciseName: 'Sentadilla en Smith' },
        { exerciseId: 2, exerciseName: 'Burpee con salto' }
      ]
    },
    {
      id: 2,
      name: 'Resistencia metabolica',
      createdAt: '2026-01-20',
      exercises: [
        { exerciseId: 2, exerciseName: 'Burpee con salto' },
        { exerciseId: 5, exerciseName: 'Plancha frontal' },
        { exerciseId: 6, exerciseName: 'Sprint en bicicleta' }
      ]
    },
    {
      id: 3,
      name: 'Fuerza tren inferior',
      createdAt: '2026-01-27',
      exercises: [
        { exerciseId: 1, exerciseName: 'Sentadilla en Smith' },
        { exerciseId: 4, exerciseName: 'Prensa de piernas' },
        { exerciseId: 3, exerciseName: 'Remo en polea baja' }
      ]
    },
    {
      id: 4,
      name: 'Movilidad y core',
      createdAt: '2026-02-03',
      exercises: [
        { exerciseId: 5, exerciseName: 'Plancha frontal' },
        { exerciseId: 7, exerciseName: 'Bird-dog controlado' },
        { exerciseId: 2, exerciseName: 'Burpee con salto' }
      ]
    }
  ];

  private routineAssignments: RoutineAssignment[] = [
    {
      id: 1,
      userId: 2,
      routineTemplateId: 1,
      days: ['lunes', 'miercoles', 'viernes'],
      startDate: '2026-03-01',
      endDate: '2026-05-01',
      exercisePlans: [
        {
          exerciseId: 1,
          exerciseName: 'Sentadilla en Smith',
          sets: 4,
          prescriptionMode: 'fijo',
          reps: 10,
          weight: 45,
          minReps: null,
          maxReps: null,
          minWeight: null,
          maxWeight: null
        },
        {
          exerciseId: 2,
          exerciseName: 'Burpee con salto',
          sets: 3,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 10,
          maxReps: 14,
          minWeight: 0,
          maxWeight: 0
        }
      ]
    },
    {
      id: 2,
      userId: 4,
      routineTemplateId: 2,
      days: ['martes', 'jueves', 'sabado'],
      startDate: '2026-03-03',
      endDate: '2026-05-03',
      exercisePlans: [
        {
          exerciseId: 2,
          exerciseName: 'Burpee con salto',
          sets: 4,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 10,
          maxReps: 16,
          minWeight: 0,
          maxWeight: 0
        },
        {
          exerciseId: 5,
          exerciseName: 'Plancha frontal',
          sets: 4,
          prescriptionMode: 'fijo',
          reps: 45,
          weight: 0,
          minReps: null,
          maxReps: null,
          minWeight: null,
          maxWeight: null
        },
        {
          exerciseId: 6,
          exerciseName: 'Sprint en bicicleta',
          sets: 6,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 20,
          maxReps: 30,
          minWeight: 0,
          maxWeight: 0
        }
      ]
    },
    {
      id: 3,
      userId: 6,
      routineTemplateId: 3,
      days: ['lunes', 'jueves'],
      startDate: '2026-03-01',
      endDate: '2026-04-30',
      exercisePlans: [
        {
          exerciseId: 1,
          exerciseName: 'Sentadilla en Smith',
          sets: 5,
          prescriptionMode: 'fijo',
          reps: 8,
          weight: 60,
          minReps: null,
          maxReps: null,
          minWeight: null,
          maxWeight: null
        },
        {
          exerciseId: 4,
          exerciseName: 'Prensa de piernas',
          sets: 4,
          prescriptionMode: 'fijo',
          reps: 10,
          weight: 120,
          minReps: null,
          maxReps: null,
          minWeight: null,
          maxWeight: null
        },
        {
          exerciseId: 3,
          exerciseName: 'Remo en polea baja',
          sets: 4,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 8,
          maxReps: 12,
          minWeight: 35,
          maxWeight: 50
        }
      ]
    },
    {
      id: 4,
      userId: 7,
      routineTemplateId: 4,
      days: ['miercoles', 'viernes'],
      startDate: '2026-03-05',
      endDate: '2026-05-10',
      exercisePlans: [
        {
          exerciseId: 5,
          exerciseName: 'Plancha frontal',
          sets: 4,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 30,
          maxReps: 60,
          minWeight: 0,
          maxWeight: 0
        },
        {
          exerciseId: 7,
          exerciseName: 'Bird-dog controlado',
          sets: 3,
          prescriptionMode: 'fijo',
          reps: 12,
          weight: 0,
          minReps: null,
          maxReps: null,
          minWeight: null,
          maxWeight: null
        },
        {
          exerciseId: 2,
          exerciseName: 'Burpee con salto',
          sets: 3,
          prescriptionMode: 'dinamico',
          reps: null,
          weight: null,
          minReps: 8,
          maxReps: 12,
          minWeight: 0,
          maxWeight: 0
        }
      ]
    }
  ];

  getTemplates(): RoutineTemplate[] {
    return this.routineTemplates.map((template) => this.cloneTemplate(template));
  }

  getAssignments(): RoutineAssignment[] {
    return this.routineAssignments.map((assignment) => this.cloneAssignment(assignment));
  }

  getTemplateById(templateId: number): RoutineTemplate | null {
    const template = this.routineTemplates.find((item) => item.id === templateId);
    return template ? this.cloneTemplate(template) : null;
  }

  upsertTemplate(draft: RoutineTemplateDraft, editingId: number | null): RoutineTemplate {
    const existing = editingId === null ? null : this.routineTemplates.find((template) => template.id === editingId) ?? null;
    const payload: RoutineTemplate = {
      id: editingId ?? nextNumericId(this.routineTemplates),
      name: draft.name,
      createdAt: existing?.createdAt ?? draft.createdAt ?? new Date().toISOString().slice(0, 10),
      exercises: draft.exercises.map((item) => ({ ...item }))
    };

    if (editingId === null) {
      this.routineTemplates = [payload, ...this.routineTemplates];
      return this.cloneTemplate(payload);
    }

    this.routineTemplates = this.routineTemplates.map((template) => (template.id === editingId ? payload : template));
    return this.cloneTemplate(payload);
  }

  upsertAssignment(draft: RoutineAssignmentDraft, editingId: number | null): RoutineAssignment {
    const payload: RoutineAssignment = {
      id: editingId ?? nextNumericId(this.routineAssignments),
      userId: draft.userId,
      routineTemplateId: draft.routineTemplateId,
      days: [...draft.days],
      startDate: draft.startDate,
      endDate: draft.endDate,
      exercisePlans: draft.exercisePlans.map((item) => ({ ...item }))
    };

    if (editingId === null) {
      this.routineAssignments = [payload, ...this.routineAssignments];
      return this.cloneAssignment(payload);
    }

    this.routineAssignments = this.routineAssignments.map((assignment) =>
      assignment.id === editingId ? payload : assignment
    );
    return this.cloneAssignment(payload);
  }

  removeTemplate(templateId: number): void {
    this.routineTemplates = this.routineTemplates.filter((template) => template.id !== templateId);
    this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.routineTemplateId !== templateId);
  }

  removeAssignment(assignmentId: number): void {
    this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.id !== assignmentId);
  }

  clearUserReferences(userId: number): void {
    this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.userId !== userId);
  }

  removeExerciseFromRoutines(exerciseId: number): void {
    this.routineTemplates = this.routineTemplates.map((template) => ({
      ...template,
      exercises: template.exercises.filter((item) => item.exerciseId !== exerciseId)
    }));

    this.routineAssignments = this.routineAssignments.map((assignment) => ({
      ...assignment,
      exercisePlans: assignment.exercisePlans.filter((item) => item.exerciseId !== exerciseId)
    }));
  }

  renameExerciseReference(exerciseId: number, exerciseName: string): void {
    this.routineTemplates = this.routineTemplates.map((template) => ({
      ...template,
      exercises: template.exercises.map((item) => (item.exerciseId === exerciseId ? { ...item, exerciseName } : item))
    }));

    this.routineAssignments = this.routineAssignments.map((assignment) => ({
      ...assignment,
      exercisePlans: assignment.exercisePlans.map((item) =>
        item.exerciseId === exerciseId ? { ...item, exerciseName } : item
      )
    }));
  }

  templateNameById(templateId: number): string {
    return this.routineTemplates.find((template) => template.id === templateId)?.name ?? 'Sin rutina';
  }

  private cloneTemplate(template: RoutineTemplate): RoutineTemplate {
    return {
      ...template,
      exercises: template.exercises.map((item) => ({ ...item }))
    };
  }

  private cloneAssignment(assignment: RoutineAssignment): RoutineAssignment {
    return {
      ...assignment,
      days: [...assignment.days],
      exercisePlans: assignment.exercisePlans.map((item) => ({ ...item }))
    };
  }
}
