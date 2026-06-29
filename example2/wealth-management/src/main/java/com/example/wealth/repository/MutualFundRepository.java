package com.example.wealth.repository;

import com.example.wealth.domain.MutualFund;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {

  List<MutualFund> findByCategoryIgnoreCase(String category);

  List<MutualFund> findByRiskLevelIgnoreCase(String riskLevel);

  List<MutualFund> findByFundHouseIgnoreCase(String fundHouse);

  List<MutualFund> findByMinimumInvestmentLessThanEqual(BigDecimal maxMinimumInvestment);

  List<MutualFund> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrFundHouseContainingIgnoreCase(
      String name, String category, String fundHouse);
}
