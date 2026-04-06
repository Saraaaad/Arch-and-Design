package org.example.tourism.common;

public class PaymentAlreadyRefundedException extends RuntimeException {

    public PaymentAlreadyRefundedException(String message) {
        super(message);
    }
}