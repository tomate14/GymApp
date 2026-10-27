import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BillingCheckoutResponse, BillingPlan, ClientSubscription } from '../class/billing.model';
import { API_BASE_URL } from '../utils/api.util';

@Injectable({
  providedIn: 'root'
})
export class BillingService {
  private readonly endpoint = `${API_BASE_URL}/billing`;

  constructor(private readonly http: HttpClient) {}

  findPlans(): Observable<BillingPlan[]> {
    return this.http.get<BillingPlan[]>(`${this.endpoint}/plans`);
  }

  getSubscription(): Observable<ClientSubscription> {
    return this.http.get<ClientSubscription>(`${this.endpoint}/subscription`);
  }

  createCheckout(billingPlanId: number, payerEmail?: string | null): Observable<BillingCheckoutResponse> {
    return this.http.post<BillingCheckoutResponse>(`${this.endpoint}/subscription/checkout`, {
      billingPlanId,
      payerEmail: payerEmail?.trim() || null
    });
  }
}
