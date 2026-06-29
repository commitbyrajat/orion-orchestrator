package com.example.msme.dto;

import com.example.msme.entity.AccountStatus;
import com.example.msme.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Bank account with transaction history")
public record BankAccountDto(
    Long accountId,
    String accountNumber,
    String bankName,
    String ifsc,
    AccountType accountType,
    BigDecimal currentBalance,
    BigDecimal averageMonthlyBalance,
    AccountStatus accountStatus,
    List<TransactionDto> transactions) {
}
