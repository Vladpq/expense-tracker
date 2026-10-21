package com.github.valdpq.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    boolean existsByCategoryIdAndMonth(Long categoryId, YearMonth month);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findAllByUserIdAndMonth(Long userId, YearMonth month);
}
