package com.github.valdpq.expensetracker.report;

import com.github.valdpq.expensetracker.expense.CategoryExpenseSummary;
import com.github.valdpq.expensetracker.expense.ExpenseRepository;
import com.github.valdpq.expensetracker.report.dto.ReportResponse;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "email@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reportByMonth_shouldDoCorrectCalculationForThreeCategories_whenReportingByMonth() {
        YearMonth targetMonth = YearMonth.of(2026, 10);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        CategoryExpenseSummary category1 = new CategoryExpenseSummary(
                1L, "Test1", BigDecimal.valueOf(100), 1L);
        CategoryExpenseSummary category2 = new CategoryExpenseSummary(
                2L, "Test2", BigDecimal.valueOf(100), 1L);
        CategoryExpenseSummary category3 = new CategoryExpenseSummary(
                3L, "Test3", BigDecimal.valueOf(100), 1L);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(category1, category2, category3));
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of());

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, response.categories().getFirst().percentageOfTotal().compareTo(new BigDecimal("33.33")));
        assertEquals(0, response.categories().get(1).percentageOfTotal().compareTo(new BigDecimal("33.33")));
        assertEquals(0, response.categories().get(2).percentageOfTotal().compareTo(new BigDecimal("33.33")));
    }

    @Test
    void reportByMonth_shouldDoCorrectCalculationForTwoCategories_whenReportingByMonth() {
        YearMonth targetMonth = YearMonth.of(2026, 10);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        CategoryExpenseSummary category1 = new CategoryExpenseSummary(
                1L, "Test1", BigDecimal.valueOf(100), 1L);
        CategoryExpenseSummary category2 = new CategoryExpenseSummary(
                2L, "Test2", BigDecimal.valueOf(200), 1L);


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(category2, category1));
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of());

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, response.categories().getFirst().percentageOfTotal().compareTo(new BigDecimal("66.67")));
        assertEquals(0, response.categories().get(1).percentageOfTotal().compareTo(new BigDecimal("33.33")));
    }

    @Test
    void reportByMonth_shouldReturnZeroTotalAndEmptyList_whenNoExpensesInMonth() {
        YearMonth targetMonth = YearMonth.of(2026, 10);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of());
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of());

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(0)));
        assertTrue(response.categories().isEmpty());
    }

    @Test
    void reportByMonth_shouldUseCurrentMonth_whenNoMonthProvided() {
        YearMonth targetMonth = YearMonth.now();
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of());
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of());

        ReportResponse response = reportService.reportByMonth(null);

        assertNotNull(response);
    }

    @Test
    void reportByMonth_shouldReturnCorrectDifference_whenLessExpenses() {
        YearMonth targetMonth = YearMonth.of(2026, 10);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        CategoryExpenseSummary category1 = new CategoryExpenseSummary(
                1L, "Test1", BigDecimal.valueOf(100), 1L);
        CategoryExpenseSummary category2 = new CategoryExpenseSummary(
                2L, "Test2", BigDecimal.valueOf(300), 1L);


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(category1));
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of(category2));

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, response.previousMonthTotal().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, response.difference().compareTo(BigDecimal.valueOf(-200)));
    }

    @Test
    void reportByMonth_shouldReturnCurrentTotalInDifference_whenNoExpensesInPrevMonth() {
        YearMonth targetMonth = YearMonth.of(2026, 10);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        CategoryExpenseSummary category1 = new CategoryExpenseSummary(
                1L, "Test1", BigDecimal.valueOf(100), 1L);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(category1));
        when(expenseRepository.sumByCategoryReport(1L, prevMonth.atDay(1), prevMonth.atEndOfMonth()))
                .thenReturn(List.of());

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, response.previousMonthTotal().compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, response.difference().compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void reportByMonth_shouldReturnCorrectDifference_whenPrevMonthInPrevYear() {
        YearMonth targetMonth = YearMonth.of(2026, 1);
        YearMonth prevMonth = targetMonth.minusMonths(1);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        CategoryExpenseSummary category1 = new CategoryExpenseSummary(
                1L, "Test1", BigDecimal.valueOf(100), 1L);
        CategoryExpenseSummary category2 = new CategoryExpenseSummary(
                2L, "Test2", BigDecimal.valueOf(300), 1L);


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)))
                .thenReturn(List.of(category1));
        when(expenseRepository.sumByCategoryReport(1L,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 31)))
                .thenReturn(List.of(category2));

        ReportResponse response = reportService.reportByMonth(targetMonth);

        assertNotNull(response);
        assertEquals(0, response.total().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, response.previousMonthTotal().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, response.difference().compareTo(BigDecimal.valueOf(-200)));
    }
}
