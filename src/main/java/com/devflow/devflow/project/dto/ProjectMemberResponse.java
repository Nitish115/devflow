package com.devflow.devflow.project.dto;

import com.devflow.devflow.auth.Role;
import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private Long id;
    private Long projectId;
    private Long userId;
    private String userEmail;
    private Role role;
    private LocalDateTime createdAt;

    public ProjectMemberResponse() {
    }

    public ProjectMemberResponse(Long id, Long projectId, Long userId, String userEmail, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
