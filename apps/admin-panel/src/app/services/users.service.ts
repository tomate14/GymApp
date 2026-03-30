import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import { UserPaymentMovement } from '../class/user-payment-movement.model';
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
  roleId: number | null;
  roleName: string | null;
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
  password: string | null;
  birthDate: string;
  userType: User['userType'];
  roleId: number | null;
  planId: number | null;
  registrationDate: string;
  paymentStatus: User['paymentStatus'];
};

type UserPaymentMovementApiDto = {
  id: number;
  userId: number;
  studentName: string;
  planId: number | null;
  planName: string;
  amount: number;
  detail: string;
  paidAt: string;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type UserPageFilters = {
  name?: string;
  dni?: string;
  userType?: UserType | 'all';
  roleId?: number | null;
  paymentStatus?: User['paymentStatus'] | 'all';
  registrationFrom?: string;
  registrationTo?: string;
  renewalFrom?: string;
  renewalTo?: string;
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

  findPage(filters: UserPageFilters, page: number, size: number): Observable<PageResponse<User>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.userType && filters.userType !== 'all') {
      params = params.set('type', filters.userType);
    }
    if (filters.roleId !== undefined && filters.roleId !== null) {
      params = params.set('roleId', String(filters.roleId));
    }
    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }
    if (filters.dni && filters.dni.trim() !== '') {
      params = params.set('dni', filters.dni.trim());
    }
    if (filters.paymentStatus && filters.paymentStatus !== 'all') {
      params = params.set('paymentStatus', filters.paymentStatus);
    }
    if (filters.registrationFrom && filters.registrationFrom !== '') {
      params = params.set('registrationFrom', filters.registrationFrom);
    }
    if (filters.registrationTo && filters.registrationTo !== '') {
      params = params.set('registrationTo', filters.registrationTo);
    }
    if (filters.renewalFrom && filters.renewalFrom !== '') {
      params = params.set('renewalFrom', filters.renewalFrom);
    }
    if (filters.renewalTo && filters.renewalTo !== '') {
      params = params.set('renewalTo', filters.renewalTo);
    }

    return this.http.get<PageApiDto<UserApiDto>>(`${this.endpoint}/page`, { params }).pipe(
      map((response) => this.mapPageFromApi(response))
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

  assignRole(userId: number, roleId: number | null): Observable<User> {
    return this.http.put<UserApiDto>(`${this.endpoint}/${userId}/role`, { roleId }).pipe(
      map((item) => this.mapFromApi(item)),
      tap((saved) => this.upsertInCache(saved))
    );
  }

  findPayments(userId: number): Observable<UserPaymentMovement[]> {
    return this.http.get<UserPaymentMovementApiDto[]>(`${this.endpoint}/${userId}/payments`).pipe(
      map((items) => items.map((item) => this.mapPaymentFromApi(item)))
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
      password: draft.password.trim() === '' ? null : draft.password.trim(),
      birthDate: draft.birthDate,
      userType: draft.userType,
      roleId: draft.roleId,
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
      roleId: dto.roleId === null ? null : Number(dto.roleId),
      roleName: dto.roleName,
      planId: dto.planId === null ? null : Number(dto.planId),
      registrationDate: dto.registrationDate,
      renewalDate: dto.renewalDate,
      paymentStatus: dto.paymentStatus
    };
  }

  private mapPaymentFromApi(dto: UserPaymentMovementApiDto): UserPaymentMovement {
    return {
      id: Number(dto.id),
      userId: Number(dto.userId),
      studentName: dto.studentName,
      planId: dto.planId === null ? null : Number(dto.planId),
      planName: dto.planName,
      amount: Number(dto.amount),
      detail: dto.detail,
      paidAt: dto.paidAt
    };
  }

  private mapPageFromApi(dto: PageApiDto<UserApiDto>): PageResponse<User> {
    return {
      content: dto.content.map((item) => this.mapFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }
}
