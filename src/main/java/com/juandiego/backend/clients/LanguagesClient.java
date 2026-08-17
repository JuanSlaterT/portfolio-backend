package com.juandiego.backend.clients;

import java.io.IOException;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.juandiego.backend.exceptions.LanguagesServiceUnavailableException;

import tools.jackson.databind.JsonNode;

@Component
public class LanguagesClient {

        private final RestClient restClient;

    public LanguagesClient(
            RestClient.Builder builder,
            @Value("${services.languages.url}") String baseUrl,
            @Value("${services.languages.auth-enabled:false}") boolean authEnabled,

    ) throws IOException {
        System.out.println("LANGUAGE SERVICE URL: " + baseUrl);
        System.out.println("LANGUAGE AUTH ENABLED: " + authEnabled);
        builder.baseUrl(baseUrl);

        if (authEnabled) {

            GoogleCredentials credentials =
                    GoogleCredentials.getApplicationDefault();
                    System.out.println(
                        "GOOGLE CREDENTIALS: " +
                        credentials.getClass().getName()
                );
            if (!(credentials instanceof IdTokenProvider provider)) {
                throw new IllegalStateException(
                        "Current credentials cannot generate ID tokens"
                );
            }

            IdTokenCredentials idTokenCredentials =
                    IdTokenCredentials.newBuilder()
                            .setIdTokenProvider(provider)
                            .setTargetAudience(baseUrl)
                            .build();

            builder.requestInterceptor(
                    (request, body, execution) -> {
                        System.out.println(
                                "ADDING CLOUD RUN AUTH TO: " +
                                request.getURI()
                        );
            
                        idTokenCredentials.refreshIfExpired();

                        request.getHeaders().setBearerAuth(
                                idTokenCredentials
                                        .getAccessToken()
                                        .getTokenValue()
                        );

                        return execution.execute(request, body);
                    }
            );
        }

        this.restClient = builder.build();
    }

        public JsonNode getLanguages() {
                return execute(() -> restClient.get()
                                .uri("/api/languages")
                                .retrieve()
                                .body(JsonNode.class));
        }

        public JsonNode getLanguage(String lang) {
                return execute(() -> restClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .path("/api/languages/{lang}")
                                                .build(lang))
                                .retrieve()
                                .body(JsonNode.class));
        }

        private JsonNode execute(Supplier<JsonNode> request) {
                try {
                        return request.get();
                } catch (ResourceAccessException e) {
                        throw new LanguagesServiceUnavailableException(
                                        "Languages service is currently unavailable",
                                        e);
                }
        }
}