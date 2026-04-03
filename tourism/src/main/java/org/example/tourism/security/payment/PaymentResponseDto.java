package org.example.tourism.security.payment;

import lombok.Data;
import org.example.tourism.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private BigDecimal refundedAmount;
}