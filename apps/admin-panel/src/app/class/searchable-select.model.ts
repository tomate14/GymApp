export type SearchableFieldType = 'text' | 'number';

export interface SearchableFieldConfig {
  field: string;
  type: SearchableFieldType;
}

export type SearchableSelectValue = string | number | null;

