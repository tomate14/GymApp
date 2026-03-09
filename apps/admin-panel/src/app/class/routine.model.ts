export type DayOfWeek = 'lunes' | 'martes' | 'miercoles' | 'jueves' | 'viernes' | 'sabado' | 'domingo';
export type PrescriptionMode = 'fijo' | 'dinamico';
export type RoutineAssignmentKind = 'generica' | 'custom';

export interface RoutineTemplateExercise {
  exerciseId: number;
  exerciseName: string;
}

export interface RoutineTemplate {
  id: number;
  name: string;
  createdAt: string;
  exercises: RoutineTemplateExercise[];
}

export interface RoutineAssignmentExercise {
  exerciseId: number;
  exerciseName: string;
  sets: number;
  prescriptionMode: PrescriptionMode;
  reps: number | null;
  weight: number | null;
  minReps: number | null;
  maxReps: number | null;
  minWeight: number | null;
  maxWeight: number | null;
}

export interface RoutineAssignment {
  id: number;
  userId: number;
  routineTemplateId: number | null;
  routineTemplateName: string;
  customName: string | null;
  custom: boolean;
  days: DayOfWeek[];
  startDate: string;
  endDate: string;
  exercisePlans: RoutineAssignmentExercise[];
}

export type RoutineTemplateDraft = Omit<RoutineTemplate, 'id'>;
export interface RoutineAssignmentDraft {
  userId: number;
  routineTemplateId: number;
  routineKind: RoutineAssignmentKind;
  customName: string;
  days: DayOfWeek[];
  startDate: string;
  endDate: string;
  exercisePlans: RoutineAssignmentExercise[];
}

export interface RoutineTemplateExerciseDraft {
  exerciseId: number | null;
}

export const createEmptyRoutineTemplateDraft = (): RoutineTemplateDraft => ({
  name: '',
  createdAt: '',
  exercises: []
});

export const createEmptyRoutineAssignmentDraft = (): RoutineAssignmentDraft => ({
  userId: 0,
  routineTemplateId: 0,
  routineKind: 'generica',
  customName: 'Rutina Custom',
  days: [],
  startDate: '',
  endDate: '',
  exercisePlans: []
});

export const createEmptyRoutineTemplateExerciseDraft = (): RoutineTemplateExerciseDraft => ({
  exerciseId: null
});
