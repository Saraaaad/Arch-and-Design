package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: DECORATOR (Concrete Component)
 *
 * The base validator that performs essential date validations.
 * This is the core validation that all decorators build upon.
 */
@Component
public class BaseBookingValidator implements BookingValidator {

    @Override
    public void validate(BookingRequestDto request) {
        if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
                request.getCheckOutDate().isEqual(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }
}