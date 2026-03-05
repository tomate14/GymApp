import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Machine, MachineDraft } from '../class/machine.model';
import { API_BASE_URL } from '../utils/api.util';

type MachineApiDto = {
  id: number;
  name: string;
  photoUrl: string;
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

  getAll(): Machine[] {
    return [...this.machines];
  }

  setAll(machines: Machine[]): void {
    this.machines = machines.map((item) => ({ ...item }));
  }

  upsert(draft: MachineDraft, editingId: number | null): Observable<Machine> {
    const request$ =
      editingId === null
        ? this.http.post<MachineApiDto>(this.endpoint, draft)
        : this.http.put<MachineApiDto>(`${this.endpoint}/${editingId}`, draft);

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
      photoUrl: dto.photoUrl
    };
  }
}
