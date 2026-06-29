package com.example.msme.dto;

import com.example.msme.entity.GstRegistrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "GST registration linked through UDYAM onboarding")
public record GstRegistrationDto(
    String gstNumber,
    String legalName,
    String tradeName,
    GstRegistrationStatus registrationStatus,
    BigDecimal annualTurnover,
    String state) {
}
