package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DESIGN PATTERN: DECORATOR (Concrete Decorator)
 *
 * Validates that bookings are made within acceptable timeframes:
 * - Not too far in the future (max 1 year)
 * - Not in the past
 */
public class AdvanceBookingValidator extends BookingValidatorDecorator {

    private static final long MAX_ADVANCE_DAYS = 365;

    public AdvanceBookingValidator(BookingValidator wrappedValidator) {
        super(wrappedValidator);
    }

    @Override
    protected void additionalValidation(BookingRequestDto request) {
        LocalDate today = LocalDate.now();

        if (request.getCheckInDate().isBefore(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, request.getCheckInDate());
        if (daysUntilCheckIn > MAX_ADVANCE_DAYS) {
            throw new IllegalArgumentException(
                    "Bookings can only be made up to " + MAX_ADVANCE_DAYS + " days in advance");
        }
    }
}