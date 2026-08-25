package com.devflow.devflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectTaskIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String devToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() throws Exception {
        ownerToken = registerAndLoginUser("pt.owner@example.com", "Password123!");
        devToken = registerAndLoginUser("pt.dev@example.com", "Password123!");
        outsiderToken = registerAndLoginUser("pt.outsider@example.com", "Password123!");
    }

    private String registerAndLoginUser(String email, String password) throws Exception {
        String registerJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "role": "DEVELOPER"
                }
                """, email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void testProjectTaskLifecycleAndNonMemberForbidden() throws Exception {
        // 1. Create Organization
        String createOrgJson = """
                {
                    "name": "Project Task Org",
                    "description": "Lifecycle Test Org"
                }
                """;

        String createOrgResponse = mockMvc.perform(post("/api/organizations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrgJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orgId = objectMapper.readTree(createOrgResponse).get("id").asLong();

        // Add dev and outsider to organization
        mockMvc.perform(post("/api/organizations/" + orgId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\": \"pt.dev@example.com\", \"role\": \"DEVELOPER\"}")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/organizations/" + orgId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\": \"pt.outsider@example.com\", \"role\": \"DEVELOPER\"}")))
                .andExpect(status().isCreated());

        // 2. Create Project
        String createProjectJson = """
                {
                    "name": "DevFlow Portal Core",
                    "description": "Core Project",
                    "status": "ACTIVE"
                }
                """;

        String createProjectResponse = mockMvc.perform(post("/api/organizations/" + orgId + "/projects")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProjectJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("DevFlow Portal Core"))
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(createProjectResponse).get("id").asLong();

        // Add dev to Project using email
        String addProjectMemberResponse = mockMvc.perform(post("/api/organizations/" + orgId + "/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"pt.dev@example.com\", \"role\": \"DEVELOPER\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long devUserId = objectMapper.readTree(addProjectMemberResponse).get("userId").asLong();


        // 3. Create Task
        String createTaskJson = """
                {
                    "title": "Fix Security Vulnerability",
                    "description": "Audit and patch JWT validation",
                    "status": "TODO",
                    "priority": "HIGH",
                    "dueDate": "2026-12-31"
                }
                """;

        String createTaskResponse = mockMvc.perform(post("/api/organizations/" + orgId + "/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTaskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fix Security Vulnerability"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createTaskResponse).get("id").asLong();

        // 4. Update Task (Assign to dev & update status to IN_PROGRESS) via PATCH /api/organizations/{orgId}/projects/{projectId}/tasks/{taskId}
        String updateTaskJson = String.format("""
                {
                    "assigneeId": %d,
                    "status": "IN_PROGRESS"
                }
                """, devUserId);

        mockMvc.perform(patch("/api/organizations/" + orgId + "/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assigneeId").value(devUserId));

        // 5. Assigned Dev can view Task
        mockMvc.perform(get("/api/organizations/" + orgId + "/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));

        // 6. Non-project member (outsider) attempting to view task -> 403 Forbidden
        mockMvc.perform(get("/api/organizations/" + orgId + "/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }
}
