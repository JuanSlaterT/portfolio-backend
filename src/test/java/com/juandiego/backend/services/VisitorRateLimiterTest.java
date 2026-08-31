package com.juandiego.backend.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.juandiego.backend.exceptions.VisitorTemporarilyBlockedException;

class VisitorRateLimiterTest {

    private static final UUID VISITOR_ID = UUID.fromString("3d594650-3436-4f38-8d58-e91f0e1c43ed");
    private static final Instant START_TIME = Instant.parse("2026-08-31T12:00:00Z");

    @Test
    void blocksTheEleventhRequestForFiveMinutes() {
        MutableClock clock = new MutableClock(START_TIME);
        VisitorRateLimiter rateLimiter = new VisitorRateLimiter(clock);

        for (int request = 0; request < 10; request++) {
            assertDoesNotThrow(() -> rateLimiter.registerRequest(VISITOR_ID));
        }

        VisitorTemporarilyBlockedException exception = assertThrows(
                VisitorTemporarilyBlockedException.class,
                () -> rateLimiter.registerRequest(VISITOR_ID));

        assertEquals(START_TIME.plus(Duration.ofMinutes(5)), exception.getBlockedUntil());
    }

    @Test
    void allowsRequestsAgainWhenTheBlockExpires() {
        MutableClock clock = new MutableClock(START_TIME);
        VisitorRateLimiter rateLimiter = new VisitorRateLimiter(clock);

        for (int request = 0; request < 10; request++) {
            rateLimiter.registerRequest(VISITOR_ID);
        }
        assertThrows(
                VisitorTemporarilyBlockedException.class,
                () -> rateLimiter.registerRequest(VISITOR_ID));

        clock.advance(Duration.ofMinutes(4).plusSeconds(59));
        assertThrows(
                VisitorTemporarilyBlockedException.class,
                () -> rateLimiter.registerRequest(VISITOR_ID));

        clock.advance(Duration.ofSeconds(1));
        assertDoesNotThrow(() -> rateLimiter.registerRequest(VISITOR_ID));
    }

    @Test
    void removesRequestsThatAreOutsideTheSlidingWindow() {
        MutableClock clock = new MutableClock(START_TIME);
        VisitorRateLimiter rateLimiter = new VisitorRateLimiter(clock);

        for (int request = 0; request < 10; request++) {
            rateLimiter.registerRequest(VISITOR_ID);
        }

        clock.advance(Duration.ofMinutes(1));

        assertDoesNotThrow(() -> rateLimiter.registerRequest(VISITOR_ID));
    }

    private static final class MutableClock extends Clock {

        private Instant currentTime;

        private MutableClock(Instant currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentTime;
        }

        private void advance(Duration duration) {
            currentTime = currentTime.plus(duration);
        }
    }
}
