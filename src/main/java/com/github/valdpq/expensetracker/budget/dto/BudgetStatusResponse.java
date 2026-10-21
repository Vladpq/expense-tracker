package com.github.valdpq.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetStatusResponse(
        Long budgetId,
        Long categoryId,
        YearMonth month,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal remaining,
        boolean exceeded
) {
}
