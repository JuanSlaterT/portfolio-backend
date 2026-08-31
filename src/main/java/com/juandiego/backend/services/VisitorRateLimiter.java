package com.juandiego.backend.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.juandiego.backend.exceptions.VisitorTemporarilyBlockedException;

@Service
public class VisitorRateLimiter {

    static final int MAX_REQUESTS_PER_MINUTE = 10;
    static final Duration REQUEST_WINDOW = Duration.ofMinutes(1);
    static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private static final long CLEANUP_INTERVAL = 256;

    private final ConcurrentMap<UUID, VisitorState> visitors = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();
    private final Clock clock;

    public VisitorRateLimiter() {
        this(Clock.systemUTC());
    }

    VisitorRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void registerRequest(UUID visitorId) {
        Instant now = clock.instant();

        visitors.compute(visitorId, (id, state) -> {
            VisitorState currentState = state == null ? new VisitorState() : state;
            currentState.registerRequest(now);
            return currentState;
        });

        if (requestCounter.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            removeInactiveVisitors(now);
        }
    }

    private void removeInactiveVisitors(Instant now) {
        visitors.forEach((visitorId, state) ->
                visitors.computeIfPresent(visitorId, (id, currentState) ->
                        currentState.isInactive(now) ? null : currentState));
    }

    private static final class VisitorState {

        private final Deque<Instant> requestTimes = new ArrayDeque<>();
        private Instant blockedUntil;
        private Instant lastActivity;

        private void registerRequest(Instant now) {
            lastActivity = now;

            if (blockedUntil != null) {
                if (now.isBefore(blockedUntil)) {
                    throw new VisitorTemporarilyBlockedException(blockedUntil);
                }

                blockedUntil = null;
                requestTimes.clear();
            }

            Instant windowStart = now.minus(REQUEST_WINDOW);
            while (!requestTimes.isEmpty() && !requestTimes.peekFirst().isAfter(windowStart)) {
                requestTimes.removeFirst();
            }

            if (requestTimes.size() >= MAX_REQUESTS_PER_MINUTE) {
                blockedUntil = now.plus(BLOCK_DURATION);
                requestTimes.clear();
                throw new VisitorTemporarilyBlockedException(blockedUntil);
            }

            requestTimes.addLast(now);
        }

        private boolean isInactive(Instant now) {
            if (lastActivity == null || lastActivity.isAfter(now.minus(BLOCK_DURATION))) {
                return false;
            }

            return blockedUntil == null || !now.isBefore(blockedUntil);
        }
    }
}
