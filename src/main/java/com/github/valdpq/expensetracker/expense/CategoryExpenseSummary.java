package com.github.valdpq.expensetracker.expense;

import java.math.BigDecimal;

public record CategoryExpenseSummary(
        Long categoryId,
        String categoryName,
        BigDecimal totalSpent,
        Long expenseCount
) {
}
