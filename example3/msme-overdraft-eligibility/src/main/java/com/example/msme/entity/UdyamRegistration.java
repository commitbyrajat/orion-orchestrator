package com.example.msme.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "udyam_registrations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UdyamRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String udyamNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EnterpriseType enterpriseType;

  @Column(nullable = false)
  private String enterpriseName;

  @Column(nullable = false)
  private LocalDate registrationDate;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal investment;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal turnover;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UdyamStatus status;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pan_holder_id", nullable = false, unique = true)
  private PanHolder panHolder;

  @Builder.Default
  @Setter(AccessLevel.PRIVATE)
  @OneToMany(mappedBy = "udyamRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GstRegistration> gstRegistrations = new LinkedHashSet<>();

  @Builder.Default
  @Setter(AccessLevel.PRIVATE)
  @OneToMany(mappedBy = "udyamRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<BusinessLocation> businessLocations = new LinkedHashSet<>();

  public void addGstRegistration(GstRegistration registration) {
    gstRegistrations.add(registration);
    registration.setUdyamRegistration(this);
  }

  public void addBusinessLocation(BusinessLocation location) {
    businessLocations.add(location);
    location.setUdyamRegistration(this);
  }
}
