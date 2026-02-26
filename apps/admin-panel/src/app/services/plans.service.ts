import { Injectable } from '@angular/core';
import { Plan, PlanDraft } from '../class/plan.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class PlansService {
  private plans: Plan[] = [
    { id: 1, name: '2 veces por semana', price: 18000, maxDaysPerWeek: 2, isActive: true },
    { id: 2, name: '3 veces por semana', price: 22000, maxDaysPerWeek: 3, isActive: true },
    { id: 3, name: 'Libre', price: 28000, maxDaysPerWeek: 7, isActive: true }
  ];

  getAll(): Plan[] {
    return [...this.plans];
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

  upsert(draft: PlanDraft, editingId: number | null): Plan {
    const payload: Plan = {
      id: editingId ?? nextNumericId(this.plans),
      ...draft
    };

    if (editingId === null) {
      this.plans = [payload, ...this.plans];
      return payload;
    }

    this.plans = this.plans.map((plan) => (plan.id === editingId ? payload : plan));
    return payload;
  }

  remove(planId: number): void {
    this.plans = this.plans.filter((plan) => plan.id !== planId);
  }
}
