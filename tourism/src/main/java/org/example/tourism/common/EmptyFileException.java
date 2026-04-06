package org.example.tourism.common;

public class EmptyFileException extends RuntimeException{
    public EmptyFileException(String message) {
        super(message);
    }
}