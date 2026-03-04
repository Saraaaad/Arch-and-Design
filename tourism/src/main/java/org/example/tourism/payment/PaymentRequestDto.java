package org.example.tourism.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}