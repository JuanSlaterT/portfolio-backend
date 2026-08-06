package com.juandiego.backend.responses;

public record ApiResponse<T>(
    int statusCode,
    String message,
    T data
){}
