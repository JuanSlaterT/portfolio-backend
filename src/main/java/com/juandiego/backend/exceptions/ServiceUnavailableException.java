package com.juandiego.backend.exceptions;

public class LanguagesServiceUnavailableException extends RuntimeException {

    public LanguagesServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}