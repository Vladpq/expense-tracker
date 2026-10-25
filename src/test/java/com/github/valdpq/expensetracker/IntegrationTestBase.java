package com.github.valdpq.expensetracker;

import com.github.valdpq.expensetracker.auth.dto.AuthResponse;
import com.github.valdpq.expensetracker.auth.dto.RegisterRequest;
import com.github.valdpq.expensetracker.category.dto.CategoryRequest;
import com.github.valdpq.expensetracker.category.dto.CategoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportTestcontainers(TestcontainersConfiguration.class)
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String registerAndGetToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterRequest(email, password));

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, AuthResponse.class).token();
    }

    protected long createCategoryAndGetId(String token, String name) throws Exception {
        String body = objectMapper.writeValueAsString(new CategoryRequest(name));

        String response = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, CategoryResponse.class).id();
    }
}
