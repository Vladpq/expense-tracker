package com.github.valdpq.expensetracker.category.dto;

import com.github.valdpq.expensetracker.category.Category;

public record CategoryResponse(
        Long id,
        String name
) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
