package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling localization and translation persistence. */
@Service
public class LocalizationService {
  private final SourceTermRepository sourceTermRepository;
  private final TranslationService translationService;
  private static final Logger logger = LoggerFactory.getLogger(LocalizationService.class);

  public LocalizationService(
      SourceTermRepository sourceTermRepository, TranslationService translationService) {
    this.sourceTermRepository = sourceTermRepository;
    this.translationService = translationService;
  }

  /**
   * Localizes text by translating and potentially romanizing it, with caching.
   *
   * @param text The text to localize.
   * @param fromLanguage The source language code.
   * @param toLanguage The target language code.
   * @return The localization response with translated and romanized text.
   */
  @Transactional
  public LocalizeResponse localize(String text, String fromLanguage, String toLanguage) {
    logger.debug("Attempting to localize text '{}' from {} to {}", text, fromLanguage, toLanguage);

    // Get or create source term (handles race conditions)
    SourceTerm sourceTerm = getOrCreateSourceTerm(text, fromLanguage, toLanguage);

    // Get or create translation (handles race conditions)
    Translation translation = getOrCreateTranslation(sourceTerm, text, fromLanguage, toLanguage);

    return new LocalizeResponse(translation.getTranslatedText(), sourceTerm.getRomanizedText());
  }

  private SourceTerm getOrCreateSourceTerm(String text, String fromLanguage, String toLanguage) {
    String romanizedText = translationService.romanizeText(text, fromLanguage);

    SourceTerm newSourceTerm = new SourceTerm();
    newSourceTerm.setLanguageCode(fromLanguage);
    newSourceTerm.setOriginalText(text);
    newSourceTerm.setRomanizedText(romanizedText);

    String translatedText = translationService.translateText(text, fromLanguage, toLanguage);
    Translation newTrans = new Translation();
    newTrans.setSourceTerm(newSourceTerm);
    newTrans.setLanguageCode(toLanguage);
    newTrans.setTranslatedText(translatedText);
    newSourceTerm.addTranslation(newTrans);

    try {
      SourceTerm savedTerm = sourceTermRepository.saveAndFlush(newSourceTerm);
      logger.info("Successfully created new source term with ID: {}", savedTerm.getId());
      return savedTerm;
    } catch (DataIntegrityViolationException ex) {
      // Race condition: another thread created the source term
      logger.debug(
          "Source term for text '{}' was created by another thread. Fetching existing.", text);
      return sourceTermRepository
          .findByOriginalText(text)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Source term not found after constraint violation for text: " + text));
    }
  }

  private Translation getOrCreateTranslation(
      SourceTerm sourceTerm, String text, String fromLanguage, String toLanguage) {
    // Check if translation already exists
    Optional<Translation> existingTranslation = sourceTerm.getTranslation(toLanguage);
    if (existingTranslation.isPresent()) {
      logger.debug("Found existing translation for text '{}' to {}", text, toLanguage);
      return existingTranslation.get();
    }

    // Try to create new translation
    logger.info("Creating new translation for text '{}' to {}", text, toLanguage);
    String translatedText = translationService.translateText(text, fromLanguage, toLanguage);
    Translation newTrans = new Translation();
    newTrans.setLanguageCode(toLanguage);
    newTrans.setTranslatedText(translatedText);
    newTrans.setSourceTerm(sourceTerm);

    sourceTerm.addTranslation(newTrans);

    try {
      sourceTermRepository.saveAndFlush(sourceTerm);
      logger.debug("Successfully created new translation for text '{}' to {}", text, toLanguage);
      return newTrans;
    } catch (DataIntegrityViolationException ex) {
      // Race condition: another thread created the translation
      logger.debug(
          "Translation for text '{}' to {} was created by another thread. Fetching existing.",
          text,
          toLanguage);
      // Re-fetch from database to get the newly added translation
      SourceTerm refreshedTerm =
          sourceTermRepository
              .findById(sourceTerm.getId())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Source term not found after constraint violation for ID: "
                              + sourceTerm.getId()));
      return refreshedTerm
          .getTranslation(toLanguage)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Translation not found after constraint violation for text: "
                          + text
                          + " to language: "
                          + toLanguage));
    }
  }
}
