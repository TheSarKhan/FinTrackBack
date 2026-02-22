package az.sarkhan.fintechsark.controller;

import az.sarkhan.fintechsark.dto.request.CategoryRequest;
import az.sarkhan.fintechsark.dto.response.CategoryResponse;
import az.sarkhan.fintechsark.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** All parent categories (system-level) */
    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> getParents() {
        return ResponseEntity.ok(categoryService.getAllParentCategories());
    }

    /** Subcategories of a specific parent (system + user's own) */
    @GetMapping("/parents/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubcategories(parentId));
    }

    /** Full category tree for dropdown usage */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategoriesForUser());
    }

    /** Create a custom subcategory */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createUserCategory(request));
    }

    /** Soft-delete a user's own custom category */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteUserCategory(id);
        return ResponseEntity.noContent().build();
    }
}
