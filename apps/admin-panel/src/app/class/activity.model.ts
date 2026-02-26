export interface Activity {
  id: number;
  name: string;
  description: string;
}

export type ActivityDraft = Omit<Activity, 'id'>;

export const createEmptyActivityDraft = (): ActivityDraft => ({
  name: '',
  description: ''
});

