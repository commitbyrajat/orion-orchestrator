package com.example.wealth.config;

import com.example.wealth.domain.MutualFund;
import com.example.wealth.domain.PortfolioHolding;
import com.example.wealth.repository.MutualFundRepository;
import com.example.wealth.repository.PortfolioHoldingRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

  private final MutualFundRepository mutualFunds;
  private final PortfolioHoldingRepository holdings;

  public DataSeeder(MutualFundRepository mutualFunds, PortfolioHoldingRepository holdings) {
    this.mutualFunds = mutualFunds;
    this.holdings = holdings;
  }

  @Override
  public void run(String... args) {
    if (mutualFunds.count() > 0) {
      return;
    }

    var bluechip = mutualFunds.save(new MutualFund(
        "WM Bluechip Equity Fund",
        "Equity",
        "Large Cap",
        "High",
        "WealthMint AMC",
        new BigDecimal("18.42"),
        new BigDecimal("14.70"),
        new BigDecimal("108.54"),
        new BigDecimal("5000.00"),
        new BigDecimal("0.85"),
        "Diversified large-cap equity fund for long-term capital growth."));

    var balanced = mutualFunds.save(new MutualFund(
        "WM Balanced Advantage Fund",
        "Hybrid",
        "Dynamic Asset Allocation",
        "Moderate",
        "WealthMint AMC",
        new BigDecimal("11.35"),
        new BigDecimal("10.12"),
        new BigDecimal("57.89"),
        new BigDecimal("1000.00"),
        new BigDecimal("0.72"),
        "Hybrid fund that dynamically allocates between equity and debt."));

    var liquid = mutualFunds.save(new MutualFund(
        "WM Liquid Treasury Fund",
        "Debt",
        "Liquid",
        "Low",
        "NorthStar Mutual",
        new BigDecimal("6.21"),
        new BigDecimal("5.91"),
        new BigDecimal("34.22"),
        new BigDecimal("500.00"),
        new BigDecimal("0.18"),
        "Low duration liquid fund for short-term parking and emergency reserves."));

    var index = mutualFunds.save(new MutualFund(
        "WM Nifty Index Fund",
        "Equity",
        "Index",
        "Moderate",
        "NorthStar Mutual",
        new BigDecimal("15.64"),
        new BigDecimal("13.24"),
        new BigDecimal("82.11"),
        new BigDecimal("1000.00"),
        new BigDecimal("0.21"),
        "Passive index fund tracking a broad Indian equity benchmark."));

    var taxSaver = mutualFunds.save(new MutualFund(
        "WM Tax Saver ELSS Fund",
        "Equity",
        "ELSS",
        "High",
        "Harbor Wealth",
        new BigDecimal("16.81"),
        new BigDecimal("12.95"),
        new BigDecimal("73.44"),
        new BigDecimal("500.00"),
        new BigDecimal("0.96"),
        "ELSS tax-saving equity fund with a statutory lock-in period."));

    var income = mutualFunds.save(new MutualFund(
        "WM Corporate Bond Income Fund",
        "Debt",
        "Corporate Bond",
        "Low",
        "Harbor Wealth",
        new BigDecimal("7.82"),
        new BigDecimal("7.31"),
        new BigDecimal("41.03"),
        new BigDecimal("1000.00"),
        new BigDecimal("0.38"),
        "Debt fund focused on high quality corporate bonds."));

    holdings.saveAll(List.of(
        new PortfolioHolding("INV-1001", bluechip, new BigDecimal("220.75"), new BigDecimal("95.10")),
        new PortfolioHolding("INV-1001", balanced, new BigDecimal("410.25"), new BigDecimal("51.35")),
        new PortfolioHolding("INV-1001", liquid, new BigDecimal("850.00"), new BigDecimal("33.12")),
        new PortfolioHolding("INV-2002", index, new BigDecimal("300.50"), new BigDecimal("71.22")),
        new PortfolioHolding("INV-2002", taxSaver, new BigDecimal("145.00"), new BigDecimal("65.80")),
        new PortfolioHolding("INV-3003", income, new BigDecimal("1250.00"), new BigDecimal("39.40")),
        new PortfolioHolding("INV-3003", balanced, new BigDecimal("120.75"), new BigDecimal("50.25"))));
  }
}
