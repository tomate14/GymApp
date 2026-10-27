package com.fitexa.services;

import com.fitexa.entities.BillingPlanEntity;
import com.fitexa.entities.MercadoPagoAccountEntity;
import com.fitexa.services.impl.MercadoPagoAuthorizedPayment;
import com.fitexa.services.impl.MercadoPagoPreapproval;
import java.util.List;

public interface MercadoPagoBillingClient {
    String createPreapprovalPlan(MercadoPagoAccountEntity account, BillingPlanEntity billingPlan);
    MercadoPagoPreapproval createPreapproval(MercadoPagoAccountEntity account, BillingPlanEntity billingPlan, String payerEmail, Long clientId);
    MercadoPagoPreapproval getPreapproval(MercadoPagoAccountEntity account, String preapprovalId);
    MercadoPagoAuthorizedPayment getAuthorizedPayment(MercadoPagoAccountEntity account, String authorizedPaymentId);
    List<MercadoPagoAuthorizedPayment> searchAuthorizedPayments(MercadoPagoAccountEntity account, String preapprovalId);
}
