export type WeekDay = 'lunes' | 'martes' | 'miercoles' | 'jueves' | 'viernes' | 'sabado' | 'domingo';

export interface GymClass {
  id: number;
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  enrolled: number;
  teacherId: number | null;
  weekDays: WeekDay[];
  allDay: boolean;
  startTime: string;
  endTime: string;
}

export type GymClassDraft = Omit<GymClass, 'id'>;

export const createEmptyClassDraft = (): GymClassDraft => ({
  className: '',
  activityId: null,
  maxCapacity: 20,
  enrolled: 0,
  teacherId: null,
  weekDays: [],
  allDay: false,
  startTime: '',
  endTime: ''
});
