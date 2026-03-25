import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DashboardActivityRankingRow,
  DashboardChart,
  DashboardClassRow,
  DashboardPageResponse,
  DashboardPlanOverloadRow,
  DashboardRenewalRow,
  DashboardSummary
} from '../class/dashboard.model';
import { API_BASE_URL } from '../utils/api.util';

type SummaryFilters = {
  fromDate?: string;
  toDate?: string;
  studentsTrendMode?: 'month' | 'year';
};

type RenewalsFilters = {
  search?: string;
  planId?: number;
  paymentStatus?: string;
  fromDate?: string;
  toDate?: string;
  renewalFrom?: string;
  renewalTo?: string;
  page: number;
  size: number;
};

type ClassFilters = {
  search?: string;
  activityId?: number;
  teacherId?: number;
  fromDate?: string;
  toDate?: string;
  page: number;
  size: number;
};

type RankingFilters = {
  search?: string;
  fromDate?: string;
  toDate?: string;
  page: number;
  size: number;
};

type OverloadFilters = {
  search?: string;
  fromDate?: string;
  toDate?: string;
  page: number;
  size: number;
};

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly endpoint = `${API_BASE_URL}/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getSummary(filters: SummaryFilters = {}): Observable<DashboardSummary> {
    let params = new HttpParams();
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    params = this.appendIfPresent(params, 'studentsTrendMode', filters.studentsTrendMode);
    return this.http.get<DashboardSummary>(`${this.endpoint}/summary`, { params });
  }

  getStudentsTrendChart(filters: SummaryFilters = {}): Observable<DashboardChart> {
    let params = new HttpParams();
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    params = this.appendIfPresent(params, 'studentsTrendMode', filters.studentsTrendMode);
    return this.http.get<DashboardChart>(`${this.endpoint}/students-trend`, { params });
  }

  getRenewals(filters: RenewalsFilters): Observable<DashboardPageResponse<DashboardRenewalRow>> {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    params = this.appendIfPresent(params, 'search', filters.search);
    params = this.appendIfPresent(params, 'planId', filters.planId);
    params = this.appendIfPresent(params, 'paymentStatus', filters.paymentStatus);
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    params = this.appendIfPresent(params, 'renewalFrom', filters.renewalFrom);
    params = this.appendIfPresent(params, 'renewalTo', filters.renewalTo);
    return this.http.get<DashboardPageResponse<DashboardRenewalRow>>(`${this.endpoint}/renewals`, { params });
  }

  getClasses(filters: ClassFilters): Observable<DashboardPageResponse<DashboardClassRow>> {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    params = this.appendIfPresent(params, 'search', filters.search);
    params = this.appendIfPresent(params, 'activityId', filters.activityId);
    params = this.appendIfPresent(params, 'teacherId', filters.teacherId);
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    return this.http.get<DashboardPageResponse<DashboardClassRow>>(`${this.endpoint}/classes`, { params });
  }

  getActivityRanking(filters: RankingFilters): Observable<DashboardPageResponse<DashboardActivityRankingRow>> {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    params = this.appendIfPresent(params, 'search', filters.search);
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    return this.http.get<DashboardPageResponse<DashboardActivityRankingRow>>(`${this.endpoint}/activity-ranking`, { params });
  }

  getPlanOverloads(filters: OverloadFilters): Observable<DashboardPageResponse<DashboardPlanOverloadRow>> {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    params = this.appendIfPresent(params, 'search', filters.search);
    params = this.appendIfPresent(params, 'fromDate', filters.fromDate);
    params = this.appendIfPresent(params, 'toDate', filters.toDate);
    return this.http.get<DashboardPageResponse<DashboardPlanOverloadRow>>(`${this.endpoint}/plan-overloads`, { params });
  }

  private appendIfPresent(params: HttpParams, key: string, value: string | number | undefined): HttpParams {
    if (value === undefined || value === '' || value === 0) {
      return params;
    }
    return params.set(key, value);
  }
}
