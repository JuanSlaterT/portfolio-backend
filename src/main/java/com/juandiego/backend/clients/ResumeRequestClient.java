package com.juandiego.backend.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.juandiego.backend.utils.FunctionsUtils;

import tools.jackson.databind.JsonNode;

@Component
public class ResumeRequestClient {

    private final RestClient restClient;

    public ResumeRequestClient(
            RestClient.Builder builder,
            @Value("${services.resume-request.url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public JsonNode createResumeRequest(JsonNode request) {
        return FunctionsUtils.execute(() -> restClient.post()
                .uri("/api/resume-request")
                .body(request)
                .retrieve()
                .body(JsonNode.class));
    }
}
