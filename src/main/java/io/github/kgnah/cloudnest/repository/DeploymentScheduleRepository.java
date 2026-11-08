package io.github.kgnah.cloudnest.repository;

import io.github.kgnah.cloudnest.entity.DeploymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DeploymentScheduleRepository extends JpaRepository<DeploymentSchedule, Long> {

    List<DeploymentSchedule> findByScheduledAtLessThanEqual(LocalDateTime now);

    List<DeploymentSchedule> findByDeploymentId(String deploymentId);
}
