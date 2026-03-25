export interface DashboardKpi {
  key: string;
  label: string;
  value: string;
  hint: string;
}

export interface DashboardAlert {
  key: string;
  label: string;
  count: number;
  severity: 'low' | 'medium' | 'high';
}

export interface DashboardChartPoint {
  label: string;
  value: number;
}

export interface DashboardChart {
  key: string;
  title: string;
  type: 'donut' | 'bar' | 'line';
  points: DashboardChartPoint[];
}

export interface DashboardSummary {
  kpis: DashboardKpi[];
  alerts: DashboardAlert[];
  charts: DashboardChart[];
}

export type DashboardStudentsTrendMode = 'month' | 'year';

export interface DashboardSummaryFilters {
  fromDate: string;
  toDate: string;
}

export interface DashboardStudentsTrendFilters {
  fromDate: string;
  toDate: string;
}

export interface DashboardRenewalsFilters {
  search: string;
  planId: number;
  paymentStatus: '' | 'al-dia' | 'adeuda';
  renewalFrom: string;
  renewalTo: string;
}

export interface DashboardClassesFilters {
  search: string;
  activityId: number;
  teacherId: number;
}

export interface DashboardRankingFilters {
  search: string;
  fromDate: string;
  toDate: string;
}

export interface DashboardOverloadsFilters {
  search: string;
}

export interface DashboardPageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DashboardRenewalRow {
  userId: number;
  studentName: string;
  dni: string;
  planName: string;
  renewalDate: string;
  paymentStatus: string;
  phone: string;
}

export interface DashboardClassRow {
  classId: number;
  className: string;
  activityName: string;
  teacherName: string;
  date: string;
  weekDay: string;
  allDay: boolean;
  startTime: string | null;
  endTime: string | null;
  enrolled: number;
  maxCapacity: number | null;
  occupancyPercent: number;
}

export interface DashboardActivityRankingRow {
  activityId: number | null;
  activityName: string;
  classesCount: number;
  totalEnrolled: number;
  averageOccupancyPercent: number;
}

export interface DashboardPlanOverloadRow {
  userId: number;
  studentName: string;
  dni: string;
  planName: string;
  maxDaysPerWeek: number;
  assignedDays: number;
  extraDays: number;
}
