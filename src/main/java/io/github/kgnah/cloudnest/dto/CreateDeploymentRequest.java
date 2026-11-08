package io.github.kgnah.cloudnest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateDeploymentRequest {

    @NotBlank
    private String name;

    private String image;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer containerPort;

    private String repositoryUrl;

    public CreateDeploymentRequest() {
    }

    // getters and setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public Integer getContainerPort() {return containerPort;}
    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public String getRepositoryUrl() {return repositoryUrl;}
    public void setRepositoryUrl(String repositoryUrl) {this.repositoryUrl = repositoryUrl;}
}
