package org.example.tourism.booking.validation;

import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: DECORATOR (Chain Builder)
 *
 * Builds the validation chain by wrapping validators.
 * New validators can be added to the chain without modifying existing code.
 *
 * Chain order: Base -> GuestCount -> StayDuration -> AdvanceBooking
 * Each validator decorates the previous one, adding its own validation layer.
 */
@Component
public class ValidationChainBuilder {

    private final BaseBookingValidator baseValidator;

    public ValidationChainBuilder(BaseBookingValidator baseValidator) {
        this.baseValidator = baseValidator;
    }

    /**
     * Build the complete validation chain
     */
    public BookingValidator buildValidationChain() {
        // Start with base validation
        BookingValidator chain = baseValidator;

        // Decorate with guest count validation
        chain = new GuestCountValidator(chain);

        // Decorate with stay duration validation
        chain = new StayDurationValidator(chain);

        // Decorate with advance booking validation
        chain = new AdvanceBookingValidator(chain);

        return chain;
    }
}