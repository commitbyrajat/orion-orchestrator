package com.example.msme.service;

import com.example.msme.dto.OverdraftEligibilityRequest;
import com.example.msme.dto.OverdraftEligibilityResponse;
import com.example.msme.entity.AccountStatus;
import com.example.msme.entity.AccountTransaction;
import com.example.msme.entity.AccountType;
import com.example.msme.entity.BankAccount;
import com.example.msme.entity.GstRegistration;
import com.example.msme.entity.GstRegistrationStatus;
import com.example.msme.entity.PanHolder;
import com.example.msme.entity.TransactionType;
import com.example.msme.entity.UdyamStatus;
import com.example.msme.exception.MissingDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdraftEligibilityService {

  private static final BigDecimal AVG_BALANCE_THRESHOLD = new BigDecimal("200000");
  private static final BigDecimal GST_TURNOVER_THRESHOLD = new BigDecimal("5000000");
  private static final BigDecimal AVG_MONTHLY_CREDIT_THRESHOLD = new BigDecimal("500000");
  private static final int ELIGIBILITY_SCORE = 70;

  private final KycService kycService;
  private final GstService gstService;
  private final AccountService accountService;
  private final Clock clock;

  public OverdraftEligibilityResponse evaluate(OverdraftEligibilityRequest request) {
    List<String> reasons = new ArrayList<>();
    int score = 0;

    PanHolder holder;
    try {
      holder = kycService.getPanHolderIfPresent(request.kycId());
    } catch (MissingDataException ex) {
      return rejected("No UDYAM registration found. UDYAM is mandatory for MSME overdraft eligibility.");
    }

    // UDYAM is the hard onboarding gate for this MSME product. A missing or inactive
    // registration is rejected before score calculation because the applicant is not
    // treated as a verified MSME borrower.
    if (holder.getUdyamRegistration() == null || holder.getUdyamRegistration().getStatus() != UdyamStatus.ACTIVE) {
      return rejected("No active UDYAM registration found. UDYAM is mandatory for MSME overdraft eligibility.");
    }

    // GST is modeled as discovered through UDYAM details. Missing GST is also a hard
    // reject because turnover-based line sizing depends on active GST registrations.
    GstRegistration gstRegistration;
    try {
      gstRegistration = gstService.getGstRegistrationForEligibility(request.kycId(), request.gstNumber());
    } catch (MissingDataException ex) {
      return rejected("No GST registration found through UDYAM details. GST is mandatory for this product.");
    }

    List<BankAccount> accounts = accountService.getAccountsForEligibility(request.kycId(), request.accountIds());
    BigDecimal annualTurnover = activeAnnualTurnover(List.of(gstRegistration));
    BigDecimal averageMonthlyBalance = averageMonthlyBalance(accounts);
    BigDecimal averageMonthlyCredits = averageMonthlyCredits(accounts);
    long creditCount = creditCountInLastSixMonths(accounts);

    // The weighted score intentionally separates liquidity, scale, vintage,
    // account conduct, transaction velocity, and credit inflow quality. This keeps
    // the decision explainable and makes each reason traceable to a banking rule.
    if (averageMonthlyBalance.compareTo(AVG_BALANCE_THRESHOLD) >= 0) {
      score += 20;
      reasons.add("+20 average monthly balance is at least INR 2,00,000.");
    } else {
      reasons.add("Average monthly balance is below INR 2,00,000.");
    }

    if (annualTurnover.compareTo(GST_TURNOVER_THRESHOLD) >= 0) {
      score += 20;
      reasons.add("+20 active GST annual turnover is at least INR 50 lakh.");
    } else {
      reasons.add("Active GST annual turnover is below INR 50 lakh.");
    }

    long businessAgeYears = ChronoUnit.YEARS.between(holder.getUdyamRegistration().getRegistrationDate(), LocalDate.now(clock));
    if (businessAgeYears >= 2) {
      score += 10;
      reasons.add("+10 business age is at least 2 years.");
    } else {
      reasons.add("Business age is less than 2 years.");
    }

    if (hasActiveCurrentAccount(accounts)) {
      score += 10;
      reasons.add("+10 active current account found.");
    } else {
      reasons.add("No active current account found.");
    }

    if (creditCount > 50) {
      score += 15;
      reasons.add("+15 more than 50 credit transactions in the last 6 months.");
    } else {
      reasons.add("Credit transactions in the last 6 months are not more than 50.");
    }

    if (accounts.stream().noneMatch(account -> account.getCurrentBalance().signum() < 0)) {
      score += 10;
      reasons.add("+10 no bank account has a negative balance.");
    } else {
      reasons.add("At least one bank account has a negative balance.");
    }

    if (averageMonthlyCredits.compareTo(AVG_MONTHLY_CREDIT_THRESHOLD) >= 0) {
      score += 15;
      reasons.add("+15 average monthly credits are at least INR 5 lakh.");
    } else {
      reasons.add("Average monthly credits are below INR 5 lakh.");
    }

    boolean eligible = score >= ELIGIBILITY_SCORE;
    if (!eligible) {
      reasons.add("Final score is below the eligibility threshold of 70.");
    }

    // Line size is capped by both turnover and bank-credit behavior. The smaller
    // value prevents high-turnover but low-bank-flow applicants from receiving an
    // oversized overdraft line.
    BigDecimal maximumEligibleAmount = eligible
        ? annualTurnover.multiply(new BigDecimal("0.20")).min(averageMonthlyCredits.multiply(new BigDecimal("6"))).setScale(2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    return new OverdraftEligibilityResponse(eligible, score, maximumEligibleAmount, reasons);
  }

  private OverdraftEligibilityResponse rejected(String reason) {
    return new OverdraftEligibilityResponse(false, 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), List.of(reason));
  }

  private BigDecimal activeAnnualTurnover(List<GstRegistration> gstRegistrations) {
    return gstRegistrations.stream()
        .filter(gst -> gst.getRegistrationStatus() == GstRegistrationStatus.ACTIVE)
        .map(GstRegistration::getAnnualTurnover)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal averageMonthlyBalance(List<BankAccount> accounts) {
    return accounts.stream()
        .map(BankAccount::getAverageMonthlyBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(Math.max(accounts.size(), 1)), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal averageMonthlyCredits(List<BankAccount> accounts) {
    BigDecimal credits = transactionsInLastSixMonths(accounts).stream()
        .filter(transaction -> transaction.getType() == TransactionType.CREDIT)
        .map(AccountTransaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return credits.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
  }

  private long creditCountInLastSixMonths(List<BankAccount> accounts) {
    return transactionsInLastSixMonths(accounts).stream()
        .filter(transaction -> transaction.getType() == TransactionType.CREDIT)
        .count();
  }

  private boolean hasActiveCurrentAccount(List<BankAccount> accounts) {
    return accounts.stream()
        .anyMatch(account -> account.getAccountType() == AccountType.CURRENT
            && account.getAccountStatus() == AccountStatus.ACTIVE);
  }

  private List<AccountTransaction> transactionsInLastSixMonths(List<BankAccount> accounts) {
    LocalDate fromDate = LocalDate.now(clock).minusMonths(6);
    return accounts.stream()
        .flatMap(account -> account.getTransactions().stream())
        .filter(transaction -> !transaction.getDate().isBefore(fromDate))
        .toList();
  }
}
