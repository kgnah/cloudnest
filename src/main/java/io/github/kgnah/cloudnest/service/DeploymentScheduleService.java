package io.github.kgnah.cloudnest.service;

import io.github.kgnah.cloudnest.dto.CreateDeploymentScheduleRequest;
import io.github.kgnah.cloudnest.entity.Deployment;
import io.github.kgnah.cloudnest.entity.DeploymentSchedule;
import io.github.kgnah.cloudnest.exception.DeploymentNotFoundException;
import io.github.kgnah.cloudnest.repository.DeploymentRepository;
import io.github.kgnah.cloudnest.repository.DeploymentScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeploymentScheduleService {

    private final DeploymentRepository deploymentRepository;
    private final DeploymentScheduleRepository scheduleRepository;

    public DeploymentScheduleService(DeploymentRepository deploymentRepository, DeploymentScheduleRepository scheduleRepository) {
        this.deploymentRepository = deploymentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // create schedule
    public DeploymentSchedule createSchedule(String deploymentId, String userId, CreateDeploymentScheduleRequest request) {

        // user's ownership checking
        Deployment deployment = deploymentRepository
                .findByIdAndUserId(deploymentId, userId)
                .orElseThrow(() -> new DeploymentNotFoundException(
                        "Deployment not found with this id " + deploymentId));

        DeploymentSchedule schedule = new DeploymentSchedule(
                request.getAction(),
                request.getScheduledAt(),
                deployment);

        // request must be created for future use, not in the past
        if (!request.getScheduledAt().isAfter((LocalDateTime.now()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Scheduled time must be in the future");
        }

        // save schedule
        return scheduleRepository.save(schedule);
    }

    // get schedules
    public List<DeploymentSchedule> getSchedules(String deploymentId, String userId) {

        Deployment deployment = deploymentRepository
                .findByIdAndUserId(deploymentId, userId)
                .orElseThrow(() -> new DeploymentNotFoundException(
                        "Deployment not found with this id " + deploymentId
                ));

        return scheduleRepository.findByDeploymentId(deployment.getId());
    }

    // delete schedule
    public void deleteSchedule(String deploymentId, Long scheduleId, String userId) {

        Deployment deployment = deploymentRepository
                .findByIdAndUserId(deploymentId, userId)
                .orElseThrow(() -> new DeploymentNotFoundException("Deployment not found with this id " + deploymentId));

        DeploymentSchedule schedule = scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new DeploymentNotFoundException("Schedule not found with this id " + scheduleId));

        if (!schedule.getDeployment().getId().equals((deployment.getId()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Schedule does not belong to this deployment");
        }

        scheduleRepository.delete(schedule);
    }
}
