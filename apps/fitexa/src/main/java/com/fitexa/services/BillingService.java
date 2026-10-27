package com.fitexa.services;

import com.fitexa.dtos.billing.BillingCheckoutRequestDto;
import com.fitexa.dtos.billing.BillingCheckoutResponseDto;
import com.fitexa.dtos.billing.BillingPlanResponseDto;
import com.fitexa.dtos.billing.ClientSubscriptionResponseDto;
import java.util.List;
import java.util.Map;

public interface BillingService {
    List<BillingPlanResponseDto> findActivePlans();
    ClientSubscriptionResponseDto getCurrentSubscription();
    BillingCheckoutResponseDto createCheckout(BillingCheckoutRequestDto request);
    void handleMercadoPagoWebhook(String payload, Map<String, String> headers, Map<String, String> queryParams);
}
