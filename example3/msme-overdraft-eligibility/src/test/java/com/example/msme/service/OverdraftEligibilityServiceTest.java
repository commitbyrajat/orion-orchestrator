package com.example.msme.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.msme.dto.OverdraftEligibilityRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OverdraftEligibilityServiceTest {

  @Autowired
  private OverdraftEligibilityService service;

  @Test
  void eligiblePanPassesThreshold() {
    var result = service.evaluate(new OverdraftEligibilityRequest(1L, "27ABCDE1234F1Z5", List.of(1L, 2L)));

    assertThat(result.eligible()).isTrue();
    assertThat(result.score()).isGreaterThanOrEqualTo(70);
    assertThat(result.maximumEligibleAmount()).isPositive();
  }

  @Test
  void lowTurnoverPanFailsThreshold() {
    var result = service.evaluate(new OverdraftEligibilityRequest(3L, "07LOWTR1234A1Z7", List.of(5L)));

    assertThat(result.eligible()).isFalse();
    assertThat(result.score()).isLessThan(70);
    assertThat(result.reasons()).anyMatch(reason -> reason.contains("turnover"));
  }

  @Test
  void missingUdyamIsRejectedImmediately() {
    var result = service.evaluate(new OverdraftEligibilityRequest(5L, "27ABCDE1234F1Z5", List.of(7L)));

    assertThat(result.eligible()).isFalse();
    assertThat(result.score()).isZero();
    assertThat(result.reasons()).anyMatch(reason -> reason.contains("UDYAM"));
  }

  @Test
  void missingGstIsRejectedImmediately() {
    var result = service.evaluate(new OverdraftEligibilityRequest(6L, "29NOGST1234E1Z5", List.of(8L)));

    assertThat(result.eligible()).isFalse();
    assertThat(result.score()).isZero();
    assertThat(result.reasons()).anyMatch(reason -> reason.contains("GST"));
  }
}
