package com.example.wealth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WealthManagementApplicationTests {

  @Autowired
  private MockMvc mvc;

  @Test
  void exposesSeededMutualFunds() throws Exception {
    mvc.perform(get("/api/funds").param("riskLevel", "Moderate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].riskLevel").value("Moderate"));
  }

  @Test
  void simulatesInvestment() throws Exception {
    mvc.perform(post("/api/investments/simulate")
            .contentType("application/json")
            .content("""
                {"investorId":"INV-9001","fundId":1,"amount":25000}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.eligible").value(true))
        .andExpect(jsonPath("$.estimatedUnits").exists());
  }

  @Test
  void simulatesInvestmentFromQueryParameters() throws Exception {
    mvc.perform(get("/api/investments/simulations")
            .param("investorId", "INV-9001")
            .param("fundId", "1")
            .param("amount", "25000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eligible").value(true))
        .andExpect(jsonPath("$.estimatedUnits").exists());
  }

  @Test
  void publishesOpenApiSpec() throws Exception {
    var response = mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(response).contains("wealth_list_mutual_funds");
    assertThat(response).contains("Wealth Management Mutual Fund API");
  }
}
