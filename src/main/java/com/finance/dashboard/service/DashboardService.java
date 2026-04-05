package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.CategorySummaryResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.finance.dashboard.dto.response.MonthlyTrendResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardSummaryResponse getDashboardSummary() {
        BigDecimal totalIncome = recordRepository.sumAmountByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumAmountByType(RecordType.EXPENSE);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<CategorySummaryResponse> incomeByCategory = recordRepository.getCategorySummaries(RecordType.INCOME);
        List<CategorySummaryResponse> expensesByCategory = recordRepository.getCategorySummaries(RecordType.EXPENSE);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .incomeByCategory(incomeByCategory)
                .expensesByCategory(expensesByCategory)
                .build();
    }

    public List<FinancialRecordResponse> getRecentRecords() {
        return recordRepository.findTop5ByOrderByDateDescCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MonthlyTrendResponse> getMonthlyTrends() {
        List<Object[]> rawData = recordRepository.getMonthlyAggregation();
        Map<String, MonthlyTrendResponse> trendMap = new HashMap<>();

        for (Object[] row : rawData) {
            int year = (Integer) row[0];
            int month = (Integer) row[1];
            RecordType type = (RecordType) row[2];
            BigDecimal total = (BigDecimal) row[3];

            String period = year + "-" + String.format("%02d", month);

            MonthlyTrendResponse trend = trendMap.getOrDefault(period,
                    new MonthlyTrendResponse(period, year, month, BigDecimal.ZERO, BigDecimal.ZERO));

            if (type == RecordType.INCOME) {
                trend.setTotalIncome(total);
            } else if (type == RecordType.EXPENSE) {
                trend.setTotalExpenses(total);
            }

            trendMap.put(period, trend);
        }

        List<MonthlyTrendResponse> sortedTrends = new ArrayList<>(trendMap.values());
        sortedTrends.sort(Comparator.comparing(MonthlyTrendResponse::getPeriod));

        return sortedTrends;
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdBy(record.getUser().getName())
                .build();
    }
}