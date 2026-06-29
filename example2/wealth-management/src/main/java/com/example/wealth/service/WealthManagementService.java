package com.example.wealth.service;

import com.example.wealth.domain.MutualFund;
import com.example.wealth.dto.HoldingResponse;
import com.example.wealth.dto.InvestmentRequest;
import com.example.wealth.dto.InvestmentSimulationResponse;
import com.example.wealth.dto.MutualFundResponse;
import com.example.wealth.dto.PortfolioSummaryResponse;
import com.example.wealth.repository.MutualFundRepository;
import com.example.wealth.repository.PortfolioHoldingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WealthManagementService {

  private final MutualFundRepository mutualFunds;
  private final PortfolioHoldingRepository holdings;

  public WealthManagementService(MutualFundRepository mutualFunds, PortfolioHoldingRepository holdings) {
    this.mutualFunds = mutualFunds;
    this.holdings = holdings;
  }

  public List<MutualFundResponse> listFunds(String category, String riskLevel, String fundHouse,
      BigDecimal maxMinimumInvestment, String sortBy) {
    var stream = mutualFunds.findAll().stream();

    if (StringUtils.hasText(category)) {
      stream = stream.filter(fund -> fund.getCategory().equalsIgnoreCase(category));
    }
    if (StringUtils.hasText(riskLevel)) {
      stream = stream.filter(fund -> fund.getRiskLevel().equalsIgnoreCase(riskLevel));
    }
    if (StringUtils.hasText(fundHouse)) {
      stream = stream.filter(fund -> fund.getFundHouse().equalsIgnoreCase(fundHouse));
    }
    if (maxMinimumInvestment != null) {
      stream = stream.filter(fund -> fund.getMinimumInvestment().compareTo(maxMinimumInvestment) <= 0);
    }

    return stream
        .sorted(comparator(sortBy))
        .map(MutualFundResponse::from)
        .toList();
  }

  public MutualFundResponse getFund(long id) {
    return MutualFundResponse.from(findFund(id));
  }

  public List<MutualFundResponse> searchFunds(String query) {
    if (!StringUtils.hasText(query)) {
      return List.of();
    }
    return mutualFunds
        .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrFundHouseContainingIgnoreCase(
            query, query, query)
        .stream()
        .sorted(comparator("name"))
        .map(MutualFundResponse::from)
        .toList();
  }

  public List<HoldingResponse> getHoldings(String investorId) {
    var investorHoldings = holdings.findByInvestorIdIgnoreCase(investorId).stream()
        .map(HoldingResponse::from)
        .toList();
    if (investorHoldings.isEmpty()) {
      throw new ResourceNotFoundException("No holdings found for investor " + investorId);
    }
    return investorHoldings;
  }

  public PortfolioSummaryResponse getPortfolioSummary(String investorId) {
    var investorHoldings = getHoldings(investorId);
    var totalCurrentValue = investorHoldings.stream()
        .map(HoldingResponse::currentValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
    var totalGainLoss = investorHoldings.stream()
        .map(HoldingResponse::unrealizedGainLoss)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);

    return new PortfolioSummaryResponse(
        investorId,
        investorHoldings.size(),
        totalCurrentValue,
        totalGainLoss,
        investorHoldings);
  }

  public InvestmentSimulationResponse simulateInvestment(InvestmentRequest request) {
    var fund = findFund(request.fundId());
    var estimatedUnits = request.amount().divide(fund.getNav(), 4, RoundingMode.HALF_UP);
    var eligible = request.amount().compareTo(fund.getMinimumInvestment()) >= 0;
    var note = eligible
        ? "Investment amount satisfies the minimum requirement for " + fund.getName() + "."
        : "Investment amount is below the minimum requirement for " + fund.getName() + ".";
    return new InvestmentSimulationResponse(
        request.investorId(),
        MutualFundResponse.from(fund),
        request.amount(),
        estimatedUnits,
        fund.getMinimumInvestment(),
        eligible,
        note);
  }

  private MutualFund findFund(long id) {
    return mutualFunds.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Mutual fund " + id + " was not found"));
  }

  private Comparator<MutualFund> comparator(String sortBy) {
    if ("return".equalsIgnoreCase(sortBy)) {
      return Comparator.comparing(MutualFund::getThreeYearReturnPercent).reversed();
    }
    if ("minimumInvestment".equalsIgnoreCase(sortBy)) {
      return Comparator.comparing(MutualFund::getMinimumInvestment);
    }
    if ("risk".equalsIgnoreCase(sortBy)) {
      return Comparator.comparing(MutualFund::getRiskLevel).thenComparing(MutualFund::getName);
    }
    return Comparator.comparing(MutualFund::getName);
  }
}
