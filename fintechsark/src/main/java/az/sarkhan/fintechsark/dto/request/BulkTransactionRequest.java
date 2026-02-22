package az.sarkhan.fintechsark.dto.request;

import az.sarkhan.fintechsark.enums.TransactionType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BulkTransactionRequest(
        @NotNull List<BulkTransactionItem> transactions
) {
    public record BulkTransactionItem(
            @NotNull BigDecimal amount,
            String description,
            @NotNull TransactionType type,
            @NotNull LocalDate date,
            @NotNull Long categoryId
    ) {}
}
