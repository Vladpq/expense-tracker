package com.github.valdpq.expensetracker.report.dto;

import java.math.BigDecimal;

public record CategoryReport(
        Long categoryId,
        String categoryName,
        BigDecimal totalSpent,
        Long expenseCount,
        BigDecimal percentageOfTotal
) {
}
