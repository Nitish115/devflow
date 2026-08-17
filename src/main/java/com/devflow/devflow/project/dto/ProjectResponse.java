package com.devflow.devflow.project.dto;

import com.devflow.devflow.project.ProjectStatus;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private ProjectStatus status;
    private Long createdById;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, String name, String description, Long organizationId,
                           ProjectStatus status, Long createdById, String createdByEmail,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.status = status;
        this.createdById = createdById;
        this.createdByEmail = createdByEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
