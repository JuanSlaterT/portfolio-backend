package com.juandiego.backend.services;

import org.springframework.stereotype.Service;

import com.juandiego.backend.clients.ResumeRequestClient;

import tools.jackson.databind.JsonNode;

@Service
public class ResumeRequestService {

    private final ResumeRequestClient resumeRequestClient;

    public ResumeRequestService(ResumeRequestClient resumeRequestClient) {
        this.resumeRequestClient = resumeRequestClient;
    }

    public JsonNode createResumeRequest(JsonNode request) {
        return resumeRequestClient.createResumeRequest(request);
    }
}
