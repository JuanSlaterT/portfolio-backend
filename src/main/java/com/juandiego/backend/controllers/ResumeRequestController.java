package com.juandiego.backend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juandiego.backend.services.ResumeRequestService;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/resume-request")
public class ResumeRequestController {

    private final ResumeRequestService resumeRequestService;

    public ResumeRequestController(ResumeRequestService resumeRequestService) {
        this.resumeRequestService = resumeRequestService;
    }

    @PostMapping({"", "/"})
    public JsonNode createResumeRequest(@RequestBody JsonNode request) {
        return resumeRequestService.createResumeRequest(request);
    }
}
