package com.github.valdpq.expensetracker.expense;

import java.math.BigDecimal;

public record CategorySpent(
        Long categoryId,
        BigDecimal total
) {
}
