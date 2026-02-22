package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.request.CategoryRequest;
import az.sarkhan.fintechsark.dto.response.CategoryResponse;
import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.entity.User;
import az.sarkhan.fintechsark.exception.BusinessException;
import az.sarkhan.fintechsark.exception.ResourceNotFoundException;
import az.sarkhan.fintechsark.repository.CategoryRepository;
import az.sarkhan.fintechsark.repository.TransactionRepository;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllParentCategories() {
        return categoryRepository.findAllParentCategories()
                .stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(Long parentId) {
        Long userId = securityUtils.getCurrentUserId();
        return categoryRepository.findSubcategoriesByParentAndUser(parentId, userId)
                .stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForUser() {
        Long userId = securityUtils.getCurrentUserId();
        List<Category> parents = categoryRepository.findAllParentCategories();
        return parents.stream()
                .map(parent -> {
                    List<CategoryResponse> children = categoryRepository
                            .findSubcategoriesByParentAndUser(parent.getId(), userId)
                            .stream()
                            .map(c -> toResponse(c, false))
                            .collect(Collectors.toList());
                    return new CategoryResponse(
                            parent.getId(), parent.getName(), parent.getType(),
                            null, null, true, true, children
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createUserCategory(CategoryRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Category parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + request.parentId()));

        if (!parent.isParentCategory()) {
            throw new BusinessException("You can only add subcategories to parent categories");
        }

        if (categoryRepository.existsByNameAndParentAndUser(request.name(), request.parentId(), currentUser.getId())) {
            throw new BusinessException("Category with this name already exists under the selected parent");
        }

        Category category = Category.builder()
                .name(request.name())
                .type(request.type())
                .parent(parent)
                .user(currentUser)
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        return toResponse(category, false);
    }

    @Override
    @Transactional
    public void deleteUserCategory(Long categoryId) {
        Long userId = securityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        if (category.isSystemCategory()) {
            throw new BusinessException("System categories cannot be deleted");
        }
        if (!category.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only delete your own categories");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public CategoryResponse toResponse(Category c, boolean includeChildren) {
        String parentName = c.getParent() != null ? c.getParent().getName() : null;
        Long parentId = c.getParent() != null ? c.getParent().getId() : null;
        return new CategoryResponse(
                c.getId(), c.getName(), c.getType(),
                parentId, parentName,
                c.isSystemCategory(), c.isParentCategory(),
                null
        );
    }
}
