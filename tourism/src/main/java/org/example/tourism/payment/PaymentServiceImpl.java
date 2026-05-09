package org.example.tourism.payment;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.common.PaymentAlreadyRefundedException;
import org.example.tourism.common.PaymentFailedException;
import org.example.tourism.notification.NotificationService;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto) {
        log.info("Processing payment for booking ID: {}", paymentRequestDto.getBookingId());

        BookingResponseDto booking = bookingService.getBooking(paymentRequestDto.getBookingId());

        // Check if booking is already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled booking");
        }

        // Check if booking is already confirmed (already paid)
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking is already confirmed and paid");
        }

        // Check if payment already exists
        var existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Payment already completed for this booking");
            }
            if (existing.getStatus() == PaymentStatus.PENDING) {
                log.info("Found existing pending payment for booking ID: {}", booking.getId());
                // Return existing payment instead of creating a new one
                return mapToDto(existing);
            }

        }

        // Process payment
        boolean success = processMockPayment(booking);
        String transactionId = UUID.randomUUID().toString();

        if (!success) {
            throw new PaymentFailedException("Payment processing failed. Please try again");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setUserId(booking.getUserId());
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setRefundedAmount(java.math.BigDecimal.ZERO);

        Payment savedPayment = paymentRepository.save(payment);

        if (success) {
            log.info("Payment successful for booking ID: {}, transaction ID: {}", booking.getId(), transactionId);

            // Confirm the booking
            BookingResponseDto confirmedBooking = bookingService.confirmBooking(booking.getId());
            log.info("Booking {} confirmed successfully", confirmedBooking.getId());

            // Send notification
            try {
                User user = userRepository.findById(booking.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + booking.getUserId()));

                String details = String.format(
                        "Hotel ID: %s, Check-in: %s, Check-out: %s, Total: $%.2f, Transaction: %s",
                        booking.getHotelId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getTotalPrice(),
                        transactionId
                );

                notificationService.sendBookingConfirmation(user.getEmail(), booking.getId(), details);
                log.info("Notification sent to user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send notification for booking {}: {}", booking.getId(), e.getMessage());
            }
        } else {
            log.error("Payment failed for booking ID: {}", booking.getId());
        }

        return mapToDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByBooking(Long bookingId) {
        log.info("Fetching payment for booking ID: {}", bookingId);

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking: " + bookingId));

        return mapToDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        return mapToDto(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getCreatedAt() == null) {
            throw new IllegalStateException("Payment creation date is missing");
        }

        // Check if payment can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded. Current status: " + payment.getStatus());
        }

        // Check if already refunded
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyRefundedException("Payment has already been refunded");
        }


        // Check if refund is within time limit (e.g., 7 days after payment)
        if (payment.getCreatedAt() != null) {
            LocalDateTime refundDeadline = payment.getCreatedAt().plusDays(7);
            if (LocalDateTime.now().isAfter(refundDeadline)) {
                throw new IllegalStateException("Refund period has expired (7 days from payment date)");
            }
        }

        // Process refund
        boolean refundSuccess = processMockRefund(payment);

        if (refundSuccess) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAmount(payment.getAmount());
            Payment refundedPayment = paymentRepository.save(payment);

            log.info("Refund successful for payment ID: {}, booking ID: {}", paymentId, payment.getBookingId());

            // Cancel the booking when refund is processed
            try {
                bookingService.cancelBooking(payment.getBookingId());
                log.info("Booking {} cancelled due to refund", payment.getBookingId());
            } catch (Exception e) {
                log.error("Failed to cancel booking after refund: {}", e.getMessage());
            }

            // Send refund notification
            try {
                BookingResponseDto booking = bookingService.getBooking(payment.getBookingId());
                User user = userRepository.findById(booking.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                notificationService.sendNotification(
                        user.getEmail(),
                        "Refund Processed for Booking #" + booking.getId(),
                        String.format("Your refund of $%.2f for booking #%d has been processed. Transaction: %s",
                                payment.getAmount(), booking.getId(), payment.getTransactionId())
                );
                log.info("Refund notification sent to user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send refund notification: {}", e.getMessage());
            }
        } else {
            log.error("Refund failed for payment ID: {}", paymentId);
            throw new IllegalStateException("Refund processing failed");
        }

        return mapToDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getUserPayments(Long userId) {
        log.info("Fetching all payments for user ID: {}", userId);

        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);

        log.info("Found {} payments for user ID: {}", payments.size(), userId);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUserOwnsBooking(Long bookingId, Long userId) {
        BookingResponseDto booking = bookingService.getBooking(bookingId);
        return booking.getUserId().equals(userId);
    }

    private boolean processMockPayment(BookingResponseDto booking) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private boolean processMockRefund(Payment payment) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentOwner(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        BookingResponseDto booking = bookingService.getBooking(payment.getBookingId());
        return booking.getUserId().equals(userId);
    }

    private PaymentResponseDto mapToDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        BeanUtils.copyProperties(payment, dto);

        if (dto.getPaymentMethod() == null) {
            dto.setPaymentMethod("CREDIT_CARD");
        }
        if (dto.getRefundedAmount() == null) {
            dto.setRefundedAmount(java.math.BigDecimal.ZERO);
        }

        return dto;
    }
}