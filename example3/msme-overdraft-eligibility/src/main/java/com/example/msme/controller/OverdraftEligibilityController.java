package com.example.msme.controller;

import com.example.msme.dto.OverdraftEligibilityRequest;
import com.example.msme.dto.OverdraftEligibilityResponse;
import com.example.msme.service.OverdraftEligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/overdraft")
@RequiredArgsConstructor
@Tag(name = "Overdraft Eligibility", description = "Weighted MSME overdraft eligibility scoring")
public class OverdraftEligibilityController {

  private final OverdraftEligibilityService overdraftEligibilityService;

  @PostMapping("/evaluate")
  @Operation(operationId = "msme_evaluate_overdraft", summary = "Evaluate MSME overdraft eligibility from KYC id, GST number, and account ids")
  public OverdraftEligibilityResponse evaluate(@Valid @RequestBody OverdraftEligibilityRequest request) {
    return overdraftEligibilityService.evaluate(request);
  }

  @GetMapping("/evaluations")
  @Operation(operationId = "msme_evaluate_overdraft_query", summary = "Evaluate MSME overdraft eligibility from query parameters")
  public OverdraftEligibilityResponse evaluateFromQuery(
      @RequestParam @NotNull @Positive Long kycId,
      @RequestParam @Pattern(regexp = "[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]") String gstNumber,
      @RequestParam @NotEmpty List<@NotNull @Positive Long> accountIds) {
    return overdraftEligibilityService.evaluate(new OverdraftEligibilityRequest(kycId, gstNumber, accountIds));
  }
}
