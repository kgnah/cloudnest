package io.github.kgnah.cloudnest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class CreateDeploymentScheduleRequest {

    @NotBlank
    @Pattern(regexp = "START|STOP", message = "Action must be START or STOP")
    private String action;

    @NotNull
    private LocalDateTime scheduledAt;

    // constructor
    public CreateDeploymentScheduleRequest() {}

    // getters & setters
    public String getAction() {return action;}
    public void setAction(String action) {this.action = action;}

    public LocalDateTime getScheduledAt() {return scheduledAt;}
    public void setScheduledAt(LocalDateTime scheduledAt) {this.scheduledAt = scheduledAt;}
}
