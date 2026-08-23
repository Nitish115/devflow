package com.devflow.devflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TaskStatusChangedEvent implements Serializable {

    private Long taskId;
    private Long projectId;
    private Long organizationId;
    private String taskTitle;
    private String oldStatus;
    private String newStatus;
    private Long updatedById;
    private String updatedByEmail;
    private Long assigneeId;
    private Long taskCreatorId;
    private LocalDateTime timestamp;

    public TaskStatusChangedEvent() {
    }

    public TaskStatusChangedEvent(Long taskId, Long projectId, Long organizationId, String taskTitle,
                                  String oldStatus, String newStatus, Long updatedById, String updatedByEmail,
                                  Long assigneeId, Long taskCreatorId) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.taskTitle = taskTitle;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updatedById = updatedById;
        this.updatedByEmail = updatedByEmail;
        this.assigneeId = assigneeId;
        this.taskCreatorId = taskCreatorId;
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

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public String getUpdatedByEmail() {
        return updatedByEmail;
    }

    public void setUpdatedByEmail(String updatedByEmail) {
        this.updatedByEmail = updatedByEmail;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getTaskCreatorId() {
        return taskCreatorId;
    }

    public void setTaskCreatorId(Long taskCreatorId) {
        this.taskCreatorId = taskCreatorId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
