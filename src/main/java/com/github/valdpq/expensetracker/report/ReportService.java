package com.github.valdpq.expensetracker.report;

import com.github.valdpq.expensetracker.auth.CurrentUserProvider;
import com.github.valdpq.expensetracker.expense.CategoryExpenseSummary;
import com.github.valdpq.expensetracker.expense.ExpenseRepository;
import com.github.valdpq.expensetracker.report.dto.CategoryReport;
import com.github.valdpq.expensetracker.report.dto.ReportResponse;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ReportResponse reportByMonth(YearMonth month) {
        User currentUser = getCurrentUser();
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        YearMonth prevMonth = targetMonth.minusMonths(1);

        List<CategoryExpenseSummary> categoryExpenseSummaries = expenseRepository
                .sumByCategoryReport(currentUser.getId(), targetMonth.atDay(1), targetMonth.atEndOfMonth());

        BigDecimal totalSpent = totalOf(categoryExpenseSummaries);

        List<CategoryReport> categoryReports = categoryExpenseSummaries.stream()
                .map(summary -> new CategoryReport(summary.categoryId(),
                        summary.categoryName(),
                        summary.totalSpent(),
                        summary.expenseCount(),
                        summary.totalSpent().multiply(BigDecimal.valueOf(100))
                                .divide(totalSpent, 2, RoundingMode.HALF_UP)))
                .toList();

        BigDecimal previousMonthTotal = totalOf(expenseRepository
                .sumByCategoryReport(currentUser.getId(), prevMonth.atDay(1), prevMonth.atEndOfMonth()));
        BigDecimal difference = totalSpent.subtract(previousMonthTotal);

        return new ReportResponse(totalSpent, categoryReports, previousMonthTotal, difference);
    }

    private static BigDecimal totalOf(List<CategoryExpenseSummary> categoryExpenseSummaries) {
        return categoryExpenseSummaries.stream()
                .map(CategoryExpenseSummary::totalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private User getCurrentUser() {
        String userEmail = CurrentUserProvider.getCurrentEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
