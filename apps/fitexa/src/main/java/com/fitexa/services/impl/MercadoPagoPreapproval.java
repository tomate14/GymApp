package com.fitexa.services.impl;

import java.time.LocalDateTime;

public record MercadoPagoPreapproval(
    String id,
    String status,
    String initPoint,
    String preapprovalPlanId,
    String payerEmail,
    LocalDateTime nextPaymentAt
) {}
