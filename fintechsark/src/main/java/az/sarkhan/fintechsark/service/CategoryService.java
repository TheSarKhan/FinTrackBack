package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.request.CategoryRequest;
import az.sarkhan.fintechsark.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllParentCategories();
    List<CategoryResponse> getSubcategories(Long parentId);
    List<CategoryResponse> getAllCategoriesForUser();
    CategoryResponse createUserCategory(CategoryRequest request);
    void deleteUserCategory(Long categoryId);
}
