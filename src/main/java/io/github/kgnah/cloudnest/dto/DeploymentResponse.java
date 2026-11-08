package io.github.kgnah.cloudnest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class DeploymentResponse {

    private String id;
    private String name;
    private String image;
    private String status;
    private String containerId;
    private Integer hostPort;
    private String url;
    private String repositoryUrl;
    private Integer containerPort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public DeploymentResponse() {}

    public DeploymentResponse(String id, String name, String image, String status, LocalDateTime createdAt, String containerId, Integer hostPort, Integer containerPort, String url, String repositoryUrl) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.createdAt = createdAt;
        this.containerId = containerId;
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.url = url;
        this.repositoryUrl = repositoryUrl;
    }

    // getters
    public String getId() {return id;}

    public String getName() {return name;}

    public String getImage() {return image;}

    public String getStatus() {return status;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public String getContainerId() {return containerId;}

    public Integer getHostPort() {return hostPort;}

    public String getUrl() {return url;}

    public String getRepositoryUrl() {return repositoryUrl;}

    public Integer getContainerPort() {return containerPort;}
}

