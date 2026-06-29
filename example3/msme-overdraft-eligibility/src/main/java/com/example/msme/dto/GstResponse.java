package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "GST registrations discovered for a PAN through UDYAM details")
public record GstResponse(String panNumber, String udyamNumber, List<GstRegistrationDto> gstRegistrations) {
}
