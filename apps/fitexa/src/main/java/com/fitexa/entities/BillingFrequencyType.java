package com.fitexa.entities;

public enum BillingFrequencyType {
    DAYS("days"),
    MONTHS("months"),
    YEARS("years");

    private final String mercadoPagoValue;

    BillingFrequencyType(String mercadoPagoValue) {
        this.mercadoPagoValue = mercadoPagoValue;
    }

    public String getMercadoPagoValue() {
        return mercadoPagoValue;
    }
}
