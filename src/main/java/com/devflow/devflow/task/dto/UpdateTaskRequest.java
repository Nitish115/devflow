package com.devflow.devflow.task.dto;

import com.devflow.devflow.task.TaskPriority;
import com.devflow.devflow.task.TaskStatus;
import java.time.LocalDate;

public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long assigneeId;
    private LocalDate dueDate;

    public UpdateTaskRequest() {
    }

    public UpdateTaskRequest(String title, String description, TaskStatus status,
                             TaskPriority priority, Long assigneeId, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
