package com.devflow.devflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String devToken;

    @BeforeEach
    void setUp() throws Exception {
        ownerToken = registerAndLoginUser("org.owner@example.com", "Password123!");
        devToken = registerAndLoginUser("org.dev@example.com", "Password123!");
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

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }

    @Test
    void testOrganizationFlowAndMemberAuthorization() throws Exception {
        // 1. Create Organization as Owner
        String createOrgJson = """
                {
                    "name": "Integration Acme Corp",
                    "description": "Tech Enterprise"
                }
                """;

        String createOrgResponse = mockMvc.perform(post("/api/organizations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrgJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Acme Corp"))
                .andExpect(jsonPath("$.ownerEmail").value("org.owner@example.com"))
                .andReturn().getResponse().getContentAsString();

        Long orgId = objectMapper.readTree(createOrgResponse).get("id").asLong();

        // 2. Add Developer to Organization
        String addMemberJson = """
                {
                    "email": "org.dev@example.com",
                    "role": "DEVELOPER"
                }
                """;

        mockMvc.perform(post("/api/organizations/" + orgId + "/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addMemberJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userEmail").value("org.dev@example.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));


        // 3. Developer views Organization
        mockMvc.perform(get("/api/organizations/" + orgId)
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId));

        // 4. Developer attempts to add another member -> 403 Forbidden
        String addAnotherMemberJson = """
                {
                    "email": "org.owner@example.com",
                    "role": "DEVELOPER"
                }
                """;

        mockMvc.perform(post("/api/organizations/" + orgId + "/members")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addAnotherMemberJson))
                .andExpect(status().isForbidden());
    }
}
