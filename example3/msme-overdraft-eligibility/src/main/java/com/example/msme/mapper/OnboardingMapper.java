package com.example.msme.mapper;

import com.example.msme.dto.AccountResponse;
import com.example.msme.dto.BankAccountDto;
import com.example.msme.dto.BusinessLocationDto;
import com.example.msme.dto.GstRegistrationDto;
import com.example.msme.dto.GstResponse;
import com.example.msme.dto.KycResponse;
import com.example.msme.dto.TransactionDto;
import com.example.msme.entity.AccountTransaction;
import com.example.msme.entity.BankAccount;
import com.example.msme.entity.BusinessLocation;
import com.example.msme.entity.GstRegistration;
import com.example.msme.entity.PanHolder;
import com.example.msme.entity.UdyamRegistration;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OnboardingMapper {

  public KycResponse toKycResponse(PanHolder holder) {
    UdyamRegistration udyam = holder.getUdyamRegistration();
    return new KycResponse(
        holder.getId(),
        holder.getPanNumber(),
        holder.getHolderName(),
        holder.getBusinessName(),
        holder.getConstitutionType(),
        holder.getMobile(),
        holder.getEmail(),
        udyam.getUdyamNumber(),
        udyam.getEnterpriseType(),
        udyam.getEnterpriseName(),
        udyam.getRegistrationDate(),
        udyam.getInvestment(),
        udyam.getTurnover(),
        udyam.getStatus(),
        udyam.getBusinessLocations().stream().map(this::toLocationDto).toList());
  }

  public GstResponse toGstResponse(PanHolder holder) {
    UdyamRegistration udyam = holder.getUdyamRegistration();
    return new GstResponse(
        holder.getPanNumber(),
        udyam.getUdyamNumber(),
        udyam.getGstRegistrations().stream()
            .sorted(Comparator.comparing(GstRegistration::getState))
            .map(this::toGstDto)
            .toList());
  }

  public AccountResponse toAccountResponse(PanHolder holder) {
    return new AccountResponse(
        holder.getPanNumber(),
        holder.getBusinessName(),
        holder.getBankAccounts().stream()
            .sorted(Comparator.comparing(BankAccount::getAccountNumber))
            .map(this::toBankAccountDto)
            .toList());
  }

  private BusinessLocationDto toLocationDto(BusinessLocation location) {
    return new BusinessLocationDto(
        location.getAddressLine1(),
        location.getCity(),
        location.getState(),
        location.getPincode());
  }

  private GstRegistrationDto toGstDto(GstRegistration gst) {
    return new GstRegistrationDto(
        gst.getGstin(),
        gst.getLegalName(),
        gst.getTradeName(),
        gst.getRegistrationStatus(),
        gst.getAnnualTurnover(),
        gst.getState());
  }

  private BankAccountDto toBankAccountDto(BankAccount account) {
    return new BankAccountDto(
        account.getId(),
        account.getAccountNumber(),
        account.getBankName(),
        account.getIfsc(),
        account.getAccountType(),
        account.getCurrentBalance(),
        account.getAverageMonthlyBalance(),
        account.getAccountStatus(),
        account.getTransactions().stream()
            .sorted(Comparator.comparing(AccountTransaction::getDate).reversed())
            .map(this::toTransactionDto)
            .toList());
  }

  private TransactionDto toTransactionDto(AccountTransaction transaction) {
    return new TransactionDto(
        transaction.getTransactionId(),
        transaction.getDate(),
        transaction.getAmount(),
        transaction.getType(),
        transaction.getNarration());
  }
}
