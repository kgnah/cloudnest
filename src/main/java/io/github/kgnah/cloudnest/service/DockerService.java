package io.github.kgnah.cloudnest.service;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import io.github.kgnah.cloudnest.dto.ContainerCreationResult;
import io.github.kgnah.cloudnest.dto.ImageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.springframework.web.server.ResponseStatusException;

import javax.sound.sampled.Port;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    // path: DefaultDockerClientConfig -> ApacheDockerHttpClient
    // ->DockerClientImpl -> Docker Engine
    public DockerService() {

        DefaultDockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("npipe:////./pipe/dockerDesktopLinuxEngine")
                        .build();

        ApacheDockerHttpClient httpClient =
                new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost()).build();

        dockerClient = com.github.dockerjava.core.DockerClientImpl
                .getInstance(config, httpClient);
    }

    // create container
    public ContainerCreationResult createContainer(String image, String name, Integer containerPort) {
        // host port
        int hostPort = findAvailablePort();

        // expose port and port binding
        ExposedPort exposedPort = ExposedPort.tcp(containerPort);
        PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(hostPort), exposedPort);

        // create container response
        CreateContainerResponse container = dockerClient
                .createContainerCmd(image)
                .withName(name).withExposedPorts(exposedPort)
                .withHostConfig(HostConfig.newHostConfig()
                        .withPortBindings(portBinding)).exec();

        // start container
        dockerClient.startContainerCmd(container.getId()).exec();

        // return container id and host port
        return new ContainerCreationResult(container.getId(), hostPort);
    }

    // create container using a specific host port
    public ContainerCreationResult createContainer(String image, String name, Integer containerPort, Integer hostPort) {
        ExposedPort exposedPort = ExposedPort.tcp(containerPort);

        PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(hostPort), exposedPort);

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withName(name).withExposedPorts().withHostConfig(
                        HostConfig.newHostConfig().withPortBindings(portBinding)).exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        return new ContainerCreationResult(container.getId(), hostPort);
    }


    // delete container
    public void deleteContainer(String containerId) {
        // inspect the container: if exist + running => stop and remove
        // exist + stopped => remove only
        // not exist => already deleted, continue
        try {
            InspectContainerResponse response =
                    dockerClient.inspectContainerCmd(containerId).exec();

            String status = response.getState().getStatus();

            if ("RUNNING".equalsIgnoreCase(status)) {
                dockerClient.stopContainerCmd(containerId).exec();
            }

            dockerClient.removeContainerCmd(containerId).exec();
        }
        catch (NotFoundException e) {
            // the container is already gone, so deletion can continue
        }
    }

    // get container status
    public String getContainerStatus(String containerId) {
        try {
            // inspect container
            InspectContainerResponse response =
                    dockerClient.inspectContainerCmd(containerId).exec();

            return response.getState().getStatus().toUpperCase();
        }
        catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such container is found", e);
        }
    }

    // start container
    public void startContainer(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();

            String status = response.getState().getStatus();

            if (!"RUNNING".equalsIgnoreCase(status)) {
                dockerClient.startContainerCmd(containerId).exec();
            }
        }
        catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No such container found", e);
        }
    }

    // stop container
    public void stopContainer(String containerId){
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();

            String status = response.getState().getStatus();

            if ("RUNNING".equalsIgnoreCase(status)) {
                dockerClient.stopContainerCmd(containerId).exec();
            }
        }
        catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No such container found", e);
        }
    }

    // get container logs
    public String getContainerLogs(String containerId) {

        StringBuilder logs = new StringBuilder();

        try{
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTimestamps(true)
                    .exec(new LogContainerResultCallback() {

                        @Override
                        public void onNext(Frame frame) {
                            // get text from frame
                            // append to logs
                            logs.append(new String (frame.getPayload(), StandardCharsets.UTF_8));
                        }
                    }).awaitCompletion(); // blocks until all log frames are processed
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Docker logs", e);
        }

        return logs.toString();
    }

    // checking image
    public boolean imageExists(String image) {
        try {
            dockerClient.inspectImageCmd(image).exec();
            return true;
        }
        catch (NotFoundException e) {
            return false;
        }
    }

    // pull image
    // docker cli equivalent: docker pull nginx:latest
    // image pulling is handled by call back, exec alone is not enough
    public void pullImage(String image) {
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion();
        }
        // java thread interruption
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while pulling image", e);
        }
        // docker java/client operation failed
        catch (DockerClientException e) {
            throw new RuntimeException("Failed to pull Docker image: " + image, e);
        }
    }

    // build Docker image from a Dockerfile
    public String buildImage(String repositoryPath, String imageName) {

        File buildContext = new File(repositoryPath);

        if (!buildContext.exists() || !buildContext.isDirectory()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Repository directory does not exist: " + repositoryPath);
        }

        File dockerfile = new File(buildContext, "Dockerfile");

        if (!dockerfile.exists()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dockerfile not found in repository");
        }

        try {
            return dockerClient.buildImageCmd(buildContext)
                    .withDockerfile(dockerfile)
                    .withTags(java.util.Set.of(imageName))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
        }
        catch (DockerClientException e) {
            throw new RuntimeException("Failed to build Docker image: " + imageName, e);
        }
    }

    // list image
    public List<ImageResponse> listImages() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        return images.stream().filter(image -> image.getRepoTags().length > 0)
                .map(image -> {

            String repoTag = image.getRepoTags()[0];

            String[] parts = repoTag.split(":");

            String repository = parts[0];
            String tag = parts[1];

            return new ImageResponse(image.getId(), repository, tag, image.getSize());

        }).toList();
    }

    // remove image
    public void deleteImage(String imageId) {
        if(isImageInUse(imageId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Image is being used by a container");
        }
        try {
            dockerClient.removeImageCmd(imageId).exec();
        }
        catch (NotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No such image is found");
        }
    }

    // container in use
    public boolean isImageInUse(String imageId) {
        // get containers
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();

        // check each container's image
        for (Container container : containers) {

            InspectImageResponse image = dockerClient.inspectImageCmd(container.getImage()).exec();

            if (image.getId().equals(imageId)) {
                return true;
            }
        }
        return false;
    }

    // find available port
    public int findAvailablePort() {
        int port = 8081;

        while (true) {
            try (ServerSocket socket = new ServerSocket(port)){
                return port;
            }
            catch (IOException e) {
                port++;
            }
        }
    }
}
