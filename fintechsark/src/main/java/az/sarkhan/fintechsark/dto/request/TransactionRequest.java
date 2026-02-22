package az.sarkhan.fintechsark.dto.request;

import az.sarkhan.fintechsark.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        String description,

        @NotNull(message = "Type is required")
        TransactionType type,

        @NotNull(message = "Date is required")
        LocalDate date,

         Long categoryId
) {}
