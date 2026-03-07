import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import { Activity, ActivityDraft } from '../class/activity.model';
import { API_BASE_URL } from '../utils/api.util';

type ActivityApiDto = {
  id: number;
  name: string;
  description: string;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ActivityPageFilters = {
  name?: string;
  description?: string;
};

@Injectable({
  providedIn: 'root'
})
export class ActivitiesService {
  private readonly endpoint = `${API_BASE_URL}/activities`;
  private activities: Activity[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<Activity[]> {
    return this.http.get<ActivityApiDto[]>(this.endpoint).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => this.setAll(items))
    );
  }

  findPage(filters: ActivityPageFilters, page: number, size: number): Observable<PageResponse<Activity>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }
    if (filters.description && filters.description.trim() !== '') {
      params = params.set('description', filters.description.trim());
    }

    return this.http.get<PageApiDto<ActivityApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
    );
  }

  getAll(): Activity[] {
    return [...this.activities];
  }

  setAll(activities: Activity[]): void {
    this.activities = activities.map((item) => ({ ...item }));
  }

  upsert(draft: ActivityDraft, editingId: number | null): Observable<Activity> {
    const request$ =
      editingId === null
        ? this.http.post<ActivityApiDto>(this.endpoint, draft)
        : this.http.put<ActivityApiDto>(`${this.endpoint}/${editingId}`, draft);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(activityId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${activityId}`).pipe(
      tap(() => {
        this.activities = this.activities.filter((activity) => activity.id !== activityId);
      })
    );
  }

  nameById(activityId: number | null): string {
    if (activityId === null) {
      return 'Sin actividad';
    }

    return this.activities.find((activity) => activity.id === activityId)?.name ?? 'Sin actividad';
  }

  private upsertInCache(payload: Activity): void {
    const exists = this.activities.some((item) => item.id === payload.id);
    if (!exists) {
      this.activities = [payload, ...this.activities];
      return;
    }

    this.activities = this.activities.map((item) => (item.id === payload.id ? payload : item));
  }

  private mapFromApi(dto: ActivityApiDto): Activity {
    return {
      id: Number(dto.id),
      name: dto.name,
      description: dto.description
    };
  }

  private mapPageFromApi(dto: PageApiDto<ActivityApiDto>): PageResponse<Activity> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }
}
