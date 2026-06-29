package com.example.msme.controller;

import com.example.msme.dto.GstResponse;
import com.example.msme.service.GstService;
import com.example.msme.util.PanValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/gst")
@RequiredArgsConstructor
@Tag(name = "GST", description = "GST registrations discovered through UDYAM data")
public class GstController {

  private final GstService gstService;

  @GetMapping("/pan/{pan}")
  @Operation(operationId = "msme_get_gst_by_pan", summary = "Get GST registrations linked to a PAN")
  public GstResponse getGst(@PathVariable @Pattern(regexp = PanValidator.PAN_REGEX) String pan) {
    return gstService.getGstByPan(pan);
  }
}
