package org.example.tourism.security.payment;

import org.example.tourism.payment.PaymentRequestDto;
import org.example.tourism.payment.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    org.example.tourism.payment.PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto);
    org.example.tourism.payment.PaymentResponseDto getPaymentByBooking(Long bookingId);
    org.example.tourism.payment.PaymentResponseDto getPaymentById(Long paymentId);
    org.example.tourism.payment.PaymentResponseDto refundPayment(Long paymentId);
    boolean isPaymentOwner(Long paymentId, Long userId);
    List<PaymentResponseDto> getUserPayments(Long userId);
    boolean validateUserOwnsBooking(Long bookingId, Long userId);
}