package com.example.wealth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated outcome for a proposed mutual fund investment")
public record InvestmentSimulationResponse(
    @Schema(description = "Investor identifier", example = "INV-9001") String investorId,
    @Schema(description = "Fund used for simulation") MutualFundResponse fund,
    @Schema(description = "Requested investment amount", example = "25000.00") BigDecimal amount,
    @Schema(description = "Units that could be purchased at current NAV", example = "230.3298") BigDecimal estimatedUnits,
    @Schema(description = "Minimum investment required", example = "5000.00") BigDecimal minimumInvestment,
    @Schema(description = "Whether amount satisfies minimum investment rules", example = "true") boolean eligible,
    @Schema(description = "Human-readable suitability note") String note) {
}
