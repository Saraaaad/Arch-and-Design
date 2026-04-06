package org.example.tourism.common;

public class ReviewNotAllowedException extends RuntimeException{
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}