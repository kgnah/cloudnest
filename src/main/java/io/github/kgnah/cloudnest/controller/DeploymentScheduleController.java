package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.CreateDeploymentScheduleRequest;
import io.github.kgnah.cloudnest.dto.DeploymentScheduleResponse;
import io.github.kgnah.cloudnest.entity.DeploymentSchedule;
import io.github.kgnah.cloudnest.service.DeploymentScheduleService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deployments")
public class DeploymentScheduleController {

    private final DeploymentScheduleService scheduleService;

    public DeploymentScheduleController(DeploymentScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/{deploymentId}/schedules")
    public DeploymentScheduleResponse createSchedule(@PathVariable String deploymentId, @Valid @RequestBody CreateDeploymentScheduleRequest request, Authentication authentication) {
        DeploymentSchedule schedule = scheduleService.createSchedule(deploymentId, authentication.getName(), request);

        return new DeploymentScheduleResponse(
                schedule.getId(),
                schedule.getAction(),
                schedule.getScheduledAt(),
                schedule.getDeployment().getId());
    }

    @GetMapping("/{deploymentId}/schedules")
    public List<DeploymentScheduleResponse> getSchedules(@PathVariable String deploymentId, Authentication authentication) {

        List<DeploymentSchedule> schedules = scheduleService.getSchedules(deploymentId, authentication.getName());

        return schedules.stream().map(
                schedule -> new DeploymentScheduleResponse(
                        schedule.getId(),
                        schedule.getAction(),
                        schedule.getScheduledAt(),
                        schedule.getDeployment().getId())).toList();
    }

    @DeleteMapping("/{deploymentId}/schedules/{scheduleId}")
    public void deleteSchedule(@PathVariable String deploymentId, @PathVariable Long scheduleId, Authentication authentication) {

        scheduleService.deleteSchedule(deploymentId, scheduleId, authentication.getName());
    }
}
