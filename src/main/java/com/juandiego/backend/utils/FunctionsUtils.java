package com.juandiego.backend.utils;

import org.springframework.http.MediaType;

public final class FunctionsUtils {
    private FunctionsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static boolean isJson(MediaType mediaType) {
        if(mediaType == null) return false;
        return MediaType.APPLICATION_JSON.includes(mediaType) || mediaType.getSubtype().endsWith("+json");
    }
}