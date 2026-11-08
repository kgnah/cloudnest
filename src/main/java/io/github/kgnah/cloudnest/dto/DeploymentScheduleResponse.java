package io.github.kgnah.cloudnest.dto;

import java.time.LocalDateTime;

public class DeploymentScheduleResponse {

    private Long id;
    private String action;
    private LocalDateTime scheduledAt;
    private String deploymentId;

    public DeploymentScheduleResponse(Long id, String action, LocalDateTime scheduledAt, String deploymentId) {
        this.id = id;
        this.action = action;
        this.scheduledAt = scheduledAt;
        this.deploymentId = deploymentId;
    }

    // getters
    public Long getId() {return id;}

    public String getAction() {return action;}

    public LocalDateTime getScheduledAt() {return scheduledAt;}

    public String getDeploymentId() {return deploymentId;}
}
