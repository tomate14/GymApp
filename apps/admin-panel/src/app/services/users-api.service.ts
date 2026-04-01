import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserDraft } from '../class/user.model';
import { API_BASE_URL } from '../utils/api.util';

type UserApiRequest = {
  firstName: string;
  lastName: string;
  dni: string;
  address: string;
  phone: string;
  email: string;
  birthDate: string;
  userType: User['userType'];
  roleId: number | null;
  planId: number | null;
  activityIds: number[];
  registrationDate: string;
  paymentStatus: User['paymentStatus'];
};

@Injectable({
  providedIn: 'root'
})
export class UsersApiService {
  private readonly endpoint = `${API_BASE_URL}/users`;

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<User[]> {
    return this.http.get<User[]>(this.endpoint);
  }

  create(draft: UserDraft): Observable<User> {
    return this.http.post<User>(this.endpoint, this.toRequestPayload(draft));
  }

  update(id: number, draft: UserDraft): Observable<User> {
    return this.http.put<User>(`${this.endpoint}/${id}`, this.toRequestPayload(draft));
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }

  renewQuota(id: number): Observable<User> {
    return this.http.post<User>(`${this.endpoint}/${id}/renew`, {});
  }

  private toRequestPayload(draft: UserDraft): UserApiRequest {
    return {
      firstName: draft.firstName,
      lastName: draft.lastName,
      dni: draft.dni,
      address: draft.address,
      phone: draft.phone,
      email: draft.email,
      birthDate: draft.birthDate,
      userType: draft.userType,
      roleId: draft.roleId,
      planId: draft.planId,
      activityIds: draft.activityIds,
      registrationDate: draft.registrationDate,
      paymentStatus: draft.paymentStatus
    };
  }
}
