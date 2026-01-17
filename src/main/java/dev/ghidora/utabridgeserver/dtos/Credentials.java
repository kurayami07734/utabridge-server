package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing authentication and refresh tokens.
 *
 * @param authToken The JWT access token.
 * @param refreshToken The secure refresh token.
 */
@Schema(description = "Authentication credentials containing JWT and refresh tokens")
public record Credentials(
    @Schema(
            description = "JWT access token for authenticating requests",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String authToken,
    @Schema(description = "Refresh token for obtaining new JWT tokens", example = "abc123xyz...")
        String refreshToken) {}
