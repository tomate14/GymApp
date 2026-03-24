export interface GymClassAttendanceStudent {
  subscriptionId: number;
  studentId: number;
  studentName: string;
  dni: string;
  phone: string;
  attendancePresent: boolean | null;
  attendanceMarkedAt: string | null;
}

export interface GymClassAttendance {
  classId: number;
  className: string;
  targetDate: string;
  students: GymClassAttendanceStudent[];
}

export interface GymClassAttendanceUpdateItem {
  subscriptionId: number;
  attendancePresent: boolean;
}
