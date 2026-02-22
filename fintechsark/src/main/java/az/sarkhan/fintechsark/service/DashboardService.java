package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.response.CategoryExpenseResponse;
import az.sarkhan.fintechsark.dto.response.DashboardStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getStats();
    List<CategoryExpenseResponse> getExpenseByParentCategory(LocalDate startDate, LocalDate endDate);
    List<CategoryExpenseResponse> getDrilldown(Long parentCategoryId, LocalDate startDate, LocalDate endDate);
    DashboardStatsResponse getStatsByPeriod(String period);
}
