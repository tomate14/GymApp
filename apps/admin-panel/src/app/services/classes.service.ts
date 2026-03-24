import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  GymClassAttendance,
  GymClassAttendanceUpdateItem
} from '../class/gym-class-attendance.model';
import { PageResponse } from '../class/page.model';
import { GymClass, GymClassDraft, WeekDay } from '../class/gym-class.model';
import { API_BASE_URL } from '../utils/api.util';

type GymClassApiDto = {
  id: number;
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  enrolled: number;
  teacherId: number | null;
  weekDays: WeekDay[];
  allDay: boolean;
  startTime: string | null;
  endTime: string | null;
};

type GymClassApiRequest = {
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  teacherId: number | null;
  weekDays: WeekDay[];
  allDay: boolean;
  startTime: string | null;
  endTime: string | null;
};

type GymClassAttendanceStudentApiDto = {
  subscriptionId: number;
  studentId: number;
  studentName: string;
  dni: string;
  phone: string;
  attendancePresent: boolean | null;
  attendanceMarkedAt: string | null;
};

type GymClassAttendanceApiDto = {
  classId: number;
  className: string;
  targetDate: string;
  students: GymClassAttendanceStudentApiDto[];
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type GymClassPageFilters = {
  className?: string;
  activityId?: number | 0;
  teacherId?: number | 0;
  weekDay?: WeekDay | '';
};

@Injectable({
  providedIn: 'root'
})
export class ClassesService {
  private readonly endpoint = `${API_BASE_URL}/classes`;
  private classes: GymClass[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<GymClass[]> {
    return this.http.get<GymClassApiDto[]>(this.endpoint).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => this.setAll(items))
    );
  }

  findPage(filters: GymClassPageFilters, page: number, size: number): Observable<PageResponse<GymClass>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.className && filters.className.trim() !== '') {
      params = params.set('className', filters.className.trim());
    }
    if (filters.activityId && filters.activityId > 0) {
      params = params.set('activityId', String(filters.activityId));
    }
    if (filters.teacherId && filters.teacherId > 0) {
      params = params.set('teacherId', String(filters.teacherId));
    }
    if (filters.weekDay) {
      params = params.set('weekDay', filters.weekDay);
    }

    return this.http.get<PageApiDto<GymClassApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
    );
  }

  getAll(): GymClass[] {
    return [...this.classes];
  }

  setAll(classes: GymClass[]): void {
    this.classes = classes.map((item) => ({ ...item }));
  }

  upsert(draft: GymClassDraft, editingId: number | null): Observable<GymClass> {
    const payload = this.toApiRequest(draft);
    const request$ =
      editingId === null
        ? this.http.post<GymClassApiDto>(this.endpoint, payload)
        : this.http.put<GymClassApiDto>(`${this.endpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(classId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${classId}`).pipe(
      tap(() => {
        this.classes = this.classes.filter((item) => item.id !== classId);
      })
    );
  }

  getAttendance(classId: number, date: string): Observable<GymClassAttendance> {
    const params = new HttpParams().set('date', date);
    return this.http
      .get<GymClassAttendanceApiDto>(`${this.endpoint}/${classId}/attendance`, { params })
      .pipe(map((response) => this.mapAttendanceFromApi(response)));
  }

  updateAttendance(classId: number, date: string, students: GymClassAttendanceUpdateItem[]): Observable<GymClassAttendance> {
    const params = new HttpParams().set('date', date);
    return this.http
      .put<GymClassAttendanceApiDto>(`${this.endpoint}/${classId}/attendance`, { students }, { params })
      .pipe(map((response) => this.mapAttendanceFromApi(response)));
  }

  clearTeacherReferences(userId: number): void {
    this.classes = this.classes.map((item) => (item.teacherId === userId ? { ...item, teacherId: null } : item));
  }

  clearActivityReferences(activityId: number): void {
    this.classes = this.classes.map((item) => (item.activityId === activityId ? { ...item, activityId: null } : item));
  }

  private toApiRequest(draft: GymClassDraft): GymClassApiRequest {
    const allDay = Boolean(draft.allDay);
    return {
      className: draft.className,
      activityId: draft.activityId,
      maxCapacity: draft.maxCapacity,
      teacherId: draft.teacherId,
      weekDays: draft.weekDays,
      allDay,
      startTime: allDay ? null : this.normalizeTime(draft.startTime),
      endTime: allDay ? null : this.normalizeTime(draft.endTime)
    };
  }

  private upsertInCache(payload: GymClass): void {
    const exists = this.classes.some((item) => item.id === payload.id);
    if (!exists) {
      this.classes = [payload, ...this.classes];
      return;
    }

    this.classes = this.classes.map((item) => (item.id === payload.id ? payload : item));
  }

  private mapFromApi(dto: GymClassApiDto): GymClass {
    return {
      id: Number(dto.id),
      className: dto.className,
      activityId: dto.activityId === null ? null : Number(dto.activityId),
      maxCapacity: dto.maxCapacity === null ? null : Number(dto.maxCapacity),
      enrolled: Number(dto.enrolled),
      teacherId: dto.teacherId === null ? null : Number(dto.teacherId),
      weekDays: dto.weekDays ?? [],
      allDay: Boolean(dto.allDay),
      startTime: dto.startTime ? dto.startTime.slice(0, 5) : '',
      endTime: dto.endTime ? dto.endTime.slice(0, 5) : ''
    };
  }

  private normalizeTime(value: string): string | null {
    if (!value || value.trim() === '') {
      return null;
    }
    return value.length === 5 ? `${value}:00` : value;
  }

  private mapPageFromApi(dto: PageApiDto<GymClassApiDto>): PageResponse<GymClass> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }

  private mapAttendanceFromApi(dto: GymClassAttendanceApiDto): GymClassAttendance {
    return {
      classId: Number(dto.classId),
      className: dto.className,
      targetDate: dto.targetDate,
      students: dto.students.map((item) => ({
        subscriptionId: Number(item.subscriptionId),
        studentId: Number(item.studentId),
        studentName: item.studentName,
        dni: item.dni,
        phone: item.phone,
        attendancePresent: item.attendancePresent,
        attendanceMarkedAt: item.attendanceMarkedAt
      }))
    };
  }
}
