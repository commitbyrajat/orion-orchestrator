package com.example.msme.controller;

import com.example.msme.dto.KycResponse;
import com.example.msme.service.KycService;
import com.example.msme.util.PanValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "PAN and UDYAM registration lookup")
public class KycController {

  private final KycService kycService;

  @GetMapping("/pan/{pan}")
  @Operation(operationId = "msme_get_kyc_by_pan", summary = "Get UDYAM KYC details for a PAN")
  public KycResponse getKyc(@PathVariable @Pattern(regexp = PanValidator.PAN_REGEX) String pan) {
    return kycService.getKycByPan(pan);
  }
}
