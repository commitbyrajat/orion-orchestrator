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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String accountNumber;

  @Column(nullable = false)
  private String bankName;

  @Column(nullable = false, length = 11)
  private String ifsc;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountType accountType;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal currentBalance;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal averageMonthlyBalance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus accountStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pan_holder_id", nullable = false)
  private PanHolder panHolder;

  @Builder.Default
  @Setter(AccessLevel.PRIVATE)
  @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AccountTransaction> transactions = new LinkedHashSet<>();

  public void addTransaction(AccountTransaction transaction) {
    transactions.add(transaction);
    transaction.setBankAccount(this);
  }
}
