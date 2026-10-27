export type BillingFrequencyType = 'DAYS' | 'MONTHS' | 'YEARS';

export type ClientSubscriptionStatus =
  | 'NONE'
  | 'PENDING'
  | 'ACTIVE'
  | 'PAUSED'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'PAYMENT_REQUIRED';

export type BillingPlan = {
  id: number;
  code: string;
  name: string;
  description: string;
  price: number;
  currencyId: string;
  frequency: number;
  frequencyType: BillingFrequencyType;
};

export type ClientSubscription = {
  clientId: number;
  clientName: string;
  status: ClientSubscriptionStatus;
  active: boolean;
  billingPlanId: number | null;
  billingPlanName: string | null;
  price: number | null;
  currencyId: string | null;
  nextPaymentAt: string | null;
  currentPeriodStart: string | null;
  currentPeriodEnd: string | null;
  activatedAt: string | null;
  cancelledAt: string | null;
  checkoutUrl: string | null;
};

export type BillingCheckoutResponse = {
  clientId: number;
  billingPlanId: number;
  status: ClientSubscriptionStatus;
  checkoutUrl: string | null;
  mercadoPagoPreapprovalId: string | null;
};
