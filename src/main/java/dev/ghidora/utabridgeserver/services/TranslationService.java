package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.exceptions.TranslationException;

/**
 * An interface for providing translation and romanization services. This decouples the application
 * from a specific provider (e.g., Google, Microsoft, etc.).
 */
public interface TranslationService {

  /**
   * Translates text from a source language to a target language.
   *
   * @param text The text to translate.
   * @param sourceLanguage The ISO 639-1 code for the source language (e.g., "en").
   * @param targetLanguage The ISO 639-1 code for the target language (e.g., "hi").
   * @return The translated text.
   * @throws TranslationException if translation fails or language is not supported.
   */
  String translateText(String text, String sourceLanguage, String targetLanguage)
      throws TranslationException;

  /**
   * Romanizes text from a language that uses a non-Latin script.
   *
   * @param text The text to romanize (e.g., "नमस्ते").
   * @param sourceLanguage The ISO 639-1 code for the source language (e.g., "hi").
   * @return The romanized text (e.g., "namaste").
   * @throws TranslationException if romanization fails or language is not supported.
   */
  String romanizeText(String text, String sourceLanguage) throws TranslationException;
}
