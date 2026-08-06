package com.juandiego.backend.services;

import org.springframework.stereotype.Service;

import com.juandiego.backend.clients.LanguagesClient;
import tools.jackson.databind.JsonNode;

@Service
public class LanguagesService {

    private final LanguagesClient languagesClient;

    public LanguagesService(LanguagesClient languagesClient) {
        this.languagesClient = languagesClient;
    }

    public JsonNode getLanguages() {
        return languagesClient.getLanguages();
    }

    public JsonNode getLanguage(String lang){
        return languagesClient.getLanguage(lang);
    }
}