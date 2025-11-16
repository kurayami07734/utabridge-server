package dev.ghidora.utabridge_server.services;

/**
 * An interface for providing translation and romanization services.
 * This decouples the application from a specific provider (e.g., Google, Microsoft, etc.).
 */
public interface TranslationService {

    /**
     * Translates text from a source language to a target language.
     *
     * @param text           The text to translate.
     * @param sourceLanguage The ISO 639-1 code for the source language (e.g., "en").
     * @param targetLanguage The ISO 639-1 code for the target language (e.g., "hi").
     * @return The translated text.
     */
    String translateText(String text, String sourceLanguage, String targetLanguage);

    /**
     * Romanizes text from a language that uses a non-Latin script.
     *
     * @param text           The text to romanize (e.g., "नमस्ते").
     * @param sourceLanguage The ISO 639-1 code for the source language (e.g., "hi").
     * @return The romanized text (e.g., "namaste").
     */
    String romanizeText(String text, String sourceLanguage);
}