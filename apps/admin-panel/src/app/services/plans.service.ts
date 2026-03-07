import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import { Plan, PlanDraft } from '../class/plan.model';
import { API_BASE_URL } from '../utils/api.util';

type PlanApiDto = {
  id: number;
  name: string;
  price: number;
  maxDaysPerWeek: number;
  active: boolean;
};

type PlanApiRequest = {
  name: string;
  price: number;
  maxDaysPerWeek: number;
  active: boolean;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PlanPageFilters = {
  name?: string;
  status?: 'all' | 'active' | 'inactive';
};

@Injectable({
  providedIn: 'root'
})
export class PlansService {
  private readonly endpoint = `${API_BASE_URL}/plans`;
  private plans: Plan[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(activeOnly = false): Observable<Plan[]> {
    return this.http.get<PlanApiDto[]>(`${this.endpoint}?activeOnly=${activeOnly}`).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => {
        if (!activeOnly) {
          this.setAll(items);
        }
      })
    );
  }

  findPage(filters: PlanPageFilters, page: number, size: number): Observable<PageResponse<Plan>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }
    if (filters.status && filters.status !== 'all') {
      params = params.set('status', filters.status);
    }

    return this.http.get<PageApiDto<PlanApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
    );
  }

  getAll(): Plan[] {
    return [...this.plans];
  }

  setAll(plans: Plan[]): void {
    this.plans = plans.map((item) => ({ ...item }));
  }

  getActive(): Plan[] {
    return this.plans.filter((plan) => plan.isActive);
  }

  getById(planId: number | null): Plan | null {
    if (planId === null) {
      return null;
    }

    return this.plans.find((plan) => plan.id === planId) ?? null;
  }

  upsert(draft: PlanDraft, editingId: number | null): Observable<Plan> {
    const payload = this.toApiRequest(draft);
    const request$ =
      editingId === null
        ? this.http.post<PlanApiDto>(this.endpoint, payload)
        : this.http.put<PlanApiDto>(`${this.endpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(planId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${planId}`).pipe(
      tap(() => {
        this.plans = this.plans.filter((plan) => plan.id !== planId);
      })
    );
  }

  private toApiRequest(draft: PlanDraft): PlanApiRequest {
    return {
      name: draft.name,
      price: draft.price,
      maxDaysPerWeek: draft.maxDaysPerWeek,
      active: draft.isActive
    };
  }

  private upsertInCache(payload: Plan): void {
    const exists = this.plans.some((item) => item.id === payload.id);
    if (!exists) {
      this.plans = [payload, ...this.plans];
      return;
    }

    this.plans = this.plans.map((item) => (item.id === payload.id ? payload : item));
  }

  private mapFromApi(dto: PlanApiDto): Plan {
    return {
      id: Number(dto.id),
      name: dto.name,
      price: Number(dto.price),
      maxDaysPerWeek: Number(dto.maxDaysPerWeek),
      isActive: Boolean(dto.active)
    };
  }

  private mapPageFromApi(dto: PageApiDto<PlanApiDto>): PageResponse<Plan> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }
}
