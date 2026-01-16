package dev.ghidora.utabridgeserver.dtos;

/** Response DTO for localization requests containing translated and romanized text. */
public record LocalizeResponse(String translatedText, String romanizedText) {}
