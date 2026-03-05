import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Activity, ActivityDraft } from '../class/activity.model';
import { API_BASE_URL } from '../utils/api.util';

type ActivityApiDto = {
  id: number;
  name: string;
  description: string;
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
}
