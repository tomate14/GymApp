package com.fitexa.dtos.billing;

import com.fitexa.entities.ClientSubscriptionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingCheckoutResponseDto {
    private Long clientId;
    private Long billingPlanId;
    private ClientSubscriptionStatus status;
    private String checkoutUrl;
    private String mercadoPagoPreapprovalId;
}
