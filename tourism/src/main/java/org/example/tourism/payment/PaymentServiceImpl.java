package org.example.tourism.payment;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto) {
        log.info("Processing payment for booking ID: {}", paymentRequestDto.getBookingId());

        BookingResponseDto booking = bookingService.getBooking(paymentRequestDto.getBookingId());

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled booking");
        }

        // Check if payment already exists
        var existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Payment already completed for this booking");
            }
        }

        // Mock payment processing - always succeeds for demo
        boolean success = true;
        String transactionId = UUID.randomUUID().toString();

        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setTransactionId(transactionId);

        Payment savedPayment = paymentRepository.save(payment);

        if (success) {
            log.info("Payment successful for booking ID: {}, transaction ID: {}", booking.getId(), transactionId);

            // Confirm the booking
            bookingService.confirmBooking(booking.getId());

            // Send notification
            notificationService.sendNotification(
                    "user@example.com",
                    "Booking Confirmed",
                    "Your booking #" + booking.getId() + " has been confirmed. Total amount: $" + booking.getTotalPrice()
            );
        } else {
            log.error("Payment failed for booking ID: {}", booking.getId());
        }

        return mapToDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByBooking(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking: " + bookingId));
        return mapToDto(payment);
    }

    private PaymentResponseDto mapToDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        BeanUtils.copyProperties(payment, dto);
        return dto;
    }
}