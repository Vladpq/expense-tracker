package com.github.valdpq.expensetracker.category;

import com.github.valdpq.expensetracker.IntegrationTestBase;
import com.github.valdpq.expensetracker.budget.dto.CreateBudgetRequest;
import com.github.valdpq.expensetracker.category.dto.CategoryRequest;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CategoryControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void addCategory_shouldReturn201_whenCategoryIsCreated() throws Exception {
        String token = registerAndGetToken("email@test.com", "password");
        CategoryRequest categoryRequest = new CategoryRequest("Name");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Name"));
    }

    @Test
    void addCategory_shouldReturn400_whenNoNameProvided() throws Exception {
        String token = registerAndGetToken("email@test.com", "password");
        CategoryRequest categoryRequest = new CategoryRequest("");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCategory_shouldReturn409_whenNameDuplicate() throws Exception {
        String token = registerAndGetToken("email@test.com", "password");
        CategoryRequest categoryRequest = new CategoryRequest("Name");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void addCategory_shouldReturn201_whenSameNameInOtherUser() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        String token2 = registerAndGetToken("user2@test.com", "password");
        CategoryRequest categoryRequest = new CategoryRequest("Name");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void getMyCategories_shouldReturn200_whenGetUserCategories() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        String token2 = registerAndGetToken("user2@test.com", "password");
        createCategoryAndGetId(token1, "Name1");
        createCategoryAndGetId(token2, "Name2");

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Name1"));

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Name2"));
    }

    @Test
    void deleteCategory_shouldReturn204_whenCategoryIsRemoved() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void deleteCategory_shouldReturn404_whenWrongCategoryOwner() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        String token2 = registerAndGetToken("user2@test.com", "password");
        long categoryId = createCategoryAndGetId(token1, "Name1");

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Name1"));
    }

    @Test
    void deleteCategory_shouldReturn409_whenDeletingCategoryWithExpenses() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        long categoryId = createCategoryAndGetId(token1, "Name");
        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(100), LocalDate.now(), categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_shouldReturn204_whenDeletingCategoryWithBudget() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        long categoryId = createCategoryAndGetId(token1, "Name");
        CreateBudgetRequest createBudgetRequest =
                new CreateBudgetRequest(BigDecimal.valueOf(100), YearMonth.now(), categoryId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
