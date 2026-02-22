package az.sarkhan.fintechsark.dto.response;

import az.sarkhan.fintechsark.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankStatementRow(
        LocalDate date,
        String description,
        Long categoryId,
        String categoryName,
        Long parentCategoryId,
        String parentCategoryName,
        TransactionType type,
        BigDecimal amount
) {}
