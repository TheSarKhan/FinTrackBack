package az.sarkhan.fintechsark.dto.request;

import az.sarkhan.fintechsark.enums.RecurringFrequency;
import az.sarkhan.fintechsark.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String description,
        @NotNull TransactionType type,
        @NotNull Long categoryId,
        @NotNull RecurringFrequency frequency,
        @NotNull @Min(1) @Max(28) Integer dayOfMonth, // max 28 — bütün aylarda var
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}