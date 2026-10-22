package com.github.valdpq.expensetracker.budget;

public class BudgetAlreadyExistsException extends RuntimeException {
    public BudgetAlreadyExistsException(String message) {
        super(message);
    }
}
