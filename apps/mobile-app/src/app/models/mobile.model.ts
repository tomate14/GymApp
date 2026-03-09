export type PaymentStatus = 'al-dia' | 'adeuda';
export type UserType = 'alumno' | 'profesor';
export type PrescriptionMode = 'FIJO' | 'DINAMICO';
export type WeekDay = 'lunes' | 'martes' | 'miercoles' | 'jueves' | 'viernes' | 'sabado' | 'domingo';

export type MobileProfile = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  birthDate: string;
  userType: UserType;
  planName: string | null;
  planPrice: number | null;
  renewalDate: string;
  paymentStatus: PaymentStatus;
};

export type MobileProfileUpdateRequest = {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  birthDate: string;
};

export type MobilePaymentMovement = {
  id: number;
  planName: string;
  amount: number;
  detail: string;
  paidAt: string;
};

export type MobileRoutineExercise = {
  exerciseId: number;
  exerciseName: string;
  activityName: string;
  machineName: string;
  videoUrl: string;
  sets: number;
  prescriptionMode: PrescriptionMode;
  reps: number | null;
  weight: number | null;
  minReps: number | null;
  maxReps: number | null;
  minWeight: number | null;
  maxWeight: number | null;
};

export type MobileRoutine = {
  id: number;
  routineTemplateName: string;
  status: string;
  days: WeekDay[];
  startDate: string;
  endDate: string;
  exercises: MobileRoutineExercise[];
};

export type MobilePersonalRecord = {
  exerciseId: number;
  exerciseName: string;
  activityName: string;
  maxWeight: number | null;
  recordedAt: string | null;
};

export type MobilePersonalRecordHistoryPoint = {
  id: number;
  weight: number;
  recordedAt: string;
};

export type MobilePersonalRecordDetail = {
  exerciseId: number;
  exerciseName: string;
  activityName: string;
  maxWeight: number | null;
  recordedAt: string | null;
  history: MobilePersonalRecordHistoryPoint[];
};

export type MobilePersonalRecordRequest = {
  maxWeight: number;
  recordedAt: string;
};

export type MobileGymClass = {
  id: number;
  className: string;
  activityName: string;
  teacherName: string;
  targetDate: string;
  allDay: boolean;
  startTime: string | null;
  endTime: string | null;
  maxCapacity: number | null;
  enrolled: number;
  availableSpots: number | null;
  subscribed: boolean;
  ownClass: boolean;
  canSubscribe: boolean;
};
