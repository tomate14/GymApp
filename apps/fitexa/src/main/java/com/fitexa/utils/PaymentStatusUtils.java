package com.fitexa.utils;

import com.fitexa.entities.PaymentStatus;
import java.time.LocalDate;

public final class PaymentStatusUtils {
    private PaymentStatusUtils() {}

    public static PaymentStatus fromRenewalDate(LocalDate renewalDate) {
        return fromRenewalDate(renewalDate, LocalDate.now());
    }

    public static PaymentStatus fromRenewalDate(LocalDate renewalDate, LocalDate referenceDate) {
        if (renewalDate != null && !renewalDate.isBefore(referenceDate)) {
            return PaymentStatus.AL_DIA;
        }
        return PaymentStatus.ADEUDA;
    }
}
