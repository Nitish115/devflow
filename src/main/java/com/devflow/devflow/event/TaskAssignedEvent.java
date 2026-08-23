package com.devflow.devflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TaskAssignedEvent implements Serializable {

    private Long taskId;
    private Long projectId;
    private Long organizationId;
    private String taskTitle;
    private Long assigneeId;
    private Long assignedById;
    private String assignedByEmail;
    private LocalDateTime timestamp;

    public TaskAssignedEvent() {
    }

    public TaskAssignedEvent(Long taskId, Long projectId, Long organizationId, String taskTitle,
                             Long assigneeId, Long assignedById, String assignedByEmail) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.taskTitle = taskTitle;
        this.assigneeId = assigneeId;
        this.assignedById = assignedById;
        this.assignedByEmail = assignedByEmail;
        this.timestamp = LocalDateTime.now();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }

    public String getAssignedByEmail() {
        return assignedByEmail;
    }

    public void setAssignedByEmail(String assignedByEmail) {
        this.assignedByEmail = assignedByEmail;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
