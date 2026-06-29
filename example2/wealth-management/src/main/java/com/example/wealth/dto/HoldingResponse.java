package com.example.wealth.dto;

import com.example.wealth.domain.PortfolioHolding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Single investor portfolio holding")
public record HoldingResponse(
    @Schema(description = "Holding identifier", example = "1") Long id,
    @Schema(description = "Investor identifier", example = "INV-1001") String investorId,
    @Schema(description = "Owned mutual fund") MutualFundResponse fund,
    @Schema(description = "Units held", example = "220.7500") BigDecimal units,
    @Schema(description = "Average purchase NAV", example = "95.10") BigDecimal averageCostNav,
    @Schema(description = "Current holding value", example = "23958.11") BigDecimal currentValue,
    @Schema(description = "Estimated gain or loss", example = "2958.11") BigDecimal unrealizedGainLoss) {

  public static HoldingResponse from(PortfolioHolding holding) {
    var currentValue = holding.getUnits().multiply(holding.getFund().getNav()).setScale(2, RoundingMode.HALF_UP);
    var investedValue = holding.getUnits().multiply(holding.getAverageCostNav()).setScale(2, RoundingMode.HALF_UP);
    return new HoldingResponse(
        holding.getId(),
        holding.getInvestorId(),
        MutualFundResponse.from(holding.getFund()),
        holding.getUnits(),
        holding.getAverageCostNav(),
        currentValue,
        currentValue.subtract(investedValue));
  }
}
