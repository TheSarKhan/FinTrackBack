package az.sarkhan.fintechsark.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FinancialWrappedResponse(
        int year,

        // Ümumi statistika
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal totalSaved,
        double savingsRate,

        // Kateqoriya üzrə xərclər (sıralanmış)
        List<CategoryStat> topExpenseCategories,

        // Ən çox xərc edilən ay
        String biggestExpenseMonth,
        BigDecimal biggestExpenseMonthAmount,

        // Ən çox gəlir olan ay
        String biggestIncomeMonth,
        BigDecimal biggestIncomeMonthAmount,

        // Aylıq breakdown
        List<MonthlyStat> monthlyBreakdown,

        // Maraqlı faktlar
        int totalTransactionCount,
        int expenseTransactionCount,
        int incomeTransactionCount,
        BigDecimal avgMonthlyExpense,
        BigDecimal avgMonthlyIncome,
        String mostExpensiveCategory,
        String mostFrequentCategory
) {
    public record CategoryStat(
            String categoryName,
            BigDecimal totalAmount,
            double percentage,
            int transactionCount
    ) {}

    public record MonthlyStat(
            String month,      // "2025-01"
            String monthLabel, // "Yanvar"
            BigDecimal income,
            BigDecimal expense,
            BigDecimal saved
    ) {}
}