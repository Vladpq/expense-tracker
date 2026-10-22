package com.github.valdpq.expensetracker.expense;

import com.github.valdpq.expensetracker.auth.CurrentUserProvider;
import com.github.valdpq.expensetracker.budget.Budget;
import com.github.valdpq.expensetracker.budget.BudgetNotFoundException;
import com.github.valdpq.expensetracker.budget.BudgetRepository;
import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.category.CategoryNotFoundException;
import com.github.valdpq.expensetracker.category.CategoryRepository;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseResponse;
import com.github.valdpq.expensetracker.expense.dto.ExpenseResponse;
import com.github.valdpq.expensetracker.expense.dto.PatchExpenseRequest;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public CreateExpenseResponse createExpense(CreateExpenseRequest request) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Expense expense = Expense.builder()
                .amount(request.amount())
                .category(category)
                .user(currentUser)
                .expenseDate(request.expenseDate())
                .description(request.description())
                .build();

        expenseRepository.save(expense);

        YearMonth month = YearMonth.of(request.expenseDate().getYear(), request.expenseDate().getMonth());

        boolean budgetExceeded = budgetRepository.findByCategoryIdAndMonth(category.getId(), month)
                .map(b -> getSpent(currentUser, month, category)
                        .compareTo(b.getAmount()) > 0)
                .orElse(false);

        return new CreateExpenseResponse(expense.getId(), expense.getAmount(), expense.getExpenseDate(),
                expense.getCategory().getId(), expense.getDescription(), budgetExceeded);
    }

    private BigDecimal getSpent(User currentUser, YearMonth month, Category category) {
        return expenseRepository
                .sumByCategory(currentUser.getId(), month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(c -> c.categoryId().equals(category.getId()))
                .map(CategorySpent::total)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public Page<ExpenseResponse> getMyExpenses(LocalDate from, LocalDate to, Long categoryId, Pageable pageable) {
        User currentUser = getCurrentUser();

        return expenseRepository.findFiltered(from, to, categoryId, currentUser.getId(), pageable)
                .map(ExpenseResponse::fromEntity);
    }

    @Transactional
    public ExpenseResponse editExpense(PatchExpenseRequest request, Long expenseId) {
        User user = getCurrentUser();
        Expense expense = expenseRepository
                .findByIdAndUserId(expenseId, user.getId())
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            expense.setCategory(category);
        }

        if (request.amount() != null) {
            expense.setAmount(request.amount());
        }

        if (request.expenseDate() != null) {
            expense.setExpenseDate(request.expenseDate());
        }

        if (request.description() != null) {
            expense.setDescription(request.description());
        }

        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, getCurrentUser().getId())
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        expenseRepository.delete(expense);
    }

    private User getCurrentUser() {
        String userEmail = CurrentUserProvider.getCurrentEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
