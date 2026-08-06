package com.juandiego.backend.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import com.juandiego.backend.responses.ApiResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.StringNode;

@RestControllerAdvice
public class RestClientExceptionHandler {

    private final ObjectMapper objectMapper;

    public RestClientExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiResponse<Object>> handleRestClientException(
            RestClientResponseException exception) {

        int statusCode = exception.getStatusCode().value();
        String message = exception.getStatusText();
        Object data = null;

        String responseBody = exception.getResponseBodyAsString();

        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode errorBody = objectMapper.readTree(responseBody);

                if (errorBody.hasNonNull("message")) {
                    message = errorBody.get("message").asString();
                } else if (errorBody.hasNonNull("error")) {
                    message = errorBody.get("error").asString();
                }

                data = errorBody.has("data")
                        ? errorBody.get("data")
                        : errorBody;

            } catch (Exception ignored) {
                data = StringNode.valueOf(responseBody);
            }
        }

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ApiResponse<>(
                        statusCode,
                        message,
                        data
                ));
    }
}