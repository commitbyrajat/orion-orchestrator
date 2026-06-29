package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registered MSME business location")
public record BusinessLocationDto(String addressLine1, String city, String state, String pincode) {
}
