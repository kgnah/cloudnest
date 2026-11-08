package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.service.RepositoryService;
import io.github.kgnah.cloudnest.service.DockerService;
import io.github.kgnah.cloudnest.dto.BuildRepositoryRequest;

import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final DockerService dockerService;

    public RepositoryController(RepositoryService repositoryService, DockerService dockerService) {
        this.repositoryService = repositoryService;
        this.dockerService = dockerService;
    }

    @PostMapping("/build")
    public String buildRepository(@RequestBody BuildRepositoryRequest request) {

        // Step 1: clone repository
        Path repositoryPath = repositoryService.cloneRepository(request.getRepositoryUrl());

        // Step 2: build docker image
        String imageId = dockerService.buildImage(repositoryPath.toString(), request.getImageName());

       return "Repository cloned sucessfully.\n"
               + "Repository path: " + repositoryPath + "\n"
               + "Docker image built successfully.\n"
               + "Image ID: " + imageId;
    }
}
