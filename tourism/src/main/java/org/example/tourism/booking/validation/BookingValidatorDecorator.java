package org.example.tourism.booking.validation;

import org.example.tourism.booking.BookingRequestDto;

/**
 * DESIGN PATTERN: DECORATOR (Abstract Decorator)
 *
 * Abstract decorator class that wraps a BookingValidator and delegates to it.
 * Concrete decorators extend this class to add additional validation.
 */
public abstract class BookingValidatorDecorator implements BookingValidator {

    protected final BookingValidator wrappedValidator;

    public BookingValidatorDecorator(BookingValidator wrappedValidator) {
        this.wrappedValidator = wrappedValidator;
    }

    @Override
    public void validate(BookingRequestDto request) {
        // Delegate to the wrapped validator first (chain of responsibility)
        wrappedValidator.validate(request);
        // Then perform additional validation
        additionalValidation(request);
    }

    /**
     * Additional validation to be implemented by concrete decorators
     */
    protected abstract void additionalValidation(BookingRequestDto request);
}