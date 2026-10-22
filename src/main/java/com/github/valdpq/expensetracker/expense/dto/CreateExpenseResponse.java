package com.github.valdpq.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        Long categoryId,
        String description,
        boolean budgetExceeded
) {
}
