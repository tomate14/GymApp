package com.fitexa.dtos.billing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BillingCheckoutRequestDto {
    @NotNull(message = "El plan de facturacion es obligatorio.")
    private Long billingPlanId;

    @Size(max = 220, message = "El email del pagador no puede superar 220 caracteres.")
    private String payerEmail;
}
