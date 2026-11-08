package io.github.kgnah.cloudnest.repository;

import io.github.kgnah.cloudnest.entity.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, String> {

    List<Deployment> findAllByUserId(String userId);

    Optional<Deployment> findByIdAndUserId(String id, String userId);

    Optional<Deployment> findByRepositoryUrl(String repositoryUrl);
}
