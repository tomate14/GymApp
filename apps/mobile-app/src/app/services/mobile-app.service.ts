import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  MobileGymClass,
  MobilePaymentMovement,
  MobilePersonalRecordDetail,
  MobilePersonalRecord,
  MobilePersonalRecordRequest,
  MobileProfile,
  MobileProfileUpdateRequest,
  MobileRoutine
} from '../models/mobile.model';
import { API_BASE_URL } from '../utils/api.util';

@Injectable({
  providedIn: 'root'
})
export class MobileAppService {
  private readonly endpoint = `${API_BASE_URL}/mobile`;

  constructor(private readonly http: HttpClient) {}

  getProfile(): Observable<MobileProfile> {
    return this.http.get<MobileProfile>(`${this.endpoint}/me`);
  }

  updateProfile(payload: MobileProfileUpdateRequest): Observable<MobileProfile> {
    return this.http.put<MobileProfile>(`${this.endpoint}/me`, payload);
  }

  getRoutines(): Observable<MobileRoutine[]> {
    return this.http.get<MobileRoutine[]>(`${this.endpoint}/routines`);
  }

  getPaymentMovements(): Observable<MobilePaymentMovement[]> {
    return this.http.get<MobilePaymentMovement[]>(`${this.endpoint}/payments`);
  }

  downloadPaymentMovementPdf(movementId: number): Observable<Blob> {
    return this.http.get(`${this.endpoint}/payments/${movementId}/pdf`, { responseType: 'blob' });
  }

  getPersonalRecords(): Observable<MobilePersonalRecord[]> {
    return this.http.get<MobilePersonalRecord[]>(`${this.endpoint}/personal-records`);
  }

  getPersonalRecordDetail(exerciseId: number): Observable<MobilePersonalRecordDetail> {
    return this.http.get<MobilePersonalRecordDetail>(`${this.endpoint}/personal-records/${exerciseId}`);
  }

  savePersonalRecord(exerciseId: number, payload: MobilePersonalRecordRequest): Observable<MobilePersonalRecord> {
    return this.http.put<MobilePersonalRecord>(`${this.endpoint}/personal-records/${exerciseId}`, payload);
  }

  getClasses(date: string): Observable<MobileGymClass[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<MobileGymClass[]>(`${this.endpoint}/classes`, { params });
  }

  subscribeToClass(classId: number, date: string): Observable<MobileGymClass> {
    const params = new HttpParams().set('date', date);
    return this.http.post<MobileGymClass>(`${this.endpoint}/classes/${classId}/subscribe`, {}, { params });
  }
}
