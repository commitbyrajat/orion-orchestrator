package com.example.wealth.dto;

import com.example.wealth.domain.MutualFund;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Mutual fund details available for discovery and investment simulation")
public record MutualFundResponse(
    @Schema(description = "Internal fund identifier", example = "1") Long id,
    @Schema(description = "Fund display name", example = "WM Bluechip Equity Fund") String name,
    @Schema(description = "Broad fund category", example = "Equity") String category,
    @Schema(description = "Fund sub-category", example = "Large Cap") String subCategory,
    @Schema(description = "Investor-facing risk level", example = "High") String riskLevel,
    @Schema(description = "Asset management company", example = "WealthMint AMC") String fundHouse,
    @Schema(description = "One year return percentage", example = "18.42") BigDecimal oneYearReturnPercent,
    @Schema(description = "Three year annualized return percentage", example = "14.70") BigDecimal threeYearReturnPercent,
    @Schema(description = "Latest net asset value", example = "108.54") BigDecimal nav,
    @Schema(description = "Minimum lump-sum investment amount", example = "5000.00") BigDecimal minimumInvestment,
    @Schema(description = "Expense ratio percentage", example = "0.85") BigDecimal expenseRatioPercent,
    @Schema(description = "Fund objective summary") String objective) {

  public static MutualFundResponse from(MutualFund fund) {
    return new MutualFundResponse(
        fund.getId(),
        fund.getName(),
        fund.getCategory(),
        fund.getSubCategory(),
        fund.getRiskLevel(),
        fund.getFundHouse(),
        fund.getOneYearReturnPercent(),
        fund.getThreeYearReturnPercent(),
        fund.getNav(),
        fund.getMinimumInvestment(),
        fund.getExpenseRatioPercent(),
        fund.getObjective());
  }
}
