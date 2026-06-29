package com.example.msme.dto;

import com.example.msme.entity.ConstitutionType;
import com.example.msme.entity.EnterpriseType;
import com.example.msme.entity.UdyamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Complete PAN and UDYAM KYC view")
public record KycResponse(
    Long kycId,
    String panNumber,
    String holderName,
    String businessName,
    ConstitutionType constitutionType,
    String mobile,
    String email,
    String udyamNumber,
    EnterpriseType enterpriseType,
    String enterpriseName,
    LocalDate registrationDate,
    BigDecimal investment,
    BigDecimal turnover,
    UdyamStatus status,
    List<BusinessLocationDto> businessLocations) {
}
