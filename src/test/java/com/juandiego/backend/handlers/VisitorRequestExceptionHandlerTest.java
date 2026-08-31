package com.juandiego.backend.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.juandiego.backend.exceptions.VisitorTemporarilyBlockedException;

class VisitorRequestExceptionHandlerTest {

    @Test
    void returnsTooManyRequestsAndTheFutureUnlockTimestamp() {
        Instant blockedUntil = Instant.parse("2026-08-31T12:05:00Z");
        VisitorRequestExceptionHandler handler = new VisitorRequestExceptionHandler();

        var response = handler.handleVisitorTemporarilyBlocked(
                new VisitorTemporarilyBlockedException(blockedUntil));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(blockedUntil.toString(), response.getHeaders().getFirst("x-missingTime"));
    }
}
