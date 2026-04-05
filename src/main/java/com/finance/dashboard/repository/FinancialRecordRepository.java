package com.finance.dashboard.repository;

import com.finance.dashboard.dto.response.CategorySummaryResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {
    @Query("SELECT SUM(f.amount) FROM FinancialRecord f WHERE f.type = :type")
    BigDecimal sumAmountByType(@Param("type") RecordType type);

    @Query("SELECT new com.finance.dashboard.dto.response.CategorySummaryResponse(f.category, SUM(f.amount)) " +
            "FROM FinancialRecord f WHERE f.type = :type GROUP BY f.category")
    List<CategorySummaryResponse> getCategorySummaries(@Param("type") RecordType type);

    List<FinancialRecord> findTop5ByOrderByDateDescCreatedAtDesc();

    @Query("SELECT YEAR(f.date), MONTH(f.date), f.type, SUM(f.amount) " +
            "FROM FinancialRecord f " +
            "GROUP BY YEAR(f.date), MONTH(f.date), f.type")
    List<Object[]> getMonthlyAggregation();

}