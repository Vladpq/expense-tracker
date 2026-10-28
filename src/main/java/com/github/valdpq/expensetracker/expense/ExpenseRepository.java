package com.github.valdpq.expensetracker.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e WHERE e.user.id = :userId
            AND (cast(:from AS LocalDate) IS NULL OR e.expenseDate >= :from)
            AND (cast(:to AS LocalDate) IS NULL OR e.expenseDate <= :to)
            AND (cast(:categoryId AS Long) IS NULL OR e.category.id = :categoryId)
            """)
    Page<Expense> findFiltered(@Param("from") LocalDate from,
                               @Param("to") LocalDate to,
                               @Param("categoryId") Long categoryId,
                               @Param("userId") Long userId,
                               Pageable pageable);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT new com.github.valdpq.expensetracker.expense.CategorySpent(e.category.id, SUM (e.amount))
            FROM Expense e
            WHERE e.user.id = :userId
            AND e.expenseDate >= :from AND e.expenseDate <= :to
            GROUP BY e.category.id
            """)
    List<CategorySpent> sumByCategory(@Param("userId") Long userId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    @Query("""
            SELECT new com.github.valdpq.expensetracker.expense.CategoryExpenseSummary(e.category.id, e.category.name, SUM (e.amount), COUNT (e))
            FROM Expense e
            WHERE e.user.id = :userId
            AND e.expenseDate >= :from AND e.expenseDate <= :to
            GROUP BY e.category.id, e.category.name
            ORDER BY SUM(e.amount) DESC, e.category.name ASC
            """)
    List<CategoryExpenseSummary> sumByCategoryReport(@Param("userId") Long userId,
                                                     @Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);
}
