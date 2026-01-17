package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/** Standardized error response for API errors. */
@Schema(description = "Standard error response structure")
public record ErrorResponse(
    @Schema(description = "Error code") String error,
    @Schema(description = "Error message") String message,
    @Schema(description = "HTTP status code") int status,
    @Schema(description = "Unix timestamp") long timestamp,
    @Schema(description = "Route of the API call") String path) {
  public ErrorResponse(String error, String message, int status, String path) {
    this(error, message, status, System.currentTimeMillis(), path);
  }
}
