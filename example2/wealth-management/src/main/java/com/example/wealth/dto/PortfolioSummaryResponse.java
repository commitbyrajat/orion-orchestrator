package com.example.wealth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Aggregated investor portfolio summary")
public record PortfolioSummaryResponse(
    @Schema(description = "Investor identifier", example = "INV-1001") String investorId,
    @Schema(description = "Number of holdings", example = "3") int holdingCount,
    @Schema(description = "Total current portfolio value", example = "87620.54") BigDecimal totalCurrentValue,
    @Schema(description = "Total estimated gain or loss", example = "12420.50") BigDecimal totalUnrealizedGainLoss,
    @Schema(description = "Current holdings") List<HoldingResponse> holdings) {
}
