package com.example.msme.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Standard API error response")
public record ErrorResponse(Instant timestamp, int status, String error, List<String> messages, String path) {
}
