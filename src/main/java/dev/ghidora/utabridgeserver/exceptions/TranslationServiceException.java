package dev.ghidora.utabridgeserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the translation service is unavailable or encounters an error. This could
 * be due to network issues, quota exceeded, or service downtime. Results in 503 Service
 * Unavailable.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class TranslationServiceException extends TranslationException {

  private final boolean retryable;

  /**
   * Constructs a new TranslationServiceException with the specified detail message.
   *
   * @param message the detail message
   */
  public TranslationServiceException(String message) {
    super(message);
    this.retryable = false;
  }

  /**
   * Constructs a new TranslationServiceException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public TranslationServiceException(String message, Throwable cause) {
    super(message, cause);
    this.retryable = false;
  }

  /**
   * Constructs a new TranslationServiceException with the specified detail message, retryable flag,
   * and cause.
   *
   * @param message the detail message
   * @param retryable true if the operation can be retried
   * @param cause the cause of the exception
   */
  public TranslationServiceException(String message, boolean retryable, Throwable cause) {
    super(message, cause);
    this.retryable = retryable;
  }

  /**
   * Constructs a new TranslationServiceException with the specified detail message, language codes,
   * retryable flag, and cause.
   *
   * @param message the detail message
   * @param sourceLanguage the source language code
   * @param targetLanguage the target language code
   * @param retryable true if the operation can be retried
   * @param cause the cause of the exception
   */
  public TranslationServiceException(
      String message,
      String sourceLanguage,
      String targetLanguage,
      boolean retryable,
      Throwable cause) {
    super(message, sourceLanguage, targetLanguage, cause);
    this.retryable = retryable;
  }

  /**
   * Returns whether the operation can be retried.
   *
   * @return true if the operation can be retried, false otherwise
   */
  public boolean isRetryable() {
    return retryable;
  }
}
