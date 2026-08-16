package com.juandiego.backend.clients;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.juandiego.backend.exceptions.LanguagesServiceUnavailableException;

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
                return execute(() ->
                        restClient.get()
                                .uri("api/languages")
                                .retrieve()
                                .body(JsonNode.class)
                        );
        }

        public JsonNode getLanguage(String lang) {
                return execute(() ->
                        restClient.get()
                        .uri(uriBuilder-> uriBuilder
                                .path("/api/languages/{lang}")
                                .build(lang))
                        .retrieve()
                        .body(JsonNode.class)
                        );
                }
              

        private JsonNode execute(Supplier<JsonNode> request) {
                try {
                        return request.get();
                } catch (ResourceAccessException e) {
                        throw new LanguagesServiceUnavailableException("Languages service is currently unavailable", e);
                }
        }
}