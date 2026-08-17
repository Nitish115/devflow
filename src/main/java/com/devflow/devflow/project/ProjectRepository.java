package com.devflow.devflow.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrganizationId(Long organizationId);
    Optional<Project> findByIdAndOrganizationId(Long id, Long organizationId);
}
