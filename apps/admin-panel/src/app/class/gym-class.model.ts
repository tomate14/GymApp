export interface GymClass {
  id: number;
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  enrolled: number;
  teacherId: number | null;
  date: string;
  time: string;
}

export type GymClassDraft = Omit<GymClass, 'id'>;

export const createEmptyClassDraft = (): GymClassDraft => ({
  className: '',
  activityId: null,
  maxCapacity: 20,
  enrolled: 0,
  teacherId: null,
  date: '',
  time: ''
});

