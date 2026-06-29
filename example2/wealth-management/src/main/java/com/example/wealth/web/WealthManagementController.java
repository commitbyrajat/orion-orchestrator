package com.example.wealth.web;

import com.example.wealth.dto.ErrorResponse;
import com.example.wealth.dto.HoldingResponse;
import com.example.wealth.dto.InvestmentRequest;
import com.example.wealth.dto.InvestmentSimulationResponse;
import com.example.wealth.dto.MutualFundResponse;
import com.example.wealth.dto.PortfolioSummaryResponse;
import com.example.wealth.service.WealthManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Wealth Management", description = "Mutual fund discovery and portfolio analysis endpoints")
public class WealthManagementController {

  private final WealthManagementService service;

  public WealthManagementController(WealthManagementService service) {
    this.service = service;
  }

  @Operation(
      operationId = "wealth_list_mutual_funds",
      summary = "List mutual funds",
      description = "Returns dummy mutual fund data with optional filters for category, risk, fund house, and minimum investment.")
  @ApiResponse(responseCode = "200", description = "Mutual funds returned",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = MutualFundResponse.class))))
  @GetMapping("/funds")
  public List<MutualFundResponse> listFunds(
      @Parameter(description = "Filter by category such as Equity, Debt, or Hybrid", example = "Equity")
      @RequestParam(required = false) String category,
      @Parameter(description = "Filter by risk level such as Low, Moderate, or High", example = "Moderate")
      @RequestParam(required = false) String riskLevel,
      @Parameter(description = "Filter by asset management company", example = "WealthMint AMC")
      @RequestParam(required = false) String fundHouse,
      @Parameter(description = "Only return funds with minimum investment less than or equal to this amount", example = "1000")
      @RequestParam(required = false) BigDecimal maxMinimumInvestment,
      @Parameter(description = "Sort by name, return, minimumInvestment, or risk", example = "return")
      @RequestParam(defaultValue = "name") String sortBy) {
    return service.listFunds(category, riskLevel, fundHouse, maxMinimumInvestment, sortBy);
  }

  @Operation(
      operationId = "wealth_get_mutual_fund",
      summary = "Get mutual fund by ID",
      description = "Returns complete details for a single mutual fund.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Mutual fund returned",
          content = @Content(schema = @Schema(implementation = MutualFundResponse.class))),
      @ApiResponse(responseCode = "404", description = "Fund not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/funds/{id}")
  public MutualFundResponse getFund(
      @Parameter(description = "Mutual fund ID", example = "1") @PathVariable long id) {
    return service.getFund(id);
  }

  @Operation(
      operationId = "wealth_search_mutual_funds",
      summary = "Search mutual funds",
      description = "Searches fund name, fund category, and fund house fields.")
  @ApiResponse(responseCode = "200", description = "Matching mutual funds returned",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = MutualFundResponse.class))))
  @GetMapping("/funds/search")
  public List<MutualFundResponse> searchFunds(
      @Parameter(description = "Search text", example = "index") @RequestParam String query) {
    return service.searchFunds(query);
  }

  @Operation(
      operationId = "wealth_get_investor_holdings",
      summary = "Get investor holdings",
      description = "Returns current dummy portfolio holdings for the supplied investor identifier.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Holdings returned",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = HoldingResponse.class)))),
      @ApiResponse(responseCode = "404", description = "Investor has no holdings",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/investors/{investorId}/holdings")
  public List<HoldingResponse> getHoldings(
      @Parameter(description = "Investor identifier", example = "INV-1001") @PathVariable String investorId) {
    return service.getHoldings(investorId);
  }

  @Operation(
      operationId = "wealth_get_investor_summary",
      summary = "Get investor portfolio summary",
      description = "Returns aggregate current value and estimated gain/loss for an investor portfolio.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Portfolio summary returned",
          content = @Content(schema = @Schema(implementation = PortfolioSummaryResponse.class))),
      @ApiResponse(responseCode = "404", description = "Investor has no holdings",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/investors/{investorId}/summary")
  public PortfolioSummaryResponse getPortfolioSummary(
      @Parameter(description = "Investor identifier", example = "INV-1001") @PathVariable String investorId) {
    return service.getPortfolioSummary(investorId);
  }

  @Operation(
      operationId = "wealth_simulate_investment",
      summary = "Simulate a mutual fund investment",
      description = "Calculates estimated units and verifies whether the requested amount satisfies fund minimums.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Investment simulation returned",
          content = @Content(schema = @Schema(implementation = InvestmentSimulationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Fund not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/investments/simulate")
  @ResponseStatus(HttpStatus.CREATED)
  public InvestmentSimulationResponse simulateInvestment(@Valid @RequestBody InvestmentRequest request) {
    return service.simulateInvestment(request);
  }

  @Operation(
      operationId = "wealth_simulate_investment_query",
      summary = "Simulate a mutual fund investment from query parameters",
      description = "Query-parameter variant of investment simulation for OpenAPI-to-MCP tool adapters.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Investment simulation returned",
          content = @Content(schema = @Schema(implementation = InvestmentSimulationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Fund not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/investments/simulations")
  public InvestmentSimulationResponse simulateInvestmentFromQuery(
      @Parameter(description = "Investor identifier", example = "INV-9001") @RequestParam String investorId,
      @Parameter(description = "Mutual fund identifier", example = "1") @RequestParam Long fundId,
      @Parameter(description = "Investment amount", example = "25000") @RequestParam BigDecimal amount) {
    return service.simulateInvestment(new InvestmentRequest(investorId, fundId, amount));
  }
}
