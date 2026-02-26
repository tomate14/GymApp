export interface Plan {
  id: number;
  name: string;
  price: number;
  maxDaysPerWeek: number;
  isActive: boolean;
}

export type PlanDraft = Omit<Plan, 'id'>;

export const createEmptyPlanDraft = (): PlanDraft => ({
  name: '',
  price: 0,
  maxDaysPerWeek: 2,
  isActive: true
});
