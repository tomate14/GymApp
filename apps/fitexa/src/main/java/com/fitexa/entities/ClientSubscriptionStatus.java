package com.fitexa.entities;

import java.util.Set;

public enum ClientSubscriptionStatus {
    NONE,
    PENDING,
    ACTIVE,
    PAUSED,
    CANCELLED,
    EXPIRED,
    PAYMENT_REQUIRED;

    private static final Set<String> ACTIVE_MERCADO_PAGO_STATUSES = Set.of("authorized", "active");
    private static final Set<String> PENDING_MERCADO_PAGO_STATUSES = Set.of("pending", "in_process");
    private static final Set<String> PAUSED_MERCADO_PAGO_STATUSES = Set.of("paused");
    private static final Set<String> CANCELLED_MERCADO_PAGO_STATUSES = Set.of("cancelled", "canceled");

    public static ClientSubscriptionStatus fromMercadoPagoStatus(String value) {
        if (value == null || value.isBlank()) {
            return PAYMENT_REQUIRED;
        }
        String normalized = value.trim().toLowerCase();
        if (ACTIVE_MERCADO_PAGO_STATUSES.contains(normalized)) {
            return ACTIVE;
        }
        if (PENDING_MERCADO_PAGO_STATUSES.contains(normalized)) {
            return PENDING;
        }
        if (PAUSED_MERCADO_PAGO_STATUSES.contains(normalized)) {
            return PAUSED;
        }
        if (CANCELLED_MERCADO_PAGO_STATUSES.contains(normalized)) {
            return CANCELLED;
        }
        return PAYMENT_REQUIRED;
    }
}
