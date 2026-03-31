import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import { Machine, MachineDraft } from '../class/machine.model';
import { API_BASE_URL, resolveApiAssetUrl } from '../utils/api.util';

type MachineApiDto = {
  id: number;
  name: string;
  quantity: number;
  photoUrl: string;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type MachinePageFilters = {
  name?: string;
  onlyInUse?: boolean;
};

@Injectable({
  providedIn: 'root'
})
export class MachinesService {
  private readonly endpoint = `${API_BASE_URL}/machines`;
  private machines: Machine[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<Machine[]> {
    return this.http.get<MachineApiDto[]>(this.endpoint).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => this.setAll(items))
    );
  }

  findPage(filters: MachinePageFilters, page: number, size: number): Observable<PageResponse<Machine>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size))
      .set('onlyInUse', String(Boolean(filters.onlyInUse)));

    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }

    return this.http.get<PageApiDto<MachineApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
    );
  }

  getAll(): Machine[] {
    return [...this.machines];
  }

  setAll(machines: Machine[]): void {
    this.machines = machines.map((item) => ({ ...item }));
  }

  upsert(draft: MachineDraft, photo: File | null, editingId: number | null): Observable<Machine> {
    const payload = new FormData();
    payload.append('name', draft.name);
    payload.append('quantity', String(draft.quantity));
    if (photo) {
      payload.append('photo', photo);
    }

    const request$ =
      editingId === null
        ? this.http.post<MachineApiDto>(this.endpoint, payload)
        : this.http.put<MachineApiDto>(`${this.endpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(machineId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${machineId}`).pipe(
      tap(() => {
        this.machines = this.machines.filter((machine) => machine.id !== machineId);
      })
    );
  }

  nameById(machineId: number | null): string {
    if (machineId === null) {
      return 'Sin maquina';
    }

    return this.machines.find((machine) => machine.id === machineId)?.name ?? 'Sin maquina';
  }

  private upsertInCache(payload: Machine): void {
    const exists = this.machines.some((item) => item.id === payload.id);
    if (!exists) {
      this.machines = [payload, ...this.machines];
      return;
    }

    this.machines = this.machines.map((item) => (item.id === payload.id ? payload : item));
  }

  private mapFromApi(dto: MachineApiDto): Machine {
    return {
      id: Number(dto.id),
      name: dto.name,
      quantity: Number(dto.quantity),
      photoUrl: resolveApiAssetUrl(dto.photoUrl)
    };
  }

  private mapPageFromApi(dto: PageApiDto<MachineApiDto>): PageResponse<Machine> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }
}
