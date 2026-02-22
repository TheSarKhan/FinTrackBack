package az.sarkhan.fintechsark.controller;

import az.sarkhan.fintechsark.dto.response.CategoryExpenseResponse;
import az.sarkhan.fintechsark.dto.response.DashboardStatsResponse;
import az.sarkhan.fintechsark.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** Full dashboard stats (totals + charts + last 10 transactions) */
    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /** Period-based stats: 1M, 6M, 1Y, 5Y, ALL */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStatsByPeriod(
            @RequestParam(defaultValue = "1M") String period) {
        return ResponseEntity.ok(dashboardService.getStatsByPeriod(period));
    }

    /** Expense pie chart grouped by parent category */
    @GetMapping("/expense-by-category")
    public ResponseEntity<List<CategoryExpenseResponse>> getExpenseByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getExpenseByParentCategory(startDate, endDate));
    }

    /** Drill-down: subcategory breakdown for a specific parent category */
    @GetMapping("/expense-by-category/{parentId}/drilldown")
    public ResponseEntity<List<CategoryExpenseResponse>> getDrilldown(
            @PathVariable Long parentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getDrilldown(parentId, startDate, endDate));
    }
}