import { AdminMenuKey } from './role.model';

export interface AdminMenuItem {
  key: AdminMenuKey;
  label: string;
  path: string;
  shortLabel: string;
}
