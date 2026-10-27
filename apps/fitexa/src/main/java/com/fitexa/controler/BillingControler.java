package com.fitexa.controler;

import com.fitexa.dtos.billing.BillingCheckoutRequestDto;
import com.fitexa.dtos.billing.BillingCheckoutResponseDto;
import com.fitexa.dtos.billing.BillingPlanResponseDto;
import com.fitexa.dtos.billing.ClientSubscriptionResponseDto;
import com.fitexa.services.BillingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Suscripcion SaaS de tenants.")
public class BillingControler {

    private final BillingService billingService;

    @GetMapping("/plans")
    public List<BillingPlanResponseDto> findActivePlans() {
        return billingService.findActivePlans();
    }

    @GetMapping("/subscription")
    public ClientSubscriptionResponseDto getCurrentSubscription() {
        return billingService.getCurrentSubscription();
    }

    @PostMapping("/subscription/checkout")
    public BillingCheckoutResponseDto createCheckout(@Valid @RequestBody BillingCheckoutRequestDto request) {
        return billingService.createCheckout(request);
    }

    @PostMapping("/webhooks/mercado-pago")
    public void mercadoPagoWebhook(
        @RequestBody(required = false) String payload,
        @RequestHeader Map<String, String> headers,
        @RequestParam Map<String, String> queryParams
    ) {
        billingService.handleMercadoPagoWebhook(payload, headers, queryParams);
    }
}
