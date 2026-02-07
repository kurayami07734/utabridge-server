package dev.ghidora.utabridgeserver.services;

import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.api.gax.rpc.ResourceExhaustedException;
import com.google.api.gax.rpc.UnavailableException;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.RomanizeTextRequest;
import com.google.cloud.translate.v3.RomanizeTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import dev.ghidora.utabridgeserver.exceptions.TranslationException;
import dev.ghidora.utabridgeserver.exceptions.TranslationServiceException;
import dev.ghidora.utabridgeserver.exceptions.UnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Implementation of TranslationService using Google Cloud Translate. */
@Service
@Primary
public class GoogleTranslateService implements TranslationService {

  private final Translate translate;
  private final TranslationServiceClient translationServiceClient;
  private final String projectId;
  private static final Logger logger = LoggerFactory.getLogger(GoogleTranslateService.class);

  // Error message patterns to identify specific errors
  private static final String UNSUPPORTED_LANGUAGE_PATTERN =
      "not supported|unsupported|invalid language";

  /**
   * Constructs a GoogleTranslateService.
   *
   * @param translate The Google Translate client.
   * @param translationServiceClient The Google Translation Service Client (v3).
   * @param projectId The GCP Project ID.
   */
  @Autowired
  public GoogleTranslateService(
      Translate translate,
      TranslationServiceClient translationServiceClient,
      @Value("${gcp.project-id}") String projectId) {
    this.translate = translate;
    this.translationServiceClient = translationServiceClient;
    this.projectId = projectId;
  }

  @Override
  public String translateText(String text, String sourceLanguage, String targetLanguage)
      throws TranslationException {
    logger.debug(
        "Calling Google Translate API to translate text from {} to {}",
        sourceLanguage,
        targetLanguage);

    try {
      Translation translation =
          translate.translate(
              text,
              TranslateOption.sourceLanguage(sourceLanguage),
              TranslateOption.targetLanguage(targetLanguage));
      String translatedText = translation.getTranslatedText();
      logger.debug("Successfully translated text. Result: '{}'", translatedText);
      return translatedText;

    } catch (Exception e) {
      throw handleTranslationException(
          e, sourceLanguage, targetLanguage, "translation", "translating text");
    }
  }

  @Override
  public String romanizeText(String text, String sourceLanguage) throws TranslationException {
    logger.debug("Calling Google Translate API to romanize text from {}", sourceLanguage);

    try {
      LocationName parent = LocationName.of(projectId, "global");

      RomanizeTextRequest request =
          RomanizeTextRequest.newBuilder()
              .setParent(parent.toString())
              .addContents(text)
              .setSourceLanguageCode(sourceLanguage)
              .build();

      RomanizeTextResponse response = translationServiceClient.romanizeText(request);

      if (response.getRomanizationsList().isEmpty()) {
        logger.warn("No romanization returned for text '{}' in language {}", text, sourceLanguage);
        return text;
      }

      String romanizedText = response.getRomanizations(0).getRomanizedText();
      logger.debug("Successfully romanized text. Result: '{}'", romanizedText);
      return romanizedText;

    } catch (Exception e) {
      throw handleTranslationException(e, sourceLanguage, null, "romanization", "romanizing text");
    }
  }

  /**
   * Unified exception handler for translation and romanization operations.
   *
   * @param e The exception thrown by the Google Translate API
   * @param sourceLanguage The source language code
   * @param targetLanguage The target language code (can be null for romanization)
   * @param operation The operation name for logging (e.g., "translation", "romanization")
   * @param operationDescription Human-readable description of the operation (e.g., "translating
   *     text")
   * @return Never returns - always throws an appropriate exception
   * @throws TranslationException Always thrown with appropriate type and message
   */
  private TranslationException handleTranslationException(
      Exception e,
      String sourceLanguage,
      String targetLanguage,
      String operation,
      String operationDescription) {

    if (e instanceof InvalidArgumentException) {
      return handleInvalidArgumentException(
          (InvalidArgumentException) e, sourceLanguage, targetLanguage, operation);
    }

    if (e instanceof TranslateException) {
      return handleTranslateException(
          (TranslateException) e, sourceLanguage, targetLanguage, operation);
    }

    if (e instanceof PermissionDeniedException) {
      logger.error(
          "Permission denied for Google Translate API during {}. Source: {}, Target: {}",
          operation,
          sourceLanguage,
          targetLanguage,
          e);
      return new TranslationServiceException(
          "Authentication failed with translation service. Please check your GCP credentials.",
          false,
          e);
    }

    if (e instanceof ResourceExhaustedException) {
      logger.error(
          "Quota exceeded for Google Translate API during {}. Source: {}, Target: {}",
          operation,
          sourceLanguage,
          targetLanguage);
      return new TranslationServiceException(
          "Translation service quota exceeded. Please try again later.", true, e);
    }

    if (e instanceof UnavailableException) {
      logger.error(
          "Google Translate API unavailable during {}. Source: {}, Target: {}",
          operation,
          sourceLanguage,
          targetLanguage);
      return new TranslationServiceException(
          "Translation service is temporarily unavailable. Please try again later.",
          sourceLanguage,
          targetLanguage,
          true,
          e);
    }

    if (e instanceof ApiException) {
      ApiException apiException = (ApiException) e;
      logger.error(
          "Google Translate API error during {}: {} (Code: {}). Source: {}, Target: {}",
          operation,
          apiException.getMessage(),
          apiException.getStatusCode().getCode(),
          sourceLanguage,
          targetLanguage,
          apiException);
      return new TranslationServiceException(
          String.format(
              "Translation service error during %s: %s (Code: %s)",
              operationDescription,
              apiException.getMessage(),
              apiException.getStatusCode().getCode()),
          sourceLanguage,
          targetLanguage,
          apiException.isRetryable(),
          apiException);
    }

    logger.error(
        "Unexpected error during {}. Source: {}, Target: {}",
        operation,
        sourceLanguage,
        targetLanguage,
        e);
    return new TranslationServiceException(
        "An unexpected error occurred during " + operationDescription + ": " + e.getMessage(),
        sourceLanguage,
        targetLanguage,
        false,
        e);
  }

