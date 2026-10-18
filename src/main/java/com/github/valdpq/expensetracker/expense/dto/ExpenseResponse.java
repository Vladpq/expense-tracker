package com.github.valdpq.expensetracker.expense.dto;

import com.github.valdpq.expensetracker.expense.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        Long categoryId,
        String description
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getAmount(),
                expense.getExpenseDate(), expense.getCategory().getId(),
                expense.getDescription());
    }
}
