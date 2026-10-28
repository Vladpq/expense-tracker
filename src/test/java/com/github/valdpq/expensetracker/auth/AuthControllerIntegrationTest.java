package com.github.valdpq.expensetracker.auth;

import com.github.valdpq.expensetracker.IntegrationTestBase;
import com.github.valdpq.expensetracker.TestcontainersConfiguration;
import com.github.valdpq.expensetracker.auth.dto.LoginRequest;
import com.github.valdpq.expensetracker.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportTestcontainers(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_shouldReturnToken_whenRequestIsValid() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200_whenPasswordIsCorrect() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");
        LoginRequest loginRequest = new LoginRequest("email@test.com", "password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }


    @Test
    void login_shouldReturn401_whenPasswordIsNotCorrect() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");
        LoginRequest loginRequest = new LoginRequest("email@test.com", "WrongPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyCategories_shouldReturn200_whenTokenIsValid() throws Exception {

        String token = registerAndGetToken("email@test.com", "password");

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getMyCategories_shouldReturn403_whenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyCategories_shouldReturn403_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }
}