  /**
   * Handles InvalidArgumentException from Google Cloud Translate API. Checks if the error is due to
   * unsupported language.
   */
  private TranslationException handleInvalidArgumentException(
      InvalidArgumentException e, String sourceLanguage, String targetLanguage, String operation) {
    String errorMessage = e.getMessage().toLowerCase();

    if (errorMessage.contains("source language")
        || errorMessage.matches(".*" + UNSUPPORTED_LANGUAGE_PATTERN + ".*")) {
      logger.warn(
          "Unsupported source language '{}' for {}. Error: {}",
          sourceLanguage,
          operation,
          e.getMessage());
      return new UnsupportedLanguageException(sourceLanguage, true, e);
    }

    if (errorMessage.contains("target language")) {
      logger.warn(
          "Unsupported target language '{}' for {}. Error: {}",
          targetLanguage,
          operation,
          e.getMessage());
      return new UnsupportedLanguageException(targetLanguage, false, e);
    }

    logger.error(
        "Invalid argument error during {}. Source: {}, Target: {}. Error: {}",
        operation,
        sourceLanguage,
        targetLanguage,
        e.getMessage());
    return new TranslationException(
        String.format("Invalid argument for %s: %s", operation, e.getMessage()),
        sourceLanguage,
        targetLanguage,
        e);
  }

  /**
   * Handles TranslateException from Google Cloud Translate API. Attempts to identify unsupported
   * language errors.
   */
  private TranslationException handleTranslateException(
      TranslateException e, String sourceLanguage, String targetLanguage, String operation) {
    String errorMessage = e.getMessage().toLowerCase();
    int errorCode = e.getCode();

    if (errorMessage.matches(".*" + UNSUPPORTED_LANGUAGE_PATTERN + ".*")) {
      if (errorMessage.contains("source") || errorMessage.contains("from")) {
        logger.warn(
            "Unsupported source language '{}' for {}. Error: {}",
            sourceLanguage,
            operation,
            e.getMessage());
        return new UnsupportedLanguageException(sourceLanguage, true, e);
      }
      if (errorMessage.contains("target") || errorMessage.contains("to")) {
        logger.warn(
            "Unsupported target language '{}' for {}. Error: {}",
            targetLanguage,
            operation,
            e.getMessage());
        return new UnsupportedLanguageException(targetLanguage, false, e);
      }
    }

    if (errorCode == 400) {
      logger.error(
          "Bad request error during {}. Source: {}, Target: {}. Error: {}",
          operation,
          sourceLanguage,
          targetLanguage,
          e.getMessage());
      return new TranslationException(
          String.format("Bad request for %s: %s", operation, e.getMessage()),
          sourceLanguage,
          targetLanguage,
          e);
    }

    if (errorCode == 403) {
      logger.error(
          "Permission denied during {}. Source: {}, Target: {}",
          operation,
          sourceLanguage,
          targetLanguage);
      return new TranslationServiceException(
          "Authentication failed with translation service.", false, e);
    }

    if (errorCode == 429) {
      logger.error(
          "Rate limit exceeded during {}. Source: {}, Target: {}",
          operation,
          sourceLanguage,
          targetLanguage);
      return new TranslationServiceException(
          "Translation service rate limit exceeded. Please try again later.", true, e);
    }

    if (errorCode >= 500) {
      logger.error(
          "Server error during {}. Source: {}, Target: {}. Code: {}",
          operation,
          sourceLanguage,
          targetLanguage,
          errorCode);
      return new TranslationServiceException(
          String.format("Translation service error (Code: %d). Please try again later.", errorCode),
          sourceLanguage,
          targetLanguage,
          true,
          e);
    }

    logger.error(
        "Google Translate error during {}. Source: {}, Target: {}. Code: {}, Message: {}",
        operation,
        sourceLanguage,
        targetLanguage,
        errorCode,
        e.getMessage());
    return new TranslationServiceException(
        String.format("Translation service error: %s (Code: %d)", e.getMessage(), errorCode),
        sourceLanguage,
        targetLanguage,
        e.isRetryable(),
        e);
  }
}
