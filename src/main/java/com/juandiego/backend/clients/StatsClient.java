package com.juandiego.backend.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.juandiego.backend.utils.FunctionsUtils;

import tools.jackson.databind.JsonNode;

@Component
public class StatsClient {
    private final RestClient restClient;

    public StatsClient(RestClient.Builder builder, @Value("${services.stats.url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public JsonNode getStats() {
        return FunctionsUtils.execute(() -> restClient.get()
                .uri("api/stats")
                .retrieve()
                .body(JsonNode.class));
    }
}
