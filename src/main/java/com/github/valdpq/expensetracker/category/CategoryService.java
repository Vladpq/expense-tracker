package com.github.valdpq.expensetracker.category;

import com.github.valdpq.expensetracker.auth.CurrentUserProvider;
import com.github.valdpq.expensetracker.category.dto.CategoryRequest;
import com.github.valdpq.expensetracker.category.dto.CategoryResponse;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        User currentUser = getCurrentUser();

        if (categoryRepository.existsByUserIdAndName(currentUser.getId(), request.name())) {
            throw new CategoryAlreadyExistsException("Category " + request.name() + " already exists.");
        }

        Category category = Category.builder()
                .name(request.name())
                .user(currentUser)
                .build();

        categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }

    public Page<CategoryResponse> getMyCategories(Pageable pageable) {
        User currentUser = getCurrentUser();

        return categoryRepository
                .findAllByUserId(currentUser.getId(), pageable)
                .map(CategoryResponse::fromEntity);
    }

    @Transactional
    public void deleteCategory(Long id) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        categoryRepository.delete(category);
    }

    private User getCurrentUser() {
        String userEmail = CurrentUserProvider.getCurrentEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
