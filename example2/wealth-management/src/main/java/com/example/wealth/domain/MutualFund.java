package com.example.wealth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "mutual_funds")
public class MutualFund {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String subCategory;

  @Column(nullable = false)
  private String riskLevel;

  @Column(nullable = false)
  private String fundHouse;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal oneYearReturnPercent;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal threeYearReturnPercent;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal nav;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal minimumInvestment;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal expenseRatioPercent;

  @Column(nullable = false, length = 500)
  private String objective;

  protected MutualFund() {
  }

  public MutualFund(String name, String category, String subCategory, String riskLevel,
      String fundHouse, BigDecimal oneYearReturnPercent, BigDecimal threeYearReturnPercent,
      BigDecimal nav, BigDecimal minimumInvestment, BigDecimal expenseRatioPercent,
      String objective) {
    this.name = name;
    this.category = category;
    this.subCategory = subCategory;
    this.riskLevel = riskLevel;
    this.fundHouse = fundHouse;
    this.oneYearReturnPercent = oneYearReturnPercent;
    this.threeYearReturnPercent = threeYearReturnPercent;
    this.nav = nav;
    this.minimumInvestment = minimumInvestment;
    this.expenseRatioPercent = expenseRatioPercent;
    this.objective = objective;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCategory() {
    return category;
  }

  public String getSubCategory() {
    return subCategory;
  }

  public String getRiskLevel() {
    return riskLevel;
  }

  public String getFundHouse() {
    return fundHouse;
  }

  public BigDecimal getOneYearReturnPercent() {
    return oneYearReturnPercent;
  }

  public BigDecimal getThreeYearReturnPercent() {
    return threeYearReturnPercent;
  }

  public BigDecimal getNav() {
    return nav;
  }

  public BigDecimal getMinimumInvestment() {
    return minimumInvestment;
  }

  public BigDecimal getExpenseRatioPercent() {
    return expenseRatioPercent;
  }

  public String getObjective() {
    return objective;
  }
}
