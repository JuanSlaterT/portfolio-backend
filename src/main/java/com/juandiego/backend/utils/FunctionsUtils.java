package com.juandiego.backend.utils;

import java.util.function.Supplier;

import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;

import com.juandiego.backend.exceptions.ServiceUnavailableException;

import tools.jackson.databind.JsonNode;

public final class FunctionsUtils {
    private FunctionsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isJson(MediaType mediaType) {
        if (mediaType == null)
            return false;
        return MediaType.APPLICATION_JSON.includes(mediaType) || mediaType.getSubtype().endsWith("+json");
    }

    public static JsonNode execute(Supplier<JsonNode> request) {
        try {
            return request.get();
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("Service is currently unavailable", e);
        }
    }
}