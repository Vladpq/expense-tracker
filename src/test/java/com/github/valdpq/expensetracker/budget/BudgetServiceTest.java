package com.github.valdpq.expensetracker.budget;

import com.github.valdpq.expensetracker.budget.dto.BudgetResponse;
import com.github.valdpq.expensetracker.budget.dto.CreateBudgetRequest;
import com.github.valdpq.expensetracker.budget.dto.UpdateBudgetRequest;
import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.category.CategoryNotFoundException;
import com.github.valdpq.expensetracker.category.CategoryRepository;
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
}
