package dev.ghidora.utabridgeserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for localization requests containing translated and romanized text. */
@Schema(description = "Response containing translated and romanized text")
public record LocalizeResponse(
    @Schema(description = "Translated text in the target language", example = "Hello")
        String translatedText,
    @Schema(description = "Romanized version of the source text", example = "Konnichiwa")
        String romanizedText) {}
