package dev.ghidora.utabridgeserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a language is not supported by the translation service. Results in 400 Bad
 * Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedLanguageException extends TranslationException {

  private final String unsupportedLanguage;
  private final boolean isSourceLanguage;

  /**
   * Constructs a new UnsupportedLanguageException with the specified language and type.
   *
   * @param unsupportedLanguage the unsupported language code
   * @param isSourceLanguage true if the unsupported language is the source language, false if it's
   *     the target language
   */
  public UnsupportedLanguageException(String unsupportedLanguage, boolean isSourceLanguage) {
    super(
        String.format(
            "Language '%s' is not supported for %s",
            unsupportedLanguage, isSourceLanguage ? "source" : "target"));
    this.unsupportedLanguage = unsupportedLanguage;
    this.isSourceLanguage = isSourceLanguage;
  }

  /**
   * Constructs a new UnsupportedLanguageException with the specified language, type, and cause.
   *
   * @param unsupportedLanguage the unsupported language code
   * @param isSourceLanguage true if the unsupported language is the source language, false if it's
   *     the target language
   * @param cause the cause of the exception
   */
  public UnsupportedLanguageException(
      String unsupportedLanguage, boolean isSourceLanguage, Throwable cause) {
    super(
        String.format(
            "Language '%s' is not supported for %s",
            unsupportedLanguage, isSourceLanguage ? "source" : "target"),
        cause);
    this.unsupportedLanguage = unsupportedLanguage;
    this.isSourceLanguage = isSourceLanguage;
  }

  /**
   * Returns the unsupported language code.
   *
   * @return the unsupported language code
   */
  public String getUnsupportedLanguage() {
    return unsupportedLanguage;
  }

  /**
   * Returns whether the unsupported language is the source language.
   *
   * @return true if the unsupported language is the source language, false if it's the target
   *     language
   */
  public boolean isSourceLanguage() {
    return isSourceLanguage;
  }
}
