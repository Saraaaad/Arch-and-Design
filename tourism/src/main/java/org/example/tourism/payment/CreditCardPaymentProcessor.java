package org.example.tourism.payment;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.example.tourism.mapper.DtoMapperFactory;
import org.example.tourism.notification.NotificationEvent;
import org.example.tourism.notification.NotificationEventPublisher;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * DESIGN PATTERN: TEMPLATE METHOD (Concrete Implementation)
 *
 * Implements credit card payment processing.
 * Overrides the abstract methods from AbstractPaymentProcessor.
 */
@Component
@Slf4j
public class CreditCardPaymentProcessor extends AbstractPaymentProcessor {

    private final UserRepository userRepository;
    private final NotificationEventPublisher eventPublisher;

    public CreditCardPaymentProcessor(PaymentRepository paymentRepository,
                                      BookingService bookingService,
                                      DtoMapperFactory dtoMapperFactory,
                                      UserRepository userRepository,
                                      NotificationEventPublisher eventPublisher) {
        super(paymentRepository, bookingService, dtoMapperFactory);
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected String processSpecificPayment(BookingResponseDto booking) {
        log.info("Processing credit card payment for ${}", booking.getTotalPrice());

        // Simulate credit card processing
        try {
            Thread.sleep(500); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentFailedException("Payment processing interrupted");
        }

        return UUID.randomUUID().toString();
    }

    @Override
    protected void postProcessPayment(Payment payment, BookingResponseDto booking) {
        // Send confirmation notification
        try {
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            String details = String.format(
                    "Credit Card Payment - Hotel ID: %s, Check-in: %s, Check-out: %s, Total: $%.2f, Transaction: %s",
                    booking.getHotelId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getTotalPrice(),
                    payment.getTransactionId()
            );

            NotificationEvent event = new NotificationEvent(
                    NotificationEvent.EventType.PAYMENT_COMPLETED,
                    user.getId(),
                    user.getEmail(),
                    payment.getId(),
                    details
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage());
        }
    }

    @Override
    protected String getPaymentMethod() {
        return "CREDIT_CARD";
    }

    @Override
    protected String getProcessorName() {
        return "CreditCardPaymentProcessor";
    }
}