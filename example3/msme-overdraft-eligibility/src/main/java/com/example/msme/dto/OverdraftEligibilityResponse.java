package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "MSME overdraft eligibility decision")
public record OverdraftEligibilityResponse(
    boolean eligible,
    int score,
    BigDecimal maximumEligibleAmount,
    List<String> reasons) {
}
