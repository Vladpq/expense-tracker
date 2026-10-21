package com.github.valdpq.expensetracker.budget;

import com.github.valdpq.expensetracker.budget.dto.BudgetResponse;
import com.github.valdpq.expensetracker.budget.dto.BudgetStatusResponse;
import com.github.valdpq.expensetracker.budget.dto.CreateBudgetRequest;
import com.github.valdpq.expensetracker.budget.dto.UpdateBudgetRequest;
import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.category.CategoryNotFoundException;
import com.github.valdpq.expensetracker.category.CategoryRepository;
import com.github.valdpq.expensetracker.expense.CategorySpent;
import com.github.valdpq.expensetracker.expense.ExpenseRepository;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {


    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

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
    void createBudget_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExists() {

        CreateBudgetRequest request = new CreateBudgetRequest(
                BigDecimal.valueOf(100), YearMonth.of(2026, 10), 1L);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> budgetService.createBudget(request));
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void createBudget_shouldThrowBudgetAlreadyExistsException_whenBudgetForMonthExists() {

        CreateBudgetRequest request = new CreateBudgetRequest(
                BigDecimal.valueOf(100), YearMonth.of(2026, 10), 1L);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .user(user)
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(category));
        when(budgetRepository.existsByCategoryIdAndMonth(1L, YearMonth.of(2026, 10)))
                .thenReturn(true);

        assertThrows(BudgetAlreadyExistsException.class,
                () -> budgetService.createBudget(request));
        verify(budgetRepository, never()).save(any());
    }


    @Test
    void createBudget_shouldSaveBudget_whenRequestIsValid() {

        CreateBudgetRequest request = new CreateBudgetRequest(
                BigDecimal.valueOf(100), YearMonth.of(2026, 10), 1L);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .user(user)
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(category));

        BudgetResponse response = budgetService.createBudget(request);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        assertNotNull(response);
        verify(budgetRepository).save(captor.capture());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(captor.getValue().getAmount()));
        assertEquals(YearMonth.of(2026, 10), captor.getValue().getMonth());
        assertEquals(1L, captor.getValue().getCategory().getId());
        assertEquals(1L, captor.getValue().getUser().getId());
    }

    @Test
    void updateBudget_shouldThrowBudgetNotFoundException_whenBudgetNotFound() {

        UpdateBudgetRequest request = new UpdateBudgetRequest(BigDecimal.valueOf(1000));
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> budgetService.updateBudget(request, 1L));
    }

    @Test
    void updateBudget_shouldUpdateOnlyAmount_whenRequestIsValid() {

        UpdateBudgetRequest request = new UpdateBudgetRequest(BigDecimal.valueOf(1000));
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .user(user)
                .build();
        Budget budget = Budget.builder()
                .id(1L)
                .user(user)
                .category(category)
                .month(YearMonth.of(2026, 10))
                .amount(BigDecimal.valueOf(1500))
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(budget));

        BudgetResponse response = budgetService.updateBudget(request, 1L);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(budget.getAmount()));
        assertEquals(1L, budget.getCategory().getId());
        assertEquals(YearMonth.of(2026, 10), budget.getMonth());
    }

    @Test
    void deleteBudget_shouldThrowBudgetNotFoundException_whenBudgetNotFound() {

        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class,
                () -> budgetService.deleteBudget(999L));
        verify(budgetRepository, never()).delete(any());
    }

    @Test
    void deleteBudget_shouldRemoveBudget_whenDeletingBudget() {

        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .user(user)
                .build();
        Budget budget = Budget.builder()
                .id(1L)
                .user(user)
                .category(category)
                .month(YearMonth.of(2026, 10))
                .amount(BigDecimal.valueOf(1500))
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(budget));

        budgetService.deleteBudget(1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void getBudgetStatus_shouldShowValidStatuses_whenGettingBudgetStatus() {

        YearMonth targetMonth = YearMonth.of(2026, 10);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category1 = Category.builder()
                .id(1L)
                .name("Test1")
                .user(user)
                .build();
        Category category2 = Category.builder()
                .id(2L)
                .name("Test2")
                .user(user)
                .build();
        Budget budget1 = Budget.builder()
                .id(1L)
                .user(user)
                .category(category1)
                .month(targetMonth)
                .amount(BigDecimal.valueOf(1000))
                .build();
        Budget budget2 = Budget.builder()
                .id(2L)
                .user(user)
                .category(category2)
                .month(targetMonth)
                .amount(BigDecimal.valueOf(500))
                .build();


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findAllByUserIdAndMonth(1L, targetMonth))
                .thenReturn(List.of(budget1, budget2));
        when(expenseRepository.sumByCategory(1L, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of(new CategorySpent(1L, BigDecimal.valueOf(1200))));

        List<BudgetStatusResponse> result = budgetService.getBudgetStatus(targetMonth);

        assertNotNull(result);
        assertEquals(0, result.get(0).spent().compareTo(BigDecimal.valueOf(1200)));
        assertEquals(0, result.get(0).remaining().compareTo(BigDecimal.valueOf(-200)));
        assertTrue(result.get(0).exceeded());
        assertEquals(0, result.get(1).spent().compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, result.get(1).remaining().compareTo(BigDecimal.valueOf(500)));
        assertFalse(result.get(1).exceeded());
    }

    @Test
    void getBudgetStatus_shouldNotMarkExceeded_whenSpentEqualsLimit() {

        YearMonth targetMonth = YearMonth.of(2026, 10);
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category1 = Category.builder()
                .id(1L)
                .name("Test1")
                .user(user)
                .build();
        Budget budget1 = Budget.builder()
                .id(1L)
                .user(user)
                .category(category1)
                .month(targetMonth)
                .amount(BigDecimal.valueOf(1000))
                .build();


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findAllByUserIdAndMonth(1L, targetMonth))
                .thenReturn(List.of(budget1));
        when(expenseRepository.sumByCategory(1L, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of(new CategorySpent(1L, BigDecimal.valueOf(1000))));

        List<BudgetStatusResponse> result = budgetService.getBudgetStatus(targetMonth);

        assertNotNull(result);
        assertFalse(result.get(0).exceeded());
    }

    @Test
    void getBudgetStatus_shouldUseCurrentMonth_whenMonthIsNull() {

        YearMonth targetMonth = YearMonth.now();
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category1 = Category.builder()
                .id(1L)
                .name("Test1")
                .user(user)
                .build();
        Budget budget1 = Budget.builder()
                .id(1L)
                .user(user)
                .category(category1)
                .month(targetMonth)
                .amount(BigDecimal.valueOf(1000))
                .build();


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findAllByUserIdAndMonth(1L, targetMonth))
                .thenReturn(List.of(budget1));
        when(expenseRepository.sumByCategory(1L, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of(new CategorySpent(1L, BigDecimal.valueOf(1200))));

        List<BudgetStatusResponse> result = budgetService.getBudgetStatus(null);

        assertNotNull(result);
        assertEquals(targetMonth, result.get(0).month());
    }

    @Test
    void getBudgetStatus_shouldReturnEmptyList_whenNoBudgetsExist() {

        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        YearMonth targetMonth = YearMonth.now();


        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findAllByUserIdAndMonth(1L, targetMonth))
                .thenReturn(List.of());
        when(expenseRepository.sumByCategory(1L, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of());

        List<BudgetStatusResponse> result = budgetService.getBudgetStatus(targetMonth);

        assertTrue(result.isEmpty());
    }
}
