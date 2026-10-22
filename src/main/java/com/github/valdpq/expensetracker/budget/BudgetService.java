package com.github.valdpq.expensetracker.budget;

import com.github.valdpq.expensetracker.auth.CurrentUserProvider;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {


    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), currentUser.getId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (budgetRepository.existsByCategoryIdAndMonth(category.getId(), request.month())) {
            throw new BudgetAlreadyExistsException("Budget for this category already exists");
        }

        Budget budget = Budget.builder()
                .amount(request.amount())
                .category(category)
                .user(currentUser)
                .month(request.month())
                .build();

        budgetRepository.save(budget);

        return BudgetResponse.fromEntity(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(UpdateBudgetRequest request, Long budgetId) {
        User currentUser = getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUser.getId())
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));

        budget.setAmount(request.amount());
        return BudgetResponse.fromEntity(budget);
    }

    @Transactional
    public void deleteBudget(Long budgetId) {
        User currentUser = getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUser.getId())
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    public List<BudgetStatusResponse> getBudgetStatus(YearMonth month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : month;

        User user = getCurrentUser();
        List<Budget> budgets = budgetRepository
                .findAllByUserIdAndMonth(user.getId(), targetMonth);

        Map<Long, BigDecimal> categorySpentList = expenseRepository
                .sumByCategory(user.getId(), targetMonth.atDay(1), targetMonth.atEndOfMonth())
                .stream()
                .collect(Collectors.toMap(CategorySpent::categoryId, CategorySpent::total));

        List<BudgetStatusResponse> statusResponseList = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spent = categorySpentList.getOrDefault(budget.getCategory().getId(), BigDecimal.ZERO);
            BudgetStatusResponse response = new BudgetStatusResponse(
                    budget.getId(),
                    budget.getCategory().getId(),
                    targetMonth,
                    budget.getAmount(),
                    spent,
                    budget.getAmount().subtract(spent),
                    spent.compareTo(budget.getAmount()) > 0);
            statusResponseList.add(response);
        }

        return statusResponseList;
    }

    private User getCurrentUser() {
        String userEmail = CurrentUserProvider.getCurrentEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
