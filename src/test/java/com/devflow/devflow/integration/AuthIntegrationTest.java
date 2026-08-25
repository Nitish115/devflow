package com.devflow.devflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterLoginAndProtectedAccess() throws Exception {
        String registerJson = """
                {
                    "email": "user.auth.test@example.com",
                    "password": "Password123!",
                    "role": "DEVELOPER"
                }
                """;

        // 1. Register User
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user.auth.test@example.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));

        String loginJson = """
                {
                    "email": "user.auth.test@example.com",
                    "password": "Password123!"
                }
                """;

        // 2. Login User
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode responseNode = objectMapper.readTree(loginResponse);
        String token = responseNode.get("token").asText();

        // 3. Access Protected Endpoint with Bearer Token
        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized());
    }
}
