package com.github.valdpq.expensetracker.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e WHERE e.user.id = :userId
            AND (cast(:from AS LocalDate) IS NULL OR e.expenseDate >= :from)
            AND (cast(:to AS LocalDate) IS NULL OR e.expenseDate <= :to)
            AND (cast(:categoryId AS Long) IS NULL OR e.category.id = :categoryId)
            """)
    Page<Expense> findFiltered(@Param("from")LocalDate from,
                               @Param("to")LocalDate to,
                               @Param("categoryId") Long categoryId,
                               @Param("userId") Long userId,
                               Pageable pageable);
}
