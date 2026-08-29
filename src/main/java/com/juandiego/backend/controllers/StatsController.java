package com.juandiego.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juandiego.backend.services.StatsService;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping({"", "/"})
    public JsonNode getGameStats() {
        return statsService.getPlayerStats();
    }
}