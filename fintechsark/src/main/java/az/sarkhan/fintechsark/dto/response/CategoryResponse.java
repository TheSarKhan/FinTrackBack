package az.sarkhan.fintechsark.dto.response;

import az.sarkhan.fintechsark.enums.CategoryType;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        Long parentId,
        String parentName,
        boolean isSystemCategory,
        boolean isParentCategory,
        List<CategoryResponse> children
) {}
