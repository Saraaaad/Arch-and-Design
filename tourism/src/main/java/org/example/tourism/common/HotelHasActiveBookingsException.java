package org.example.tourism.common;

public class HotelHasActiveBookingsException extends RuntimeException {

    public HotelHasActiveBookingsException(String message) {
        super(message);
    }
}