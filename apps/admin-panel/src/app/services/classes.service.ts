import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { GymClass, GymClassDraft } from '../class/gym-class.model';
import { API_BASE_URL } from '../utils/api.util';

type GymClassApiDto = {
  id: number;
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  enrolled: number;
  teacherId: number | null;
  date: string;
  time: string;
};

type GymClassApiRequest = {
  className: string;
  activityId: number | null;
  maxCapacity: number | null;
  teacherId: number | null;
  date: string;
  time: string;
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

  clearTeacherReferences(userId: number): void {
    this.classes = this.classes.map((item) => (item.teacherId === userId ? { ...item, teacherId: null } : item));
  }

  clearActivityReferences(activityId: number): void {
    this.classes = this.classes.map((item) => (item.activityId === activityId ? { ...item, activityId: null } : item));
  }

  private toApiRequest(draft: GymClassDraft): GymClassApiRequest {
    return {
      className: draft.className,
      activityId: draft.activityId,
      maxCapacity: draft.maxCapacity,
      teacherId: draft.teacherId,
      date: draft.date,
      time: draft.time.length === 5 ? `${draft.time}:00` : draft.time
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
      date: dto.date,
      time: dto.time.slice(0, 5)
    };
  }
}
