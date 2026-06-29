package com.example.msme.service;

import com.example.msme.dto.AccountResponse;
import com.example.msme.entity.BankAccount;
import com.example.msme.entity.PanHolder;
import com.example.msme.exception.InvalidRequestException;
import com.example.msme.exception.MissingDataException;
import com.example.msme.exception.PanNotFoundException;
import com.example.msme.exception.ResourceNotFoundException;
import com.example.msme.mapper.OnboardingMapper;
import com.example.msme.repository.PanHolderRepository;
import com.example.msme.util.PanValidator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final PanHolderRepository panHolderRepository;
  private final OnboardingMapper mapper;

  @Transactional(readOnly = true)
  public AccountResponse getAccountsByPan(String pan) {
    return mapper.toAccountResponse(getHolderWithAccounts(pan));
  }

  @Transactional(readOnly = true)
  public List<BankAccount> getAccountsForEligibility(String pan) {
    PanHolder holder = getHolderWithAccounts(pan);
    if (holder.getBankAccounts().isEmpty()) {
      throw new MissingDataException("No bank accounts found for PAN " + holder.getPanNumber());
    }
    return holder.getBankAccounts().stream().toList();
  }

  @Transactional(readOnly = true)
  public List<BankAccount> getAccountsForEligibility(Long kycId, List<Long> accountIds) {
    PanHolder holder = getHolderWithAccounts(kycId);
    if (holder.getBankAccounts().isEmpty()) {
      throw new MissingDataException("No bank accounts found for KYC id " + kycId);
    }

    Set<Long> requestedIds = new LinkedHashSet<>(accountIds);
    if (requestedIds.size() != accountIds.size()) {
      throw new InvalidRequestException("accountIds must not contain duplicate values");
    }

    List<BankAccount> selectedAccounts = holder.getBankAccounts().stream()
        .filter(account -> requestedIds.contains(account.getId()))
        .toList();
    if (selectedAccounts.size() != requestedIds.size()) {
      throw new MissingDataException("One or more account ids are not linked to KYC id " + kycId);
    }
    return selectedAccounts;
  }

  private PanHolder getHolderWithAccounts(String pan) {
    String normalizedPan = PanValidator.normalizeAndValidate(pan);
    return panHolderRepository.findWithAccountsByPanNumber(normalizedPan)
        .orElseThrow(() -> new PanNotFoundException(normalizedPan));
  }

  private PanHolder getHolderWithAccounts(Long kycId) {
    return panHolderRepository.findWithAccountsById(kycId)
        .orElseThrow(() -> new ResourceNotFoundException("KYC record not found: " + kycId));
  }
}
