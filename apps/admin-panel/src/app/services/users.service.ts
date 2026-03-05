import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { User, UserDraft, UserType } from '../class/user.model';
import { API_BASE_URL } from '../utils/api.util';

type UserApiDto = {
  id: number;
  firstName: string;
  lastName: string;
  dni: string;
  address: string;
  phone: string;
  email: string;
  birthDate: string;
  userType: User['userType'];
  planId: number | null;
  registrationDate: string;
  renewalDate: string;
  paymentStatus: User['paymentStatus'];
};

type UserApiRequest = {
  firstName: string;
  lastName: string;
  dni: string;
  address: string;
  phone: string;
  email: string;
  birthDate: string;
  userType: User['userType'];
  planId: number | null;
  registrationDate: string;
  paymentStatus: User['paymentStatus'];
};

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private readonly endpoint = `${API_BASE_URL}/users`;
  private users: User[] = [];

  constructor(private readonly http: HttpClient) {}

  findAll(type: UserType | null = null): Observable<User[]> {
    const url = type === null ? this.endpoint : `${this.endpoint}?type=${type}`;
    return this.http.get<UserApiDto[]>(url).pipe(
      map((items) => items.map((item) => this.mapFromApi(item))),
      tap((items) => {
        if (type === null) {
          this.setAll(items);
          return;
        }
        this.mergeByType(items, type);
      })
    );
  }

  getAll(): User[] {
    return [...this.users];
  }

  setAll(users: User[]): void {
    this.users = users.map((user) => ({ ...user }));
  }

  getByType(userType: UserType): User[] {
    return this.users.filter((user) => user.userType === userType);
  }

  upsert(draft: UserDraft, editingId: number | null): Observable<User> {
    const payload = this.toApiRequest(draft);
    const request$ =
      editingId === null
        ? this.http.post<UserApiDto>(this.endpoint, payload)
        : this.http.put<UserApiDto>(`${this.endpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  renewSubscription(userId: number): Observable<User> {
    return this.http.post<UserApiDto>(`${this.endpoint}/${userId}/renew`, {}).pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  remove(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${userId}`).pipe(
      tap(() => {
        this.users = this.users.filter((user) => user.id !== userId);
      })
    );
  }

  clearPlanReferences(planId: number): void {
    this.users = this.users.map((user) =>
      user.planId === planId
        ? {
            ...user,
            planId: null
          }
        : user
    );
  }

  getById(userId: number): User | null {
    const user = this.users.find((item) => item.id === userId);
    return user ? { ...user } : null;
  }

  fullName(userId: number | null): string {
    if (userId === null) {
      return 'Sin usuario';
    }

    const user = this.users.find((item) => item.id === userId);
    return user ? `${user.firstName} ${user.lastName}` : 'Sin usuario';
  }

  private mergeByType(items: User[], userType: UserType): void {
    const others = this.users.filter((user) => user.userType !== userType);
    this.users = [...others, ...items];
  }

  private upsertInCache(payload: User): void {
    const exists = this.users.some((item) => item.id === payload.id);
    if (!exists) {
      this.users = [payload, ...this.users];
      return;
    }

    this.users = this.users.map((item) => (item.id === payload.id ? payload : item));
  }

  private toApiRequest(draft: UserDraft): UserApiRequest {
    return {
      firstName: draft.firstName,
      lastName: draft.lastName,
      dni: draft.dni,
      address: draft.address,
      phone: draft.phone,
      email: draft.email,
      birthDate: draft.birthDate,
      userType: draft.userType,
      planId: draft.planId,
      registrationDate: draft.registrationDate,
      paymentStatus: draft.paymentStatus
    };
  }

  private mapFromApi(dto: UserApiDto): User {
    return {
      id: Number(dto.id),
      firstName: dto.firstName,
      lastName: dto.lastName,
      dni: dto.dni,
      address: dto.address,
      phone: dto.phone,
      email: dto.email,
      birthDate: dto.birthDate,
      userType: dto.userType,
      planId: dto.planId === null ? null : Number(dto.planId),
      registrationDate: dto.registrationDate,
      renewalDate: dto.renewalDate,
      paymentStatus: dto.paymentStatus
    };
  }
}
