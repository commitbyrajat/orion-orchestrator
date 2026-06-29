package com.example.msme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gst_registrations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 15)
  private String gstin;

  @Column(nullable = false)
  private String legalName;

  @Column(nullable = false)
  private String tradeName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GstRegistrationStatus registrationStatus;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal annualTurnover;

  @Column(nullable = false)
  private String state;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "udyam_registration_id", nullable = false)
  private UdyamRegistration udyamRegistration;
}
