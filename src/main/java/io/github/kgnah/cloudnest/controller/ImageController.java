package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.ImageResponse;
import io.github.kgnah.cloudnest.service.DockerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final DockerService dockerService;

    public ImageController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping
    public List<ImageResponse> getImages() {
        return dockerService.listImages();
    }

    @DeleteMapping("/{id}")
    public void deleteImage(@PathVariable String id) {
        dockerService.deleteImage(id);
    }

    @GetMapping("{id}/in-use")
    public boolean isImageInUse(@PathVariable String id) {
        return dockerService.isImageInUse(id);
    }
}
