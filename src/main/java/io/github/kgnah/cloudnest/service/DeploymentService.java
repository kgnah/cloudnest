package io.github.kgnah.cloudnest.service;

import com.github.dockerjava.api.exception.NotFoundException;

import io.github.kgnah.cloudnest.dto.ContainerCreationResult;
import io.github.kgnah.cloudnest.dto.CreateDeploymentRequest;
import io.github.kgnah.cloudnest.dto.DeploymentResponse;

import io.github.kgnah.cloudnest.entity.Deployment;
import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.entity.DeploymentStatus;

import io.github.kgnah.cloudnest.repository.DeploymentRepository;
import io.github.kgnah.cloudnest.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

@Service
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final DockerService dockerService;
    private final UserRepository userRepository;
    private final RepositoryService repositoryService;

    private static final Logger log = Logger.getLogger(DeploymentService.class.getName());

    // constructor
    public DeploymentService(DeploymentRepository deploymentRepository, DockerService dockerService, UserRepository userRepository, RepositoryService repositoryService) {
        this.deploymentRepository = deploymentRepository;
        this.dockerService = dockerService;
        this.userRepository = userRepository;
        this.repositoryService = repositoryService;
    }

    // create deployment
    public DeploymentResponse createDeployment(CreateDeploymentRequest request, String userId) {

        // check if the image exists
        boolean hasImage = request.getImage() != null && !request.getImage().isBlank();

        boolean hasRepository = request.getRepositoryUrl() != null && !request.getRepositoryUrl().isBlank();

        if (hasImage == hasRepository) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide either an image or a repository url, but not both");
        }

        // generating the deployment's ID
        String id = UUID.randomUUID().toString();

        // time
        LocalDateTime createdAt = LocalDateTime.now();

        // user
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));

        // determine which docker image this deployment will use
        String image = hasImage ? request.getImage() : null;

        // create the deployment immediately so we can track its lifecycle
        Deployment deployment = new Deployment(
                id,
                request.getName(),
                image,
                DeploymentStatus.CREATING,
                createdAt,
                null,
                null,
                request.getContainerPort(),
                user,
                request.getRepositoryUrl());

        // save the initial state
        deploymentRepository.save(deployment);

        try {
            if (hasRepository) {

                // update deployment status: building the application from the repository
                deployment.setStatus(DeploymentStatus.BUILDING);
                deploymentRepository.save(deployment);

                // clone the source repository
                Path repositoryPath = repositoryService.cloneRepository(request.getRepositoryUrl());

                try {
                    // generate an image name from the deployment name
                    String imageTag = request.getName()
                            .toLowerCase()
                            .replaceAll("[^a-z0-9._-]", "-")
                            .replaceAll("^[._-]+|[._-]+$", "");

                    if (imageTag.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment name cannot produce a valid image name");
                    }

                    image = "cloudnest-" + imageTag + ":latest";

                    // build docker image from the clone repository
                    dockerService.buildImage(repositoryPath.toString(), image);
                }

                // finally executes whether the code in try succeeds or throws an exception
                finally {
                    // remove the temporary cloned repository
                    repositoryService.deleteRepository(repositoryPath);
                }
            }
            else {
                // use the image supplied by the user
                image = request.getImage();

                boolean imageExists = dockerService.imageExists(image);

                if (!imageExists) {
                    try {
                        dockerService.pullImage(image);
                    }
                    catch (NotFoundException e) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Docker image not found: " + image);
                    }
                }
            }

            // container id
            ContainerCreationResult result = dockerService.createContainer(
                    image,
                    request.getName(),
                    request.getContainerPort());

            // update deployment after the container was successfully created
            deployment.setImage(image);
            deployment.setContainerId(result.getContainerId());
            deployment.setHostPort(result.getHostPort());
            deployment.setStatus(DeploymentStatus.RUNNING);

            // url
            String url = buildDeploymentUrl(deployment.getHostPort());

            // save updated deployment
            deploymentRepository.save(deployment);

            // return dto
            return new DeploymentResponse(
                    deployment.getId(),
                    deployment.getName(),
                    deployment.getImage(),
                    deployment.getStatus().name(),
                    deployment.getCreatedAt(),
                    result.getContainerId(),
                    result.getHostPort(),
                    deployment.getContainerPort(),
                    url,
                    deployment.getRepositoryUrl());
        }
        catch (Exception e) {

            // mark deployment as failed
            deployment.setStatus(DeploymentStatus.FAILED);
            deploymentRepository.save(deployment);

            // rethrow the original exception
            throw e;
        }
    }

    // get deployment value
    public List<DeploymentResponse> getDeployments(String userId) {
        // return the deployment list
        return deploymentRepository.findAllByUserId(userId)
                .stream()
                .map(deployment -> {

                    // start with the status stored in the database
                    DeploymentStatus currentStatus = deployment.getStatus();

                // handle container is manually deleted scenario, but the deployment still exist
                // status -> missing
                if (deployment.getContainerId() != null) {
                    try {
                        // get the actual status from Docker
                        String dockerStatus = dockerService.getContainerStatus(deployment.getContainerId());

                        // safely map Docker status to our enum
                        currentStatus = mapDockerStatus(dockerStatus);
                    }
                    catch (ResponseStatusException e) {
                        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                            currentStatus = DeploymentStatus.MISSING;
                        }
                        else {
                            throw e;
                        }
                    }
                }

                // synchronize the database if the status changed
                if (deployment.getStatus() != currentStatus) {
                    deployment.setStatus(currentStatus);
                    deploymentRepository.save(deployment);
                }

                // url
                String url = buildDeploymentUrl(deployment.getHostPort());

        return new DeploymentResponse(
                        deployment.getId(),
                        deployment.getName(),
                        deployment.getImage(),
                        currentStatus.name(),
                        deployment.getCreatedAt(),
                        deployment.getContainerId(),
                        deployment.getHostPort(),
                        deployment.getContainerPort(),
                        url,
                        deployment.getRepositoryUrl());
                })
                .toList();
    }

    // get deployment by id
    public DeploymentResponse getDeploymentById(String id, String userId) {
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        // start with the status stored in the database
        DeploymentStatus currentStatus = deployment.getStatus();

        if (deployment.getContainerId() != null) {
            try {
                // get the actual status from Docker
                String dockerStatus = dockerService.getContainerStatus(deployment.getContainerId());

                // safely map Docker status to our enum
                currentStatus = mapDockerStatus(dockerStatus);
            }
            catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    currentStatus = DeploymentStatus.MISSING;
                }
                else {
                    throw e;
                }
            }
        }

        // synchronize the database if the status changed
        if (deployment.getStatus() != currentStatus) {
            deployment.setStatus(currentStatus);
            deploymentRepository.save(deployment);
        }

        // url
        String url = buildDeploymentUrl(deployment.getHostPort());

        return new DeploymentResponse(
                deployment.getId(),
                deployment.getName(),
                deployment.getImage(),
                currentStatus.name(),
                deployment.getCreatedAt(),
                deployment.getContainerId(),
                deployment.getHostPort(),
                deployment.getContainerPort(),
                url,
                deployment.getRepositoryUrl());
    }

    // delete deployment
    public void deleteDeployment(String id, String userId) {

        // handle error
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        // delete docker container
        // old deployments didn't have container id
        if (deployment.getContainerId() != null) {
            dockerService.deleteContainer(deployment.getContainerId());
        }

        // delete database record
        deploymentRepository.deleteById(id);
    }

    // start deployment
    public void startDeployment(String id, String userId) {
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        if (deployment.getContainerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment has no container");
        }

        String status = dockerService.getContainerStatus(deployment.getContainerId());

        if ("RUNNING".equals(status)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Deployment is already running");
        }

        dockerService.startContainer(deployment.getContainerId());

        // set status and save it
        deployment.setStatus(DeploymentStatus.RUNNING);
        deploymentRepository.save(deployment);
    }

    // stop deployment
    public void stopDeployment(String id, String userId) {
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        if (deployment.getContainerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment has no container");
        }

        String status = dockerService.getContainerStatus(deployment.getContainerId());

        if (!"RUNNING".equals(status)) {
            throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Deployment is not running");
        }

        // stop the container
        dockerService.stopContainer(deployment.getContainerId());

        // update the status in the database
        deployment.setStatus(DeploymentStatus.EXITED);
        deploymentRepository.save(deployment);
    }

    // get deployment logs
    public String getDeploymentLogs(String id, String userId) {
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        if (deployment.getContainerId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deployment has no container");
        }

        return dockerService.getContainerLogs(deployment.getContainerId());
    }

    // refresh deployment status
    public void refreshDeploymentStatus(String id, String userId) {
        // handling error
        Deployment deployment = deploymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deployment not found with this id " + id));

        if (deployment.getContainerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment has no container");
        }

        // get container status
        String status = dockerService.getContainerStatus(deployment.getContainerId());

        // convert Docker status Sting to DeploymentStatus enum
        DeploymentStatus deploymentStatus = mapDockerStatus(status);

        // set status and save entity
        deployment.setStatus(deploymentStatus);
        deploymentRepository.save(deployment);
    }

    // base url
    @Value("${cloudnest.base-url}")
    private String baseUrl;

    // build deployment url
    private String buildDeploymentUrl(Integer hostPort) {
        if (hostPort == null) {
            return null;
        }
        return baseUrl + ":" + hostPort;
    }

    // rebuild docker image from the latest repository code
    public String rebuildDeploymentImage(Deployment deployment) {

        // check whether this deployment was created from a git repository
        if (deployment.getRepositoryUrl() == null || deployment.getRepositoryUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment does not have a Git repository");
        }

        // clone the latest repository code
        Path repositoryPath = repositoryService.cloneRepository((deployment.getRepositoryUrl()));

        try {
            // generate the image name using the deployment name
            String imageTag = deployment.getName()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9._-]", "-")
                    .replaceAll("^[._-]+|[._-]+$", "");

            if (imageTag.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment name cannot product a valid image name");
            }

            String image = "cloudnest-" + imageTag + ":latest";

            // build the new docker image
            dockerService.buildImage(repositoryPath.toString(), image);

            return image;
        }
        finally {
            try {
                // always clean up the temporary repository
                repositoryService.deleteRepository(repositoryPath);
            }
            catch (RuntimeException e) {
                System.err.println("WARNING: Could not clean temporary repository: " + repositoryPath);
                e.printStackTrace();
            }
        }
    }

    // redeploy container
    public void redeployContainer(Deployment deployment, String newImage) {

        if (deployment.getContainerPort() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Deployment does not have a container port configured");
        }

        // 1. remember the old container information
        String oldImage = deployment.getImage();
        String oldContainerId = deployment.getContainerId();
        Integer oldHostPort = deployment.getHostPort();

        // 2. generate a unique name for the replacement
        String newContainerName = deployment.getName() + "-" + System.currentTimeMillis();

        ContainerCreationResult result = null;

        try {
            // 3. create and start the replacement container
             result = dockerService.createContainer(
                    newImage,
                    newContainerName,
                    deployment.getContainerPort());

            // 4. verify that the replacement is running
            String newStatus = dockerService.getContainerStatus(result.getContainerId());

            if (!"RUNNING".equalsIgnoreCase(newStatus)) {
                throw new RuntimeException("Replacement container failed to start. Status: " + newStatus);
            }

            // 5. update deployment information
            deployment.setImage(newImage);
            deployment.setContainerId(result.getContainerId());
            deployment.setHostPort(result.getHostPort());
            deployment.setStatus(DeploymentStatus.RUNNING);

            deploymentRepository.save(deployment);
        }
        catch (Exception e) {

            // restore old deployment information in memory
            deployment.setImage(oldImage);
            deployment.setContainerId(oldContainerId);
            deployment.setHostPort(oldHostPort);

            // remove the replacement if it was created
            if (result != null) {
                try {
                    dockerService.deleteContainer(result.getContainerId());
                }
                catch (Exception cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            }

            throw new RuntimeException("Failed to redeploy. Old deployment remains unchange.", e);
        }

        // remove the old container after successful replacement
        if (oldContainerId != null && !oldContainerId.equals(result.getContainerId())) {
            try {
                dockerService.deleteContainer(oldContainerId);
            }
            catch (Exception e) {
                // the new deployment is already active
                // log the error so the old container can be cleaned up later
                log.log(
                        Level.WARNING, "Failed to remove old container: " + oldContainerId, e);
            }
        }
    }

    private String normalizeRepositoryUrl(String url) {

        if (url == null) {
            return null;
        }

        return url
                .trim()
                .replaceAll("/+$", "")
                .replaceAll("\\.git$", "");
    }

    // find by repository url
    public List<Deployment> findAllByRepositoryUrl(String repositoryUrl) {

        String normalizeUrl = normalizeRepositoryUrl(repositoryUrl);

        List<Deployment> deployments = deploymentRepository.findAll()
                .stream()
                .filter(deployment -> deployment.getRepositoryUrl() != null &&
                        normalizeUrl.equals(normalizeRepositoryUrl((deployment.getRepositoryUrl()))))
                .toList();

        if (deployments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No deployment found for repository: " + repositoryUrl);
        }

        return deployments;
    }

    // find deployment by id
    public Deployment findById(String id) {
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Deployment not found with this id: " + id));
    }

    // safe mapping method
    private DeploymentStatus mapDockerStatus(String status) {

        if (status == null) {
            return DeploymentStatus.FAILED;
        }

        return switch (status.toUpperCase()) {
            case "RUNNING" -> DeploymentStatus.RUNNING;
            case "EXITED" -> DeploymentStatus.EXITED;
            case "CREATED" -> DeploymentStatus.CREATING;
            case "RESTARTING" -> DeploymentStatus.CREATING;
            case "DEAD" -> DeploymentStatus.FAILED;
            default -> DeploymentStatus.FAILED;
        };
    }
}