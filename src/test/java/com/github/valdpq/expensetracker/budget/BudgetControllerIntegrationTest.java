package com.github.valdpq.expensetracker.budget;

import com.github.valdpq.expensetracker.IntegrationTestBase;
import com.github.valdpq.expensetracker.budget.dto.CreateBudgetRequest;
import com.github.valdpq.expensetracker.budget.dto.UpdateBudgetRequest;
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

public class BudgetControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void createBudget_shouldReturn201_whenBudgetIsSaved() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateBudgetRequest createBudgetRequest =
                new CreateBudgetRequest(BigDecimal.valueOf(100), YearMonth.now(), categoryId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(categoryId));
    }


    @Test
    void createBudget_shouldReturn400_whenMonthInPast() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateBudgetRequest createBudgetRequest =
                new CreateBudgetRequest(BigDecimal.valueOf(100), YearMonth.now().minusMonths(1), categoryId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createBudget_shouldReturn404_whenWrongOwner() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        String token2 = registerAndGetToken("email2@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateBudgetRequest createBudgetRequest =
                new CreateBudgetRequest(BigDecimal.valueOf(100), YearMonth.now(),
                        categoryId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token2)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBudget_shouldReturn409_whenBudgetForMonthAlreadyExists() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        CreateBudgetRequest createBudgetRequest =
                new CreateBudgetRequest(BigDecimal.valueOf(100), YearMonth.now(), categoryId);


        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateBudget_shouldReturnEditedBudget_whenUpdatingBudgetAmount() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long budgetId = createBudgetAndGetId(token, categoryId,
                BigDecimal.valueOf(500), YearMonth.now());

        mockMvc.perform(patch("/api/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateBudgetRequest(BigDecimal.valueOf(300)))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].limit").value(300.0));
    }


    @Test
    void deleteBudget_shouldReturn204_whenBudgetIsRemoved() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");
        long budgetId = createBudgetAndGetId(token, categoryId,
                BigDecimal.valueOf(500), YearMonth.now());

        mockMvc.perform(delete("/api/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getBudgetStatus_shouldReturnExceededFalse_whenSpentEqualsLimit() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(100), YearMonth.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(60), LocalDate.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(40), LocalDate.now());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(100.0))
                .andExpect(jsonPath("$[0].exceeded").value(false));
    }

    @Test
    void getBudgetStatus_shouldReturnExceededTrue_whenSpentExceededLimit() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(100), YearMonth.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(60), LocalDate.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(40), LocalDate.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(1), LocalDate.now());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(101.0))
                .andExpect(jsonPath("$[0].exceeded").value(true));
    }

    @Test
    void getBudgetStatus_shouldReturnZeroSpent_whenCategoryWithoutExpenses() throws Exception {
        String token = registerAndGetToken("user@test.com", "password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(100), YearMonth.now());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(0))
                .andExpect(jsonPath("$[0].remaining").value(100.0));
    }

    @Test
    void getBudgetStatus_shouldReturnCurrentUserStatus_whenOtherUserHasExpenses() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "password");
        String token2 = registerAndGetToken("user2@test.com", "password");
        long categoryId1 = createCategoryAndGetId(token1, "Name");
        long categoryId2 = createCategoryAndGetId(token2, "Name");
        createBudgetAndGetId(token1, categoryId1, BigDecimal.valueOf(100), YearMonth.now());
        createExpenseAndGetId(token2, categoryId2, BigDecimal.valueOf(60), LocalDate.now());
        createExpenseAndGetId(token1, categoryId1, BigDecimal.valueOf(40), LocalDate.now());

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(40.0));
    }

    @Test
    void getBudgetStatus_shouldCountOnlyCurrentMonthExpenses_whenExpenseOnPreviousMonthEnd() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        YearMonth currentMonth = YearMonth.now();
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(1000), currentMonth);
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(500),
                currentMonth.minusMonths(1).atEndOfMonth());   // минулий місяць, не рахується
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100),
                currentMonth.atDay(1));                         // перше число, рахується

        mockMvc.perform(get("/api/budgets/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(100.0));
    }

    @Test
    void createExpense_shouldReturnBudgetExceededFalse_whenTotalEqualsLimit() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(100), YearMonth.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(60), LocalDate.now());

        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(40), LocalDate.now(),
                        categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetExceeded").value(false));
    }

    @Test
    void createExpense_shouldReturnBudgetExceededTrue_whenExpenseExceedsLimit() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createBudgetAndGetId(token, categoryId, BigDecimal.valueOf(100), YearMonth.now());
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(60), LocalDate.now());

        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(41), LocalDate.now(),
                        categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetExceeded").value(true));
    }

    @Test
    void createExpense_shouldReturnBudgetExceededFalse_whenCategoryWithoutBudget() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");
        long categoryId = createCategoryAndGetId(token, "Name");
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(60), LocalDate.now());

        CreateExpenseRequest createExpenseRequest =
                new CreateExpenseRequest(BigDecimal.valueOf(40), LocalDate.now(),
                        categoryId, "Description");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetExceeded").value(false));
    }
}
