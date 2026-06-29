package com.example.wealth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Standard error payload")
public record ErrorResponse(
    @Schema(description = "UTC timestamp", example = "2026-06-29T10:15:30Z") Instant timestamp,
    @Schema(description = "HTTP status code", example = "404") int status,
    @Schema(description = "Error category", example = "Not Found") String error,
    @Schema(description = "Error details") List<String> messages,
    @Schema(description = "Request path", example = "/api/funds/99") String path) {
}
