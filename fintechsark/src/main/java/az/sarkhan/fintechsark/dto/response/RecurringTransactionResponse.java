package az.sarkhan.fintechsark.dto.response;

import az.sarkhan.fintechsark.enums.RecurringFrequency;
import az.sarkhan.fintechsark.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecurringTransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        RecurringFrequency frequency,
        Integer dayOfMonth,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastExecutedDate,
        LocalDate nextExecutionDate,
        Boolean isActive,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {}