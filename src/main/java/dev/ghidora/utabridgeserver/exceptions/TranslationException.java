package dev.ghidora.utabridgeserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for translation-related errors. This is the parent class for all translation
 * exceptions.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TranslationException extends RuntimeException {

  private final String sourceLanguage;
  private final String targetLanguage;

  /**
   * Constructs a new TranslationException with the specified detail message.
   *
   * @param message the detail message
   */
  public TranslationException(String message) {
    super(message);
    this.sourceLanguage = null;
    this.targetLanguage = null;
  }

  /**
   * Constructs a new TranslationException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public TranslationException(String message, Throwable cause) {
    super(message, cause);
    this.sourceLanguage = null;
    this.targetLanguage = null;
  }

  /**
   * Constructs a new TranslationException with the specified detail message, language codes, and
   * cause.
   *
   * @param message the detail message
   * @param sourceLanguage the source language code
   * @param targetLanguage the target language code
   * @param cause the cause of the exception
   */
  public TranslationException(
      String message, String sourceLanguage, String targetLanguage, Throwable cause) {
    super(message, cause);
    this.sourceLanguage = sourceLanguage;
    this.targetLanguage = targetLanguage;
  }

  /**
   * Returns the source language code.
   *
   * @return the source language code, or null if not set
   */
  public String getSourceLanguage() {
    return sourceLanguage;
  }

  /**
   * Returns the target language code.
   *
   * @return the target language code, or null if not set
   */
  public String getTargetLanguage() {
    return targetLanguage;
  }
}
