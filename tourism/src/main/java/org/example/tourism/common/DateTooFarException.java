package org.example.tourism.common;

public class DateTooFarException extends RuntimeException{
    public DateTooFarException(String message) {
        super(message);
    }
}