package com.devflow.devflow.project.dto;

import com.devflow.devflow.auth.Role;
import jakarta.validation.constraints.NotNull;

public class AddProjectMemberRequest {

    private Long userId;

    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    public AddProjectMemberRequest() {
    }

    public AddProjectMemberRequest(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public AddProjectMemberRequest(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    public AddProjectMemberRequest(Long userId, String email, Role role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
