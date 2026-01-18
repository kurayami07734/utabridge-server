package dev.ghidora.utabridgeserver.dtos;

import dev.ghidora.utabridgeserver.enums.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response structure")
public record ErrorResponse(
    @Schema(description = "Error code") ErrorType error,
    @Schema(description = "Error message") String message,
    @Schema(description = "HTTP status code") int status,
    @Schema(description = "Unix timestamp") long timestamp,
    @Schema(description = "Route of the API call") String path) {
  public ErrorResponse(ErrorType error, String message, int status, String path) {
    this(error, message, status, System.currentTimeMillis(), path);
  }
}
