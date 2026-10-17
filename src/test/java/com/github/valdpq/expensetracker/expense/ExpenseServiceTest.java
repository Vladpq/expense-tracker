package com.github.valdpq.expensetracker.expense;

import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.category.CategoryNotFoundException;
import com.github.valdpq.expensetracker.category.CategoryRepository;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import com.github.valdpq.expensetracker.expense.dto.ExpenseResponse;
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
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

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
    void createExpense_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExists() {

        CreateExpenseRequest request = new CreateExpenseRequest(
                BigDecimal.valueOf(100), LocalDate.of(2025, Month.AUGUST, 20),
                1L, "Description");
        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> expenseService.createExpense(request));
        verify(expenseRepository, never()).save(any());
    }


    @Test
    void createExpense_shouldSaveExpense_whenRequestIsValid() {

        CreateExpenseRequest request = new CreateExpenseRequest(
                BigDecimal.valueOf(100), LocalDate.of(2025, Month.AUGUST, 20),
                1L, "Description");
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

        ExpenseResponse response = expenseService.createExpense(request);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        assertNotNull(response);
        verify(expenseRepository).save(captor.capture());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(captor.getValue().getAmount()));
        assertEquals(LocalDate.of(2025, Month.AUGUST, 20), captor.getValue().getExpenseDate());
        assertEquals(1L, captor.getValue().getCategory().getId());
        assertEquals(1L, captor.getValue().getUser().getId());
        assertEquals("Description", captor.getValue().getDescription());
    }

}
