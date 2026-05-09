package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;

import java.time.temporal.ChronoUnit;

/**
 * DESIGN PATTERN: DECORATOR (Concrete Decorator)
 *
 * Validates the duration of stay:
 * - Minimum 1 night
 * - Maximum 30 nights
 */
public class StayDurationValidator extends BookingValidatorDecorator {

    private static final long MAX_NIGHTS = 30;

    public StayDurationValidator(BookingValidator wrappedValidator) {
        super(wrappedValidator);
    }

    @Override
    protected void additionalValidation(BookingRequestDto request) {
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        if (nights < 1) {
            throw new IllegalArgumentException("Minimum stay is 1 night");
        }
        if (nights > MAX_NIGHTS) {
            throw new IllegalArgumentException("Maximum stay is " + MAX_NIGHTS + " nights");
        }
    }
}