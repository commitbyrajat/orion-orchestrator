package com.example.wealth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_holdings")
public class PortfolioHolding {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String investorId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fund_id", nullable = false)
  private MutualFund fund;

  @Column(nullable = false, precision = 14, scale = 4)
  private BigDecimal units;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal averageCostNav;

  protected PortfolioHolding() {
  }

  public PortfolioHolding(String investorId, MutualFund fund, BigDecimal units,
      BigDecimal averageCostNav) {
    this.investorId = investorId;
    this.fund = fund;
    this.units = units;
    this.averageCostNav = averageCostNav;
  }

  public Long getId() {
    return id;
  }

  public String getInvestorId() {
    return investorId;
  }

  public MutualFund getFund() {
    return fund;
  }

  public BigDecimal getUnits() {
    return units;
  }

  public BigDecimal getAverageCostNav() {
    return averageCostNav;
  }
}
