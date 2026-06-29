package com.example.msme.dto;

import com.example.msme.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Bank account transaction")
public record TransactionDto(
    String transactionId,
    LocalDate date,
    BigDecimal amount,
    TransactionType type,
    String narration) {
}
