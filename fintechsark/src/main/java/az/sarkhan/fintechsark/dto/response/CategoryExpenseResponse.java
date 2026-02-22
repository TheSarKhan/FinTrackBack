package az.sarkhan.fintechsark.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CategoryExpenseResponse(
        Long categoryId,
        String categoryName,
        BigDecimal total,
        Double percentage,
        List<CategoryExpenseResponse> subcategories
) {}
