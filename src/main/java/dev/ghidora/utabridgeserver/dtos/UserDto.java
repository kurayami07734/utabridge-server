package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * DTO representing user profile information.
 *
 * @param id The user's unique identifier.
 * @param name The user's display name.
 * @param pictureUrl The user's profile picture URL.
 * @param email The user's email address.
 * @param preferences The user's preferences.
 */
@Schema(description = "User profile information")
public record UserDto(
    @Schema(example = "10") Long id,
    @Schema(example = "John Doe") String name,
    @Schema(example = "https://example.com/avatar.jpg") String pictureUrl,
    @Schema(example = "user@example.com") String email,
    @Schema(example = "{\"PRIMARY_TEXT_TYPE\": \"ROMANIZATION\"}")
        Map<String, String> preferences) {}
