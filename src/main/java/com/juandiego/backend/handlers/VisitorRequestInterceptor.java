package com.juandiego.backend.handlers;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.juandiego.backend.exceptions.InvalidVisitorHeadersException;
import com.juandiego.backend.services.VisitorRateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class VisitorRequestInterceptor implements HandlerInterceptor {

    private static final String VISITOR_ID_HEADER = "x-visitorId";
    private static final String IP_HASH_HEADER = "x-ipHash";
    private static final String USER_AGENT_HEADER = "x-userAgent";
    private static final String LAST_SEEN_AT_HEADER = "x-lastSeenAt";
    private static final long EPOCH_MILLISECONDS_THRESHOLD = 100_000_000_000L;

    private final VisitorRateLimiter visitorRateLimiter;

    public VisitorRequestInterceptor(VisitorRateLimiter visitorRateLimiter) {
        this.visitorRateLimiter = visitorRateLimiter;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        UUID visitorId = parseVisitorId(requireHeader(request, VISITOR_ID_HEADER));
        requireHeader(request, IP_HASH_HEADER);
        requireHeader(request, USER_AGENT_HEADER);
        validateTimestamp(requireHeader(request, LAST_SEEN_AT_HEADER));

        visitorRateLimiter.registerRequest(visitorId);
        return true;
    }

    private String requireHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            throw new InvalidVisitorHeadersException("Missing required header: " + headerName);
        }
        return value.trim();
    }

    private UUID parseVisitorId(String value) {
        try {
            UUID visitorId = UUID.fromString(value);
            if (!visitorId.toString().equalsIgnoreCase(value)
                    || visitorId.version() != 4
                    || visitorId.variant() != 2) {
                throw new IllegalArgumentException("UUID is not version 4");
            }
            return visitorId;
        } catch (IllegalArgumentException exception) {
            throw new InvalidVisitorHeadersException(
                    VISITOR_ID_HEADER + " must be a valid UUID v4");
        }
    }

    private void validateTimestamp(String value) {
        try {
            Instant.parse(value);
            return;
        } catch (DateTimeException ignored) {
            // Numeric Unix timestamps are also accepted for browser clients.
        }

        try {
            long timestamp = Long.parseLong(value);
            if (timestamp > -EPOCH_MILLISECONDS_THRESHOLD
                    && timestamp < EPOCH_MILLISECONDS_THRESHOLD) {
                Instant.ofEpochSecond(timestamp);
            } else {
                Instant.ofEpochMilli(timestamp);
            }
        } catch (NumberFormatException | DateTimeException exception) {
            throw new InvalidVisitorHeadersException(
                    LAST_SEEN_AT_HEADER + " must be a valid ISO-8601 or Unix timestamp");
        }
    }
}
