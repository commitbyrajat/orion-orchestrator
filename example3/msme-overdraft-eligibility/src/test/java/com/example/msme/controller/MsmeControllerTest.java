package com.example.msme.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MsmeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void kycControllerReturnsUdyamDetails() throws Exception {
    mockMvc.perform(get("/api/kyc/pan/ABCDE1234F"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kycId").value(1))
        .andExpect(jsonPath("$.udyamNumber").value("UDYAM-MH-01-0001111"));
  }

  @Test
  void overdraftControllerReturnsEligibleDecision() throws Exception {
    mockMvc.perform(post("/api/overdraft/evaluate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "kycId": 2,
                  "gstNumber": "27PQRST6789L1Z5",
                  "accountIds": [3, 4]
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eligible").value(true))
        .andExpect(jsonPath("$.score").value(100));
  }

  @Test
  void invalidOverdraftRequestReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/overdraft/evaluate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "kycId": 2,
                  "gstNumber": "bad-gst",
                  "accountIds": [3, 4]
                }
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void gstControllerReturnsUnprocessableEntityWhenGstMissing() throws Exception {
    mockMvc.perform(get("/api/gst/pan/NOGST1234E"))
        .andExpect(status().isUnprocessableEntity());
  }
}
