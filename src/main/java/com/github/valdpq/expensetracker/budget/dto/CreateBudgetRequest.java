package com.github.valdpq.expensetracker.budget.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CreateBudgetRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Month is required")
        @FutureOrPresent(message = "Month must not be in the past")
        YearMonth month,

        @NotNull(message = "Category is required")
        Long categoryId
) {
}
