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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pan_holders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanHolder {

  // PAN is the root identity in this demo banking onboarding graph. UDYAM, GST,
  // bank accounts, and transactions are all reachable from this aggregate root.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 10)
  private String panNumber;

  @Column(nullable = false)
  private String holderName;

  @Column(nullable = false)
  private String businessName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConstitutionType constitutionType;

  @Column(nullable = false, length = 10)
  private String mobile;

  @Column(nullable = false)
  private String email;

  @OneToOne(mappedBy = "panHolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private UdyamRegistration udyamRegistration;

  @Builder.Default
  @Setter(AccessLevel.PRIVATE)
  @OneToMany(mappedBy = "panHolder", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<BankAccount> bankAccounts = new LinkedHashSet<>();

  public void assignUdyamRegistration(UdyamRegistration registration) {
    this.udyamRegistration = registration;
    if (registration != null) {
      registration.setPanHolder(this);
    }
  }

  public void addBankAccount(BankAccount account) {
    bankAccounts.add(account);
    account.setPanHolder(this);
  }
}
