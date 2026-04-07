package org.example.tourism.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'GUEST')")
    @Operation(summary = "Process a payment for a booking",
            description = "Any authenticated user can pay for their booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking or payment already completed"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public PaymentResponseDto processPayment(
            @Valid @RequestBody PaymentRequestDto paymentRequestDto,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");


        return paymentService.processPayment(paymentRequestDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authz.isSelfOrAdmin(#id, authentication)")
    @Operation(summary = "Get payment by ID", description = "Payment owner or Admin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public PaymentResponseDto getPaymentById(@PathVariable Long id, Authentication authentication) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("@authz.isBookingOwnerOrAdmin(#bookingId, authentication)")
    @Operation(summary = "Get payment details for a booking",
            description = "Booking owner or Admin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found for this booking")
    })
    public PaymentResponseDto getPaymentByBooking(@PathVariable Long bookingId, Authentication authentication) {
        return paymentService.getPaymentByBooking(bookingId);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a payment", description = "Admin only - Process a refund for a completed payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "400", description = "Payment cannot be refunded"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponseDto> refundPayment(@PathVariable Long paymentId) {
        PaymentResponseDto refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedPayment);
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my payments", description = "Get all payments for the authenticated user")
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        List<PaymentResponseDto> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }
}