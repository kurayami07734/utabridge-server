package dev.ghidora.utabridgeserver.dtos;

import dev.ghidora.utabridgeserver.enums.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard error response structure returned by the API.
 *
 * @param error The error type/code.
 * @param message The error message.
 * @param status The HTTP status code.
 * @param timestamp Unix timestamp of when the error occurred.
 * @param path The request path that caused the error.
 */
@Schema(description = "Standard error response structure")
public record ErrorResponse(
    @Schema(description = "Error code") ErrorType error,
    @Schema(description = "Error message") String message,
    @Schema(description = "HTTP status code") int status,
    @Schema(description = "Unix timestamp") long timestamp,
    @Schema(description = "Route of the API call") String path) {
  /**
   * Constructs an ErrorResponse with current timestamp.
   *
   * @param error Error type.
   * @param message Error message.
   * @param status HTTP status.
   * @param path Request path.
   */
  public ErrorResponse(ErrorType error, String message, int status, String path) {
    this(error, message, status, System.currentTimeMillis(), path);
  }
}
