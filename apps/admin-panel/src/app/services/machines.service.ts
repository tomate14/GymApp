import { Injectable } from '@angular/core';
import { Machine, MachineDraft } from '../class/machine.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class MachinesService {
  private machines: Machine[] = [
    {
      id: 1,
      name: 'Smith Machine',
      photoUrl: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=900&q=80'
    },
    {
      id: 2,
      name: 'Remo bajo',
      photoUrl: 'https://images.unsplash.com/photo-1584466977773-e625c37cdd50?auto=format&fit=crop&w=900&q=80'
    },
    {
      id: 3,
      name: 'Prensa 45',
      photoUrl: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=80'
    },
    {
      id: 4,
      name: 'Polea alta',
      photoUrl: 'https://images.unsplash.com/photo-1571019613576-2b22c76fd955?auto=format&fit=crop&w=900&q=80'
    }
  ];

  getAll(): Machine[] {
    return [...this.machines];
  }

  upsert(draft: MachineDraft, editingId: number | null): Machine {
    const payload: Machine = {
      id: editingId ?? nextNumericId(this.machines),
      ...draft
    };

    if (editingId === null) {
      this.machines = [payload, ...this.machines];
      return payload;
    }

    this.machines = this.machines.map((machine) => (machine.id === editingId ? payload : machine));
    return payload;
  }

  remove(machineId: number): void {
    this.machines = this.machines.filter((machine) => machine.id !== machineId);
  }

  nameById(machineId: number | null): string {
    if (machineId === null) {
      return 'Sin maquina';
    }

    return this.machines.find((machine) => machine.id === machineId)?.name ?? 'Sin maquina';
  }
}
