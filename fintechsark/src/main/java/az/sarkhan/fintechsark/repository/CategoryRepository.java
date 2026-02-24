package az.sarkhan.fintechsark.repository;

import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // All parent categories (system-level, parent_id = null)
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.id")
    List<Category> findAllParentCategories();

    // Subcategories of a parent (system + user's own)
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true AND (c.user IS NULL OR c.user.id = :userId)")
    List<Category> findSubcategoriesByParentAndUser(@Param("parentId") Long parentId, @Param("userId") Long userId);

    // All categories visible to a user (system + their own subcategories)
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND (c.user IS NULL OR c.user.id = :userId)")
    List<Category> findAllVisibleToUser(@Param("userId") Long userId);

    // User's custom subcategories only
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true")
    List<Category> findByUserId(@Param("userId") Long userId);

    // Check name uniqueness within parent for a user
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.parent.id = :parentId AND (c.user IS NULL OR c.user.id = :userId) AND c.isActive = true")
    boolean existsByNameAndParentAndUser(@Param("name") String name, @Param("parentId") Long parentId, @Param("userId") Long userId);

    long countByUserIsNull();
}
