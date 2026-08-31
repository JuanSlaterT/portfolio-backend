package com.juandiego.backend.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.juandiego.backend.exceptions.InvalidVisitorHeadersException;
import com.juandiego.backend.exceptions.VisitorTemporarilyBlockedException;
import com.juandiego.backend.responses.ApiResponse;

@RestControllerAdvice
public class VisitorRequestExceptionHandler {

    private static final String MISSING_TIME_HEADER = "x-missingTime";

    @ExceptionHandler(InvalidVisitorHeadersException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidVisitorHeaders(
            InvalidVisitorHeadersException exception) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        null));
    }

    @ExceptionHandler(VisitorTemporarilyBlockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleVisitorTemporarilyBlocked(
            VisitorTemporarilyBlockedException exception) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(MISSING_TIME_HEADER, exception.getBlockedUntil().toString())
                .body(new ApiResponse<>(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        exception.getMessage(),
                        null));
    }
}
