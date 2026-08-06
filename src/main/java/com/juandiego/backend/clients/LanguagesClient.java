package com.juandiego.backend.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class LanguagesClient {

    private final RestClient restClient;

    public LanguagesClient(
            RestClient.Builder builder,
            @Value("${services.languages.url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public JsonNode getLanguages() {
        return restClient.get()
                .uri("/api/languages")
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getLanguage(String lang) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/languages/{lang}")
                        .build(lang))
                .retrieve()
                .body(JsonNode.class);
    }
}