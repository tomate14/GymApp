import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BillingPlan, ClientSubscription, ClientSubscriptionStatus } from '../../class/billing.model';
import { BillingService } from '../../services/billing.service';

@Component({
  selector: 'app-billing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './billing.component.html',
  styleUrl: './billing.component.css'
})
export class BillingComponent implements OnInit {
  plans: BillingPlan[] = [];
  subscription: ClientSubscription | null = null;
  selectedPlanId: number | null = null;
  payerEmail = '';
  isLoading = true;
  isStartingCheckout = false;
  apiError: string | null = null;

  constructor(private readonly billingService: BillingService) {}

  ngOnInit(): void {
    this.loadBilling();
  }

  get statusLabel(): string {
    const status = this.subscription?.status ?? 'NONE';
    const labels: Record<ClientSubscriptionStatus, string> = {
      NONE: 'Sin suscripcion',
      PENDING: 'Pendiente',
      ACTIVE: 'Activa',
      PAUSED: 'Pausada',
      CANCELLED: 'Cancelada',
      EXPIRED: 'Vencida',
      PAYMENT_REQUIRED: 'Pago requerido'
    };
    return labels[status];
  }

  get isActive(): boolean {
    return this.subscription?.active === true;
  }

  selectPlan(planId: number): void {
    this.selectedPlanId = planId;
  }

  startCheckout(): void {
    if (this.selectedPlanId === null || this.isStartingCheckout) {
      return;
    }

    this.apiError = null;
    this.isStartingCheckout = true;
    this.billingService.createCheckout(this.selectedPlanId, this.payerEmail).subscribe({
      next: (response) => {
        if (response.checkoutUrl) {
          window.location.href = response.checkoutUrl;
          return;
        }
        this.apiError = 'Mercado Pago no devolvio una URL de autorizacion.';
        this.isStartingCheckout = false;
      },
      error: (err) => {
        this.apiError = err.error?.message ?? 'No se pudo iniciar la suscripcion.';
        this.isStartingCheckout = false;
      }
    });
  }

  formatFrequency(plan: BillingPlan): string {
    const unit = plan.frequencyType === 'MONTHS' ? 'mes' : plan.frequencyType === 'YEARS' ? 'ano' : 'dia';
    return plan.frequency === 1 ? `Cada ${unit}` : `Cada ${plan.frequency} ${unit}s`;
  }

  private loadBilling(): void {
    this.isLoading = true;
    this.apiError = null;
    forkJoin({
      plans: this.billingService.findPlans(),
      subscription: this.billingService.getSubscription()
    }).subscribe({
      next: ({ plans, subscription }) => {
        this.plans = plans;
        this.subscription = subscription;
        this.selectedPlanId = subscription.billingPlanId ?? plans[0]?.id ?? null;
        this.payerEmail = '';
        this.isLoading = false;
      },
      error: (err) => {
        this.apiError = err.error?.message ?? 'No se pudo cargar la facturacion.';
        this.isLoading = false;
      }
    });
  }
}
