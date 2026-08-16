package com.juandiego.backend.handlers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.juandiego.backend.responses.ApiResponse;
import com.juandiego.backend.utils.FunctionsUtils;

import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final List<String> ERROR_FIELDS_TO_REMOVE = List.of(
            "trace",
            "status",
            "error");

    public GlobalResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Object> handleResourceAccessException(
        ResourceAccessException ex, HttpServletRequest request){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "timestamp", Instant.now(),
                "messge", "Service temporarily unavailable",
                "path", request.getRequestURI()
            ));
        }


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!FunctionsUtils.isJson(contentType)) {
            return body;
        }

        Object sanitizedBody = sanitizeBody(body);
        if (body instanceof ApiResponse<?>) {
            return sanitizedBody;
        }
        int statusCode = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            statusCode = servletResponse.getServletResponse().getStatus();
        }
        String message;
        try {
            message = HttpStatus.valueOf(statusCode).getReasonPhrase();
        } catch (Exception e) {
            message = "Internal server error";
        }

        return new ApiResponse<>(statusCode, message, sanitizedBody);
    }

    private Object sanitizeBody(Object body) {
        if (body == null)
            return null;
        JsonNode jsonNode = objectMapper.valueToTree(body);
        removeFieldRecursively(jsonNode, ERROR_FIELDS_TO_REMOVE);
        return objectMapper.treeToValue(jsonNode, body.getClass());
    }

    private void removeFieldRecursively(
            JsonNode node,
            List<String> fieldsToRemove) {
        if (node == null || fieldsToRemove == null || fieldsToRemove.isEmpty()) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            objectNode.remove(fieldsToRemove);

            objectNode.values().forEach(
                    child -> removeFieldRecursively(child, fieldsToRemove));

        } else if (node.isArray()) {
            node.values().forEach(
                    child -> removeFieldRecursively(child, fieldsToRemove));
        }
    }
}