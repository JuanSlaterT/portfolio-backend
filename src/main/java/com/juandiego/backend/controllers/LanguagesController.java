package com.juandiego.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.juandiego.backend.services.LanguagesService;

import tools.jackson.databind.JsonNode;


@RestController
@RequestMapping("/api/languages")
public class LanguagesController {

    private final LanguagesService languagesService;

    public LanguagesController(LanguagesService languagesService) {
        this.languagesService = languagesService;
    }

    @GetMapping({"", "/"})
    public JsonNode getLanguages() {
        return languagesService.getLanguages();
    }

    @GetMapping("/{language}")
    public JsonNode getLanguage(@PathVariable String language) {
        return languagesService.getLanguage(language);
    }
    
}