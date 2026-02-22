package az.sarkhan.fintechsark.dto.response;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        String month,
        BigDecimal income,
        BigDecimal expense
) {}
