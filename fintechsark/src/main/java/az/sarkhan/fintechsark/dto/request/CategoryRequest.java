package az.sarkhan.fintechsark.dto.request;

import az.sarkhan.fintechsark.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100) String name,

        @NotNull(message = "Type is required")
        CategoryType type,

        @NotNull(message = "Parent category is required")
        Long parentId
) {}
