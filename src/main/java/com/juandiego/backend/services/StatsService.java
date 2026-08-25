package com.juandiego.backend.services;

import org.springframework.stereotype.Service;

import com.juandiego.backend.clients.StatsClient;

import tools.jackson.databind.JsonNode;

@Service
public class StatsService {
    private final StatsClient statsClient;

    public StatsService(StatsClient statsClient){
        this.statsClient = statsClient;
    }

    public JsonNode getPlayerStats(){
        return statsClient.getStats();
    }
}
