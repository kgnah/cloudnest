package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.service.StatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kgnah.cloudnest.dto.StatusResponse;

// http handler class
@RestController
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    // mapping url
    @GetMapping("/status")
    public StatusResponse getStatus() {
        return statusService.getStatus();
    }
}
