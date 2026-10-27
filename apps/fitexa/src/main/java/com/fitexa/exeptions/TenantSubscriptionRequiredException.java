package com.fitexa.exeptions;

public class TenantSubscriptionRequiredException extends RuntimeException {
    public TenantSubscriptionRequiredException(String message) {
        super(message);
    }
}
