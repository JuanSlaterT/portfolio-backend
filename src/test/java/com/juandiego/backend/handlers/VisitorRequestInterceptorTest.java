package com.juandiego.backend.handlers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.juandiego.backend.exceptions.InvalidVisitorHeadersException;
import com.juandiego.backend.services.VisitorRateLimiter;

class VisitorRequestInterceptorTest {

    private static final UUID VISITOR_ID = UUID.fromString("3d594650-3436-4f38-8d58-e91f0e1c43ed");

    private VisitorRateLimiter rateLimiter;
    private VisitorRequestInterceptor interceptor;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        rateLimiter = mock(VisitorRateLimiter.class);
        interceptor = new VisitorRequestInterceptor(rateLimiter);
        request = validRequest();
    }

    @Test
    void acceptsValidHeadersAndRegistersTheRequest() {
        assertDoesNotThrow(() -> interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));

        verify(rateLimiter).registerRequest(VISITOR_ID);
    }

    @Test
    void rejectsRequestsWithMissingHeaders() {
        request.removeHeader("x-ipHash");

        assertThrows(
                InvalidVisitorHeadersException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void rejectsVisitorIdsThatAreNotUuidVersionFour() {
        request.removeHeader("x-visitorId");
        request.addHeader("x-visitorId", "f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

        assertThrows(
                InvalidVisitorHeadersException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void rejectsInvalidLastSeenTimestamps() {
        request.removeHeader("x-lastSeenAt");
        request.addHeader("x-lastSeenAt", "not-a-timestamp");

        assertThrows(
                InvalidVisitorHeadersException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void acceptsUnixTimestampsInMilliseconds() {
        request.removeHeader("x-lastSeenAt");
        request.addHeader("x-lastSeenAt", "1788177600000");

        assertDoesNotThrow(() -> interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));
    }

    @Test
    void allowsCorsPreflightWithoutVisitorHeaders() {
        MockHttpServletRequest preflightRequest = new MockHttpServletRequest("OPTIONS", "/api/stats");

        assertDoesNotThrow(() -> interceptor.preHandle(
                preflightRequest,
                new MockHttpServletResponse(),
                new Object()));
    }

    private MockHttpServletRequest validRequest() {
        MockHttpServletRequest validRequest = new MockHttpServletRequest("GET", "/api/stats");
        validRequest.addHeader("x-visitorId", VISITOR_ID.toString());
        validRequest.addHeader("x-ipHash", "hashed-ip-address");
        validRequest.addHeader("x-userAgent", "Mozilla/5.0");
        validRequest.addHeader("x-lastSeenAt", "2026-08-31T12:00:00Z");
        return validRequest;
    }
}
