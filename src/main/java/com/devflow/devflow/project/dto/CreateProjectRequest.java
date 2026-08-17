package com.devflow.devflow.project.dto;

import com.devflow.devflow.project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private ProjectStatus status;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = ProjectStatus.ACTIVE;
    }

    public CreateProjectRequest(String name, String description, ProjectStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
