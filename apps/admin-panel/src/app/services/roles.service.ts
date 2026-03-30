import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Role, RoleDraft, RoleMenuItem } from '../class/role.model';
import { API_BASE_URL } from '../utils/api.util';

type RoleApiDto = {
  id: number;
  name: string;
  description: string;
  systemRole: boolean;
  assignedUsersCount: number;
  menuItems: RoleMenuItem['key'][];
};

type RoleMenuItemApiDto = {
  key: RoleMenuItem['key'];
  label: string;
  path: string;
};

@Injectable({
  providedIn: 'root'
})
export class RolesService {
  private readonly endpoint = `${API_BASE_URL}/roles`;
  private roles: Role[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<Role[]> {
    return this.http.get<RoleApiDto[]>(this.endpoint).pipe(
      map((items) => items.map((item) => this.mapRole(item))),
      map((items) => {
        this.roles = items;
        return items;
      })
    );
  }

  findMenuItems(): Observable<RoleMenuItem[]> {
    return this.http.get<RoleMenuItemApiDto[]>(`${this.endpoint}/menu-items`).pipe(
      map((items) => items.map((item) => ({
        key: item.key,
        label: item.label,
        path: item.path
      } satisfies RoleMenuItem)))
    );
  }

  create(draft: RoleDraft): Observable<Role> {
    return this.http.post<RoleApiDto>(this.endpoint, draft).pipe(
      map((item) => this.mapRole(item))
    );
  }

  update(id: number, draft: RoleDraft): Observable<Role> {
    return this.http.put<RoleApiDto>(`${this.endpoint}/${id}`, draft).pipe(
      map((item) => this.mapRole(item))
    );
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }

  getCached(): Role[] {
    return [...this.roles];
  }

  private mapRole(dto: RoleApiDto): Role {
    return {
      id: Number(dto.id),
      name: dto.name,
      description: dto.description,
      systemRole: dto.systemRole,
      assignedUsersCount: Number(dto.assignedUsersCount),
      menuItems: [...dto.menuItems]
    };
  }
}
