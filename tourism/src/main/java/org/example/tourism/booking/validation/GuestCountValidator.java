package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;

/**
 * DESIGN PATTERN: DECORATOR (Concrete Decorator)
 *
 * Adds guest count validation to the booking validation chain.
 * Ensures the number of guests is within acceptable limits.
 */
public class GuestCountValidator extends BookingValidatorDecorator {

    private static final int MAX_GUESTS = 10;

    public GuestCountValidator(BookingValidator wrappedValidator) {
        super(wrappedValidator);
    }

    @Override
    protected void additionalValidation(BookingRequestDto request) {
        if (request.getGuests() == null || request.getGuests() < 1) {
            throw new IllegalArgumentException("At least 1 guest is required");
        }
        if (request.getGuests() > MAX_GUESTS) {
            throw new IllegalArgumentException("Maximum " + MAX_GUESTS + " guests allowed per booking");
        }
    }
}