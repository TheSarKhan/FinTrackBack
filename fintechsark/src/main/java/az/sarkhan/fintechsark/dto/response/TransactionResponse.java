package az.sarkhan.fintechsark.dto.response;

import az.sarkhan.fintechsark.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String description,
        TransactionType type,
        LocalDate date,
        Long categoryId,
        String categoryName,
        Long parentCategoryId,
        String parentCategoryName,
        LocalDateTime createdAt
) {}
