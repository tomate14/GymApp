export interface Machine {
  id: number;
  name: string;
  photoUrl: string;
}

export type MachineDraft = Omit<Machine, 'id'>;

export const createEmptyMachineDraft = (): MachineDraft => ({
  name: '',
  photoUrl: ''
});

