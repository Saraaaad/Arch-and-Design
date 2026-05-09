package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;

/**
 * DESIGN PATTERN: DECORATOR (Component Interface)
 *
 * Base interface for booking validation decorators.
 * Each validator can add its own validation logic while delegating
 * to the next validator in the chain.
 */
public interface BookingValidator {

    /**
     * Validate a booking request
     * @param request The booking request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validate(BookingRequestDto request);
}