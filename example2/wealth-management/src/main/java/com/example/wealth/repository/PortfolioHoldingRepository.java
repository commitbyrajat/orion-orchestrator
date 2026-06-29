package com.example.wealth.repository;

import com.example.wealth.domain.PortfolioHolding;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

  @EntityGraph(attributePaths = "fund")
  List<PortfolioHolding> findByInvestorIdIgnoreCase(String investorId);
}
