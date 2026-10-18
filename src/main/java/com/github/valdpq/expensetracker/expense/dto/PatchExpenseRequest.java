package com.github.valdpq.expensetracker.expense.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PatchExpenseRequest(

        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
        BigDecimal amount,

        @PastOrPresent(message = "Expense date must not be in a future")
        LocalDate expenseDate,

        Long categoryId,

        @Size(max = 100, message = "Description must not exceed 100 characters")
        String description
) {
}
