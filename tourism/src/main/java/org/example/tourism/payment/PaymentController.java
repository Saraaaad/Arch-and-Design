package org.example.tourism.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Endpoints for payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Process a payment for a booking")
    public PaymentResponseDto processPayment(@Valid @RequestBody PaymentRequestDto paymentRequestDto) {
        return paymentService.processPayment(paymentRequestDto);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment details for a booking")
    public PaymentResponseDto getPaymentByBooking(@PathVariable Long bookingId) {
        return paymentService.getPaymentByBooking(bookingId);
    }
}