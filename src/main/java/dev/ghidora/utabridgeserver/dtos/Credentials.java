package dev.ghidora.utabridgeserver.dtos;

/**
 * DTO representing authentication and refresh tokens.
 *
 * @param authToken The JWT access token.
 * @param refreshToken The secure refresh token.
 */
public record Credentials(String authToken, String refreshToken) {}
