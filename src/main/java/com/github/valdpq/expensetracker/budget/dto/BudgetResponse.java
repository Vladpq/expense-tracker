package com.github.valdpq.expensetracker.budget.dto;

import com.github.valdpq.expensetracker.budget.Budget;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetResponse(
        Long id,
        BigDecimal amount,
        YearMonth month,
        Long categoryId
) {
    public static BudgetResponse fromEntity(Budget budget) {
        return new BudgetResponse(budget.getId(), budget.getAmount(),
                budget.getMonth(), budget.getCategory().getId());
    }
}
