package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "All bank accounts and transactions linked to a PAN")
public record AccountResponse(String panNumber, String businessName, List<BankAccountDto> accounts) {
}
