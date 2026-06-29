package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Schema(description = "Inputs collected from KYC, GST, and Accounts APIs for overdraft eligibility evaluation")
public record OverdraftEligibilityRequest(
    @NotNull @Positive Long kycId,
    @NotBlank @Pattern(regexp = "[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]") String gstNumber,
    @NotEmpty List<@NotNull @Positive Long> accountIds) {
}
