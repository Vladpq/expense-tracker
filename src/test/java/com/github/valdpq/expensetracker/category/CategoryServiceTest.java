package com.github.valdpq.expensetracker.category;

import com.github.valdpq.expensetracker.budget.BudgetRepository;
import com.github.valdpq.expensetracker.category.dto.CategoryRequest;
import com.github.valdpq.expensetracker.category.dto.CategoryResponse;
import com.github.valdpq.expensetracker.expense.ExpenseRepository;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private CategoryService categoryService;

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
    void addCategory_shouldThrowCategoryAlreadyExistsException_whenCategoryAlreadyExists() {

        CategoryRequest categoryRequest = new CategoryRequest("Category name");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User()));
        when(categoryRepository.existsByUserIdAndName(any(), anyString()))
                .thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.addCategory(categoryRequest));
    }

    @Test
    void addCategory_shouldSaveCategory_whenRequestIsValid() {

        CategoryRequest categoryRequest = new CategoryRequest("Category name");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User()));
        when(categoryRepository.existsByUserIdAndName(any(), anyString()))
                .thenReturn(false);

        CategoryResponse categoryResponse = categoryService.addCategory(categoryRequest);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        assertNotNull(categoryResponse);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Category name", captor.getValue().getName());
    }

    @Test
    void getMyCategories_shouldReturnPageOfCategories_whenGetMyCategories() {

        Pageable pageable = PageRequest.of(0, 10);

        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .user(user)
                .build();

        List<Category> categoryList = List.of(category);
        Page<Category> categories = new PageImpl<>(categoryList);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findAllByUserId(1L, pageable))
                .thenReturn(categories);

        Page<CategoryResponse> categoryResponses = categoryService.getMyCategories(pageable);

        assertNotNull(categoryResponses);
        assertEquals(1, categoryResponses.getTotalElements());
    }

    @Test
    void deleteCategory_shouldThrowCategoryNotFoundException_whenCategoryNotFound() {

        User user = User.builder()
                .id(1L)
                .email("email@test.com")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(999L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_shouldRemoveCategory_whenDeletingCategory() {

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
        when(expenseRepository.existsByCategoryId(1L))
                .thenReturn(false);

        categoryService.deleteCategory(1L);

        InOrder inOrder = inOrder(budgetRepository, categoryRepository);
        inOrder.verify(budgetRepository).deleteAllByCategoryId(1L);
        inOrder.verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowCategoryWithExpenseException_whenCategoryHasExpenses() {

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
        when(expenseRepository.existsByCategoryId(1L))
                .thenReturn(true);

        assertThrows(CategoryWithExpenseException.class,
                () -> categoryService.deleteCategory(1L));
        verify(budgetRepository, never()).deleteAllByCategoryId(any());
        verify(categoryRepository, never()).delete(any());
    }
}
