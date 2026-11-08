package io.github.kgnah.cloudnest.service;

import io.github.kgnah.cloudnest.entity.Deployment;
import io.github.kgnah.cloudnest.entity.DeploymentSchedule;
import io.github.kgnah.cloudnest.entity.DeploymentStatus;

import io.github.kgnah.cloudnest.repository.DeploymentRepository;
import io.github.kgnah.cloudnest.repository.DeploymentScheduleRepository;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private final DeploymentScheduleRepository scheduleRepository;
    private final DockerService dockerService;
    private final DeploymentRepository deploymentRepository;

    public SchedulerService(DeploymentScheduleRepository scheduleRepository, DockerService dockerService, DeploymentRepository deploymentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.dockerService = dockerService;
        this.deploymentRepository = deploymentRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void checkSchedules() {

        LocalDateTime now = LocalDateTime.now();

        List<DeploymentSchedule> schedules = scheduleRepository.findByScheduledAtLessThanEqual(now);

        for (DeploymentSchedule schedule : schedules) {
            Deployment deployment = schedule.getDeployment();

            System.out.println("Executing scheduled action: " + schedule.getAction() + " for deployment " + deployment.getId());

            if ("START".equalsIgnoreCase(schedule.getAction())) {
                dockerService.startContainer((deployment.getContainerId()));

                deployment.setStatus(DeploymentStatus.RUNNING);
            }
            else if ("STOP".equalsIgnoreCase(schedule.getAction())) {
                dockerService.stopContainer(deployment.getContainerId());

                deployment.setStatus(DeploymentStatus.EXITED);
            }
            else {
                System.out.println("Unknown scheduled action: " + schedule.getAction());
                continue;
            }

            // update the database
            deploymentRepository.save(deployment);
            scheduleRepository.delete(schedule);
        }
    }

}
