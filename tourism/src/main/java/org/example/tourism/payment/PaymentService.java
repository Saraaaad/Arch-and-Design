package org.example.tourism.payment;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto);
    PaymentResponseDto getPaymentByBooking(Long bookingId);
    PaymentResponseDto getPaymentById(Long paymentId);
    PaymentResponseDto refundPayment(Long paymentId);
    boolean isPaymentOwner(Long paymentId, Long userId);
    List<PaymentResponseDto> getUserPayments(Long userId);
    boolean validateUserOwnsBooking(Long bookingId, Long userId);
}