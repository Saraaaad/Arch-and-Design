package org.example.tourism.payment;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto);
    PaymentResponseDto getPaymentByBooking(Long bookingId);
}