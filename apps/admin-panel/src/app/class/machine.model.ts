export interface Machine {
  id: number;
  name: string;
  quantity: number;
  photoUrl: string;
}

export type MachineDraft = Omit<Machine, 'id'>;

export const createEmptyMachineDraft = (): MachineDraft => ({
  name: '',
  quantity: 1,
  photoUrl: ''
});
