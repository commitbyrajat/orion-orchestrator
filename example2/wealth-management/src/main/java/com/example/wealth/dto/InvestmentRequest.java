package com.example.wealth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Investment simulation request")
public record InvestmentRequest(
    @NotBlank
    @Schema(description = "Investor identifier", example = "INV-9001") String investorId,
    @NotNull
    @Schema(description = "Mutual fund identifier", example = "1") Long fundId,
    @NotNull
    @DecimalMin(value = "1.00")
    @Schema(description = "Investment amount", example = "25000.00") BigDecimal amount) {
}
