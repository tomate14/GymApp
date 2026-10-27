package com.fitexa.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MercadoPagoAuthorizedPayment(
    String id,
    String preapprovalId,
    String paymentId,
    String invoiceStatus,
    String paymentStatus,
    String paymentStatusDetail,
    BigDecimal amount,
    String currencyId,
    LocalDateTime debitDate
) {
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(paymentStatus);
    }
}
