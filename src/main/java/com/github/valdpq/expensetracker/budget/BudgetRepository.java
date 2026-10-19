package com.github.valdpq.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month);
}
