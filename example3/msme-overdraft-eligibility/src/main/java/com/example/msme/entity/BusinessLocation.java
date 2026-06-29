package com.example.msme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String addressLine1;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String state;

  @Column(nullable = false)
  private String pincode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "udyam_registration_id", nullable = false)
  private UdyamRegistration udyamRegistration;
}
