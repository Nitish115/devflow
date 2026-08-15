package com.devflow.devflow.organization.dto;

import com.devflow.devflow.auth.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    public UpdateMemberRoleRequest() {
    }

    public UpdateMemberRoleRequest(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
