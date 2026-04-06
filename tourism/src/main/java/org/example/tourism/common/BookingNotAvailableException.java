package org.example.tourism.common;

public class BookingNotAvailableException extends RuntimeException {

    public BookingNotAvailableException(String message) {
        super(message);
    }
}