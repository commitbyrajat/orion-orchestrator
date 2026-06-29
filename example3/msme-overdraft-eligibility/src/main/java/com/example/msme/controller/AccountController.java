package com.example.msme.controller;

import com.example.msme.dto.AccountResponse;
import com.example.msme.service.AccountService;
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
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank accounts and transaction history")
public class AccountController {

  private final AccountService accountService;

  @GetMapping("/pan/{pan}")
  @Operation(operationId = "msme_get_accounts_by_pan", summary = "Get all bank accounts and transactions for a PAN")
  public AccountResponse getAccounts(@PathVariable @Pattern(regexp = PanValidator.PAN_REGEX) String pan) {
    return accountService.getAccountsByPan(pan);
  }
}
