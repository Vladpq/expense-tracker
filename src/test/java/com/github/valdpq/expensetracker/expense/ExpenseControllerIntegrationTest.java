package com.github.valdpq.expensetracker.expense;

import com.github.valdpq.expensetracker.IntegrationTestBase;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import com.github.valdpq.expensetracker.expense.dto.PatchExpenseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExpenseControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void createExpense_shouldReturn201_whenExpenseIsSaved() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(100), LocalDate.now(), categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(categoryId));
    }


    @Test
    void createExpense_shouldReturn400_whenDateIsTomorrow() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(100), LocalDate.now().plusDays(1),
                        categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createExpense_shouldReturn404_whenWrongOwner() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        String token2 = registerAndGetToken("email2@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(100), LocalDate.now(),
                        categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token2)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyExpenses_shouldReturnAllUserSortedExpenses_whenNoFilterProvided() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long expenseId1 = createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100), LocalDate.now());
        long expenseId2 = createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(500), LocalDate.now());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(expenseId2))
                .andExpect(jsonPath("$.content[1].id").value(expenseId1))
                .andExpect(jsonPath("$.content[0].amount").value(500.0));
    }

    @Test
    void getMyExpenses_shouldReturnExpensesFilteredByPeriod_whenPeriodFilterIsProvided() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(400),
                LocalDate.of(2026, 10, 20));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(300),
                LocalDate.of(2026, 10, 19));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(700),
                LocalDate.of(2026, 10, 26));

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .param("from", String.valueOf(LocalDate.of(2026, 10, 20)))
                        .param("to", String.valueOf(LocalDate.of(2026, 10, 25))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].amount").value(100.0))
                .andExpect(jsonPath("$.content[1].amount").value(400.0));
    }

    @Test
    void getMyExpenses_shouldReturnExpensesFilteredByPeriodAndCategory_whenPeriodAndCategoryFilterIsProvided() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long categoryId2 = createCategoryAndGetId(token, "Name2");
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(400),
                LocalDate.of(2026, 10, 20));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(300),
                LocalDate.of(2026, 10, 19));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(700),
                LocalDate.of(2026, 10, 26));

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .param("from", String.valueOf(LocalDate.of(2026, 10, 20)))
                        .param("to", String.valueOf(LocalDate.of(2026, 10, 25)))
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].amount").value(100.0));
    }

    @Test
    void getMyExpenses_shouldReturnOnlyCurrentUserExpenses_whenGetMyExpenses() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        String token2 = registerAndGetToken("email2@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long categoryId2 = createCategoryAndGetId(token2, "Name2");
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));
        createExpenseAndGetId(token2, categoryId2, BigDecimal.valueOf(400),
                LocalDate.of(2026, 10, 20));
        createExpenseAndGetId(token2, categoryId2, BigDecimal.valueOf(300),
                LocalDate.of(2026, 10, 19));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(700),
                LocalDate.of(2026, 10, 26));

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].amount").value(700.0));
    }


    @Test
    void editExpense_shouldReturnEditedExpense_whenEditingOnlyAmount() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long expenseId = createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));

        mockMvc.perform(patch("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PatchExpenseRequest(
                                BigDecimal.valueOf(300), null, null, null))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(300.0))
                .andExpect(jsonPath("$.content[0].categoryId").value(categoryId))
                .andExpect(jsonPath("$.content[0].expenseDate").value("2026-10-25"));
    }

    @Test
    void deleteExpense_shouldReturn204_whenExpenseIsRemoved() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long expenseId = createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));

        mockMvc.perform(delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void deleteExpense_shouldReturn404_whenWrongExpenseOwner() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        String token2 = registerAndGetToken("user2@test.com", "password");
        long categoryId = createCategoryAndGetId(token1, "Name1");
        long expenseId = createExpenseAndGetId(token1, categoryId, BigDecimal.valueOf(100),
                LocalDate.of(2026, 10, 25));

        mockMvc.perform(delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(100.0));
    }
}
