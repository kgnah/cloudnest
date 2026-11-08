package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.CreateDeploymentRequest;
import io.github.kgnah.cloudnest.dto.DeploymentResponse;
import io.github.kgnah.cloudnest.service.DeploymentService;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/deployments")
public class DeploymentController {
    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping
    public DeploymentResponse createDeployment(@Valid @RequestBody CreateDeploymentRequest request, Authentication authentication) {
        return deploymentService.createDeployment(request, authentication.getName());
    }

    @GetMapping
    public List <DeploymentResponse> getDeployments(Authentication authentication) {
        return deploymentService.getDeployments(authentication.getName());
    }

    // handle specific deployments
    @GetMapping("/{id}")
    public DeploymentResponse getDeploymentById(@PathVariable String id, Authentication authentication) {
        return deploymentService.getDeploymentById(id, authentication.getName());
    }

    // delete deployment
    @DeleteMapping("/{id}")
    public void deleteDeployment(@PathVariable String id, Authentication authentication) {

        deploymentService.deleteDeployment(id, authentication.getName());
    }

    // start deployment
    @PostMapping("/{id}/start")
    public void startDeployment(@PathVariable String id, Authentication authentication) {
        deploymentService.startDeployment(id, authentication.getName());
    }

    // stop deployment
    @PostMapping("/{id}/stop")
    public void stopDeployment(@PathVariable String id, Authentication authentication) {
        deploymentService.stopDeployment(id, authentication.getName());
    }

    // get deployment logs
    @GetMapping("/{id}/logs")
    public String getDeploymentLogs(@PathVariable String id, Authentication authentication) {
        return deploymentService.getDeploymentLogs(id, authentication.getName());
    }

    @PutMapping("/{id}/refresh")
    public void refreshDeploymentStatus(@PathVariable String id, Authentication authentication) {
        deploymentService.refreshDeploymentStatus(id, authentication.getName());
    }
}
