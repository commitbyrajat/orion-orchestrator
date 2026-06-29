package com.example.msme.service;

import com.example.msme.dto.KycResponse;
import com.example.msme.entity.PanHolder;
import com.example.msme.exception.MissingDataException;
import com.example.msme.exception.PanNotFoundException;
import com.example.msme.exception.ResourceNotFoundException;
import com.example.msme.mapper.OnboardingMapper;
import com.example.msme.repository.PanHolderRepository;
import com.example.msme.util.PanValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KycService {

  private final PanHolderRepository panHolderRepository;
  private final OnboardingMapper mapper;

  @Transactional(readOnly = true)
  public KycResponse getKycByPan(String pan) {
    return mapper.toKycResponse(getPanHolderWithUdyam(pan));
  }

  @Transactional(readOnly = true)
  public PanHolder getPanHolderWithUdyam(String pan) {
    String normalizedPan = PanValidator.normalizeAndValidate(pan);
    PanHolder holder = panHolderRepository.findWithKycByPanNumber(normalizedPan)
        .orElseThrow(() -> new PanNotFoundException(normalizedPan));
    if (holder.getUdyamRegistration() == null) {
      throw new MissingDataException("No UDYAM registration found for PAN " + normalizedPan);
    }
    return holder;
  }

  @Transactional(readOnly = true)
  public PanHolder getPanHolderWithUdyam(Long kycId) {
    PanHolder holder = getPanHolderIfPresent(kycId);
    if (holder.getUdyamRegistration() == null) {
      throw new MissingDataException("No UDYAM registration found for KYC id " + kycId);
    }
    return holder;
  }

  @Transactional(readOnly = true)
  public PanHolder getPanHolderIfPresent(String pan) {
    String normalizedPan = PanValidator.normalizeAndValidate(pan);
    return panHolderRepository.findWithKycByPanNumber(normalizedPan)
        .orElseThrow(() -> new PanNotFoundException(normalizedPan));
  }

  @Transactional(readOnly = true)
  public PanHolder getPanHolderIfPresent(Long kycId) {
    return panHolderRepository.findWithKycById(kycId)
        .orElseThrow(() -> new ResourceNotFoundException("KYC record not found: " + kycId));
  }
}
