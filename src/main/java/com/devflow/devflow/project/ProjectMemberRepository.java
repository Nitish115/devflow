package com.devflow.devflow.project;

import com.devflow.devflow.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findAllByProjectId(Long projectId);
    long countByProjectIdAndRole(Long projectId, Role role);
}
