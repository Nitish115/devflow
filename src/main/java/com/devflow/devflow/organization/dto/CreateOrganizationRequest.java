package com.devflow.devflow.organization.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String description;

    public CreateOrganizationRequest() {
    }

    public CreateOrganizationRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
