package com.juandiego.backend.exceptions;

import java.time.Instant;

public class VisitorTemporarilyBlockedException extends RuntimeException {

    private final Instant blockedUntil;

    public VisitorTemporarilyBlockedException(Instant blockedUntil) {
        super("Visitor is temporarily blocked");
        this.blockedUntil = blockedUntil;
    }

    public Instant getBlockedUntil() {
        return blockedUntil;
    }
}
