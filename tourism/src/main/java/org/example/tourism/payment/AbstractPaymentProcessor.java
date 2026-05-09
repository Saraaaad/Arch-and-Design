package org.example.tourism.payment;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.common.PaymentFailedException;
import org.example.tourism.mapper.DtoMapperFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * DESIGN PATTERN: TEMPLATE METHOD
 *
 * Defines the skeleton of payment processing algorithm.
 * Subclasses override specific steps while the overall flow remains consistent.
 *
 * Template: validatePayment -> checkExistingPayment -> processPayment -> createRecord -> confirmBooking
 */
@Slf4j
public abstract class AbstractPaymentProcessor {

    protected final PaymentRepository paymentRepository;
    protected final BookingService bookingService;
    protected final DtoMapperFactory dtoMapperFactory;

    public AbstractPaymentProcessor(PaymentRepository paymentRepository,
                                    BookingService bookingService,
                                    DtoMapperFactory dtoMapperFactory) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.dtoMapperFactory = dtoMapperFactory;
    }

    /**
     * TEMPLATE METHOD: Defines the algorithm skeleton
     * Subclasses cannot override this method (final)
     */
    public final PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment using {}", getProcessorName());

        // Step 1: Validate booking (same for all processors)
        BookingResponseDto booking = validateBooking(request.getBookingId());

        // Step 2: Check for existing payment (same for all processors)
        checkExistingPayment(request.getBookingId());

        // Step 3: Process payment (varies by payment type - ABSTRACT)
        String transactionId = processSpecificPayment(booking);

        // Step 4: Create payment record (same for all processors)
        Payment payment = createPaymentRecord(booking, transactionId);

        // Step 5: Post-processing (hook method - can be overridden)
        postProcessPayment(payment, booking);

        // Step 6: Confirm booking (same for all processors)
        confirmBookingIfSuccessful(payment, booking);

        return dtoMapperFactory.createPaymentResponseDto(payment);
    }

    // Common step - same for all
    protected BookingResponseDto validateBooking(Long bookingId) {
        BookingResponseDto booking = bookingService.getBooking(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking is already confirmed and paid");
        }

        return booking;
    }

    // Common step - same for all
    protected void checkExistingPayment(Long bookingId) {
        var existingPayment = paymentRepository.findByBookingId(bookingId);
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Payment already completed for this booking");
            }
            if (existing.getStatus() == PaymentStatus.PENDING) {
                log.info("Found existing pending payment for booking ID: {}", bookingId);
                throw new IllegalStateException("Pending payment exists for this booking");
            }
        }
    }

    /**
     * ABSTRACT METHOD: Must be implemented by subclasses
     * Different payment types have different processing logic
     */
    protected abstract String processSpecificPayment(BookingResponseDto booking);

    // Common step - same for all
    protected Payment createPaymentRecord(BookingResponseDto booking, String transactionId) {
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setUserId(booking.getUserId());
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod(getPaymentMethod());
        payment.setRefundedAmount(java.math.BigDecimal.ZERO);
        return paymentRepository.save(payment);
    }

    /**
     * HOOK METHOD: Optional override for post-processing
     */
    protected void postProcessPayment(Payment payment, BookingResponseDto booking) {
        // Default: no post-processing
    }

    // Common step - same for all
    protected void confirmBookingIfSuccessful(Payment payment, BookingResponseDto booking) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            bookingService.confirmBooking(booking.getId());
            log.info("Booking {} confirmed after successful payment", booking.getId());
        }
    }

    /**
     * Returns the payment method identifier
     */
    protected abstract String getPaymentMethod();

    /**
     * Returns the processor name for logging
     */
    protected abstract String getProcessorName();
}