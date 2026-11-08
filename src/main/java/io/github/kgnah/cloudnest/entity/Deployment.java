package io.github.kgnah.cloudnest.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// entity means  this class represents a database table
@Entity
public class Deployment {

    // primary key of the database table
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private DeploymentStatus status;

    private String name;
    private String image;
    private LocalDateTime createdAt;
    private String containerId;
    private Integer hostPort; // use Integer to handle null instead of primitive int
    private String repositoryUrl;
    private Integer containerPort;

    // ownership
    @ManyToOne // many deployments can belong to one user
    @JoinColumn(name = "user_id") // temporary ignore the null = false to test
    private User user;

    // empty constructor: jpa uses this
    public Deployment() {
    }

    // application constructor: application uses this
    public Deployment(
            String id, String name, String image, DeploymentStatus status,
            LocalDateTime createdAt, String containerId, Integer hostPort, Integer containerPort,
            User user, String repositoryUrl) {

        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.createdAt = createdAt;
        this.containerId = containerId;
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.user = user;
        this.repositoryUrl = repositoryUrl;
    }

    // getters and setters
    public String getId() {return id;}

    public String getName() {return name;}

    public String getImage() {return image;}
    public void setImage(String image) {this.image = image;}


    public DeploymentStatus getStatus() {return status;}
    public void setStatus(DeploymentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() {return createdAt;}

    public String getContainerId() {return containerId;}
    public void setContainerId(String containerId) {this.containerId = containerId;}

    public Integer getHostPort() {return hostPort;}
    public void setHostPort(Integer hostPort) {this.hostPort = hostPort;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public String getRepositoryUrl() { return repositoryUrl;}
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl;}

    public Integer getContainerPort() {return containerPort;}
    public void setContainerPort(Integer containerPort) {this.containerPort = containerPort;}
}
