package org.example.tourism.common;

public class CancellationNotAllowedException extends RuntimeException{
    public CancellationNotAllowedException(String message) {
        super(message);
    }
}