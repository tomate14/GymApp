export interface Exercise {
  id: number;
  name: string;
  activityId: number | null;
  videoUrl: string;
  machineId: number | null;
}

export type ExerciseDraft = Omit<Exercise, 'id'>;

export const createEmptyExerciseDraft = (): ExerciseDraft => ({
  name: '',
  activityId: null,
  videoUrl: '',
  machineId: null
});

