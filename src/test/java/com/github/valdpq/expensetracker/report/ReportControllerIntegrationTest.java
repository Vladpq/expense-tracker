package com.github.valdpq.expensetracker.report;

import com.github.valdpq.expensetracker.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReportControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void reportByMonth_shouldReturnCorrectReportValues_whenGettingReport() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        long categoryId1 = createCategoryAndGetId(token, "Name1");
        long categoryId2 = createCategoryAndGetId(token, "Name2");

        createExpenseAndGetId(token, categoryId1, BigDecimal.valueOf(50), LocalDate.now());
        createExpenseAndGetId(token, categoryId1, BigDecimal.valueOf(50), LocalDate.now());
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(100), LocalDate.now());
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(100), LocalDate.now());

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(300))
                .andExpect(jsonPath("$.categories[1].totalSpent").value(100))
                .andExpect(jsonPath("$.categories[0].totalSpent").value(200))
                .andExpect(jsonPath("$.categories[1].expenseCount").value(2))
                .andExpect(jsonPath("$.categories[0].expenseCount").value(2))
                .andExpect(jsonPath("$.categories[1].percentageOfTotal").value(33.33))
                .andExpect(jsonPath("$.categories[0].percentageOfTotal").value(66.67));
    }

    @Test
    void reportByMonth_shouldReturnCorrectlySortedCategories_whenTotalSpentIsEqual() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        long categoryId1 = createCategoryAndGetId(token, "Games");
        long categoryId2 = createCategoryAndGetId(token, "Food");

        createExpenseAndGetId(token, categoryId1, BigDecimal.valueOf(100), LocalDate.now());
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(100), LocalDate.now());

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.categories[1].categoryName").value("Games"));
    }

    @Test
    void reportByMonth_shouldReturnPreviousMonthTotalAndNegativeDifference_whenExpensesDecreased() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        long categoryId1 = createCategoryAndGetId(token, "Games");
        long categoryId2 = createCategoryAndGetId(token, "Food");

        createExpenseAndGetId(token, categoryId1, BigDecimal.valueOf(300), LocalDate.now().minusMonths(1));
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(100), LocalDate.now());

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousMonthTotal").value(300.0))
                .andExpect(jsonPath("$.difference").value(-200.0));
    }

    @Test
    void reportByMonth_shouldCompareWithPreviousYearDecember_whenMonthIsJanuary() throws Exception {
        LocalDate month = LocalDate.of(2025, 1, 1);
        LocalDate prevMonth = month.minusMonths(1);
        String token = registerAndGetToken("email@test.com", "Password");

        long categoryId1 = createCategoryAndGetId(token, "Games");
        long categoryId2 = createCategoryAndGetId(token, "Food");

        createExpenseAndGetId(token, categoryId1, BigDecimal.valueOf(300), prevMonth);
        createExpenseAndGetId(token, categoryId2, BigDecimal.valueOf(100), month);

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("month", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousMonthTotal").value(300.0))
                .andExpect(jsonPath("$.difference").value(-200.0));
    }

    @Test
    void reportByMonth_shouldReturn200_whenNoExpenses() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.categories", hasSize(0)));
    }

    @Test
    void reportByMonth_shouldReturn400_whenInvalidMonthValue() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("month", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportByMonth_shouldCountOnlyExpensesWithinMonth_whenExpensesOnBoundaryDates() throws Exception {
        String token = registerAndGetToken("email@test.com", "Password");

        long categoryId = createCategoryAndGetId(token, "Name");

        LocalDate firstDay = YearMonth.now().minusMonths(2).atDay(1);
        LocalDate lastDay = YearMonth.now().minusMonths(2).atEndOfMonth();

        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(50), firstDay);
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(20), lastDay);
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100), firstDay.minusDays(1));
        createExpenseAndGetId(token, categoryId, BigDecimal.valueOf(100), lastDay.plusDays(1));

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("month", String.valueOf(YearMonth.now().minusMonths(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(70));
    }

    @Test
    void reportByMonth_shouldIgnoreOtherUsersExpenses_whenCalculatingReport() throws Exception {
        String token1 = registerAndGetToken("user1@test.com", "Password");
        String token2 = registerAndGetToken("user2@test.com", "Password");

        long categoryId1 = createCategoryAndGetId(token1, "Name1");
        long categoryId2 = createCategoryAndGetId(token2, "Name2");

        createExpenseAndGetId(token1, categoryId1, BigDecimal.valueOf(100), LocalDate.now());
        createExpenseAndGetId(token2, categoryId2, BigDecimal.valueOf(900), LocalDate.now());

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Name1"));
    }
}
