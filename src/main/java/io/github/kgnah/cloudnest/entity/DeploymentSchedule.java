package io.github.kgnah.cloudnest.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class DeploymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private LocalDateTime scheduledAt;

    @ManyToOne
    @JoinColumn(name = "deployment_id", nullable = false)
    private Deployment deployment;

    public DeploymentSchedule() {}

    public DeploymentSchedule(String action, LocalDateTime scheduledAt, Deployment deployment) {
        this.action = action;
        this.scheduledAt = scheduledAt;
        this.deployment = deployment;
    }

    // getters and setters
    public Long getId() {return id;}

    public String getAction() {return action;}

    public LocalDateTime getScheduledAt() {return scheduledAt;}

    public Deployment getDeployment() {return deployment;}
    public void setDeployment(Deployment deployment) {this.deployment = deployment;}
}
