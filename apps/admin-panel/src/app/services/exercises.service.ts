import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import { Exercise, ExerciseDraft } from '../class/exercise.model';
import { API_BASE_URL } from '../utils/api.util';

type ExerciseApiDto = {
  id: number;
  name: string;
  activityId: number | null;
  machineId: number | null;
  videoUrl: string;
};

type ExerciseApiRequest = {
  name: string;
  activityId: number | null;
  machineId: number | null;
  videoUrl: string;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ExercisePageFilters = {
  name?: string;
  activityId?: number | 0;
  machineId?: number | 0;
};

@Injectable({
  providedIn: 'root'
})
export class ExercisesService {
  private readonly endpoint = `${API_BASE_URL}/exercises`;
  private exercises: Exercise[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<Exercise[]> {
    return this.http.get<ExerciseApiDto[]>(this.endpoint).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => this.setAll(items))
    );
  }

  findPage(filters: ExercisePageFilters, page: number, size: number): Observable<PageResponse<Exercise>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }
    if (filters.activityId && filters.activityId > 0) {
      params = params.set('activityId', String(filters.activityId));
    }
    if (filters.machineId && filters.machineId > 0) {
      params = params.set('machineId', String(filters.machineId));
    }

    return this.http.get<PageApiDto<ExerciseApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
    );
  }

  getAll(): Exercise[] {
    return [...this.exercises];
  }

  setAll(exercises: Exercise[]): void {
    this.exercises = exercises.map((item) => ({ ...item }));
  }

  upsert(draft: ExerciseDraft, editingId: number | null): Observable<Exercise> {
    const payload = this.toApiRequest(draft);
    const request$ =
      editingId === null
        ? this.http.post<ExerciseApiDto>(this.endpoint, payload)
        : this.http.put<ExerciseApiDto>(`${this.endpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(exerciseId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${exerciseId}`).pipe(
      tap(() => {
        this.exercises = this.exercises.filter((exercise) => exercise.id !== exerciseId);
      })
    );
  }

  clearActivityReferences(activityId: number): void {
    this.exercises = this.exercises.map((exercise) =>
      exercise.activityId === activityId ? { ...exercise, activityId: null } : exercise
    );
  }

  clearMachineReferences(machineId: number): void {
    this.exercises = this.exercises.map((exercise) =>
      exercise.machineId === machineId ? { ...exercise, machineId: null } : exercise
    );
  }

  nameById(exerciseId: number): string | null {
    return this.exercises.find((exercise) => exercise.id === exerciseId)?.name ?? null;
  }

  private toApiRequest(draft: ExerciseDraft): ExerciseApiRequest {
    return {
      name: draft.name,
      activityId: draft.activityId,
      machineId: draft.machineId,
      videoUrl: draft.videoUrl
    };
  }

  private upsertInCache(payload: Exercise): void {
    const exists = this.exercises.some((item) => item.id === payload.id);
    if (!exists) {
      this.exercises = [payload, ...this.exercises];
      return;
    }

    this.exercises = this.exercises.map((item) => (item.id === payload.id ? payload : item));
  }

  private mapFromApi(dto: ExerciseApiDto): Exercise {
    return {
      id: Number(dto.id),
      name: dto.name,
      activityId: dto.activityId === null ? null : Number(dto.activityId),
      videoUrl: dto.videoUrl,
      machineId: dto.machineId === null ? null : Number(dto.machineId)
    };
  }

  private mapPageFromApi(dto: PageApiDto<ExerciseApiDto>): PageResponse<Exercise> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }
}
