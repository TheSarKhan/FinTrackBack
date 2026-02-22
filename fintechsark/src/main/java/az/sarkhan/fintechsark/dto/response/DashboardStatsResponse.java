package az.sarkhan.fintechsark.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        BigDecimal currentMonthExpense,
        List<CategoryExpenseResponse> expenseByCategory,
        List<MonthlyTrendResponse> monthlyTrend,
        List<TransactionResponse> recentTransactions
) {}
