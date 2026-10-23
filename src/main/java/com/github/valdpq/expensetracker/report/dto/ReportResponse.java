package com.github.valdpq.expensetracker.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportResponse(
        BigDecimal total,
        List<CategoryReport> categories
) {
}
