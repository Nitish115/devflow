package com.devflow.devflow.organization.dto;

import com.devflow.devflow.auth.Role;
import java.time.LocalDateTime;

public class OrganizationMemberResponse {

    private Long id;
    private Long organizationId;
    private Long userId;
    private String userEmail;
    private Role role;
    private LocalDateTime createdAt;

    public OrganizationMemberResponse() {
    }

    public OrganizationMemberResponse(Long id, Long organizationId, Long userId, String userEmail, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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
