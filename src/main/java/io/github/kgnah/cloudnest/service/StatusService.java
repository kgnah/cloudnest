package io.github.kgnah.cloudnest.service;


import io.github.kgnah.cloudnest.dto.StatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class StatusService {

    // spring reads YAML and injects the value
    @Value("${cloudnest.message}")
    private String message;

    @Value("${cloudnest.version}")
    private String version;

    @Value("${cloudnest.status}")
    private String status;

    public StatusResponse getStatus() {
        return new StatusResponse(message, version, status);
    }
}
