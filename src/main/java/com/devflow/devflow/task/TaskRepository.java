package com.devflow.devflow.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProjectId(Long projectId);
    List<Task> findAllByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findAllByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
    List<Task> findAllByProjectIdAndStatusAndAssigneeId(Long projectId, TaskStatus status, Long assigneeId);
    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
