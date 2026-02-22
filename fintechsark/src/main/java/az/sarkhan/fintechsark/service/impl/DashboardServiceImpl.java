package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.response.*;
import az.sarkhan.fintechsark.repository.TransactionRepository;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;
    private final TransactionServiceImpl transactionService;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        return getStatsByPeriod("ALL");
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStatsByPeriod(String period) {
        Long userId = securityUtils.getCurrentUserId();

        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = switch (period.toUpperCase()) {
            case "1M" -> endDate.minusMonths(1);
            case "6M" -> endDate.minusMonths(6);
            case "1Y" -> endDate.minusYears(1);
            case "5Y" -> endDate.minusYears(5);
            default   -> null; // ALL — tarix filteri yoxdur
        };

        // ALL üçün filtersiz, digərləri üçün period ilə
        BigDecimal totalIncome = startDate == null
                ? transactionRepository.sumIncomeByUser(userId)
                : transactionRepository.sumIncomeByPeriod(userId, startDate, endDate);

        BigDecimal totalExpense = startDate == null
                ? transactionRepository.sumExpenseByUser(userId)
                : transactionRepository.sumExpenseByPeriod(userId, startDate, endDate);

        BigDecimal netBalance          = totalIncome.subtract(totalExpense);
        BigDecimal currentMonthExpense = transactionRepository.sumCurrentMonthExpense(userId);

        List<CategoryExpenseResponse> expenseByCategory =
                getExpenseByParentCategory(startDate, endDate);

        List<MonthlyTrendResponse> monthlyTrend = startDate == null
                ? buildMonthlyTrend(userId)
                : buildMonthlyTrendByPeriod(userId, startDate);

        List<TransactionResponse> recent = transactionRepository
                .findTop10ByUserId(userId, PageRequest.of(0, 10))
                .stream()
                .map(transactionService::toResponse)
                .toList();

        return new DashboardStatsResponse(
                totalIncome, totalExpense, netBalance, currentMonthExpense,
                expenseByCategory, monthlyTrend, recent
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getExpenseByParentCategory(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUtils.getCurrentUserId();

        // null ötürülsə PostgreSQL CAST(null AS date) IS NULL → true, bütün tarixlər gəlir
        String start = startDate != null ? startDate.toString() : null;
        String end   = endDate   != null ? endDate.toString()   : null;

        List<Object[]> rows = transactionRepository.findExpenseByParentCategory(userId, start, end);

        BigDecimal grandTotal = rows.stream()
                .map(r -> (BigDecimal) r[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            Long catId       = ((Number) r[0]).longValue();
            String catName   = (String) r[1];
            BigDecimal total = (BigDecimal) r[2];
            double pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            return new CategoryExpenseResponse(catId, catName, total, pct, null);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getDrilldown(Long parentCategoryId, LocalDate startDate, LocalDate endDate) {
        Long userId = securityUtils.getCurrentUserId();
        LocalDate start = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        LocalDate end   = endDate   != null ? endDate   : LocalDate.now();

        List<Object[]> rows = transactionRepository.findExpenseBySubcategory(userId, parentCategoryId, start, end);

        BigDecimal parentTotal = rows.stream()
                .map(r -> (BigDecimal) r[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            Long catId       = ((Number) r[0]).longValue();
            String catName   = (String) r[1];
            BigDecimal total = (BigDecimal) r[2];
            double pct = parentTotal.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : total.divide(parentTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            return new CategoryExpenseResponse(catId, catName, total, pct, null);
        }).collect(Collectors.toList());
    }

    // ALL period — son 6 ay
    private List<MonthlyTrendResponse> buildMonthlyTrend(Long userId) {
        return mapToMonthlyTrend(transactionRepository.findMonthlyTrend(userId));
    }

    // Müəyyən startDate-dən etibarən
    private List<MonthlyTrendResponse> buildMonthlyTrendByPeriod(Long userId, LocalDate startDate) {
        return mapToMonthlyTrend(transactionRepository.findMonthlyTrendByPeriod(userId, startDate));
    }

    private List<MonthlyTrendResponse> mapToMonthlyTrend(List<Object[]> rows) {
        Map<String, BigDecimal> incomeMap  = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String month     = (String) row[0];
            String type      = (String) row[1];
            BigDecimal total = (BigDecimal) row[2];
            if ("INCOME".equals(type)) incomeMap.put(month, total);
            else                       expenseMap.put(month, total);
        }

        Set<String> allMonths = new LinkedHashSet<>();
        allMonths.addAll(incomeMap.keySet());
        allMonths.addAll(expenseMap.keySet());

        return allMonths.stream().sorted().map(month -> new MonthlyTrendResponse(
                month,
                incomeMap.getOrDefault(month, BigDecimal.ZERO),
                expenseMap.getOrDefault(month, BigDecimal.ZERO)
        )).toList();
    }
}