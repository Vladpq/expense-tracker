package com.github.valdpq.expensetracker.expense;

import com.github.valdpq.expensetracker.auth.CurrentUserProvider;
import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.category.CategoryNotFoundException;
import com.github.valdpq.expensetracker.category.CategoryRepository;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import com.github.valdpq.expensetracker.expense.dto.ExpenseResponse;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
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

        return ExpenseResponse.fromEntity(expense);
    }

    private User getCurrentUser() {
        String userEmail = CurrentUserProvider.getCurrentEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
