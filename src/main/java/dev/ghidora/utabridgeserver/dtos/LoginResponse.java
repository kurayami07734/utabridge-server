package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the login response containing authentication tokens and user details.
 *
 * @param authToken The JWT access token.
 * @param refreshToken The refresh token for obtaining new JWT tokens.
 * @param user The authenticated user's profile information.
 */
@Schema(description = "Login response containing tokens and user details")
public record LoginResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIs...") String authToken,
    @Schema(description = "Refresh token", example = "abc123xyz...") String refreshToken,
    @Schema(description = "Authenticated user details") UserDto user) {}
