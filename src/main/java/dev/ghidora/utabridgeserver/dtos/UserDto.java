package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing user profile information.
 *
 * @param name The user's display name.
 * @param pictureUrl The user's profile picture URL.
 */
@Schema(description = "User profile information")
public record UserDto(
    @Schema(example = "John Doe") String name,
    @Schema(example = "https://example.com/avatar.jpg") String pictureUrl) {}
