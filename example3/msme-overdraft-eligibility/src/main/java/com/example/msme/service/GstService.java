package com.example.msme.service;

import com.example.msme.dto.GstResponse;
import com.example.msme.entity.GstRegistration;
import com.example.msme.entity.PanHolder;
import com.example.msme.exception.MissingDataException;
import com.example.msme.mapper.OnboardingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GstService {

  private final KycService kycService;
  private final OnboardingMapper mapper;

  @Transactional(readOnly = true)
  public GstResponse getGstByPan(String pan) {
    return mapper.toGstResponse(getActiveGstHolder(pan));
  }

  @Transactional(readOnly = true)
  public List<GstRegistration> getGstRegistrationsForEligibility(String pan) {
    return getActiveGstHolder(pan).getUdyamRegistration().getGstRegistrations().stream().toList();
  }

  @Transactional(readOnly = true)
  public GstRegistration getGstRegistrationForEligibility(Long kycId, String gstNumber) {
    PanHolder holder = kycService.getPanHolderWithUdyam(kycId);
    if (holder.getUdyamRegistration().getGstRegistrations().isEmpty()) {
      throw new MissingDataException("No GST registrations found for KYC id " + kycId);
    }
    String normalizedGstNumber = gstNumber.trim().toUpperCase();
    return holder.getUdyamRegistration().getGstRegistrations().stream()
        .filter(gst -> gst.getGstin().equals(normalizedGstNumber))
        .findFirst()
        .orElseThrow(() -> new MissingDataException("GST number " + normalizedGstNumber + " is not linked to KYC id " + kycId));
  }

  private PanHolder getActiveGstHolder(String pan) {
    PanHolder holder = kycService.getPanHolderWithUdyam(pan);
    if (holder.getUdyamRegistration().getGstRegistrations().isEmpty()) {
      throw new MissingDataException("No GST registrations found for PAN " + holder.getPanNumber());
    }
    return holder;
  }
}
