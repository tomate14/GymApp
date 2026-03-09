package com.fitexa.services;

import com.fitexa.entities.UserPaymentMovementEntity;

public interface PaymentReceiptPdfService {
    byte[] generateReceipt(UserPaymentMovementEntity movement);
}
