package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling localization and translation persistence. */
@Service
public class LocalizationService {
  private final SourceTermRepository sourceTermRepository;
  private final TranslationService translationService;
  @PersistenceContext private EntityManager entityManager;
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

    SourceTerm sourceTerm = getOrCreateSourceTerm(text, fromLanguage, toLanguage);

    Translation translation = getOrCreateTranslation(sourceTerm, text, fromLanguage, toLanguage);

    return new LocalizeResponse(translation.getTranslatedText(), sourceTerm.getRomanizedText());
  }

  private SourceTerm getOrCreateSourceTerm(String text, String fromLanguage, String toLanguage) {
    return sourceTermRepository
        .findByOriginalText(text)
        .orElseGet(() -> createSourceTerm(text, fromLanguage, toLanguage));
  }

  private SourceTerm createSourceTerm(String text, String fromLanguage, String toLanguage) {
    String romanizedText = translationService.romanizeText(text, fromLanguage);
    String translatedText = translationService.translateText(text, fromLanguage, toLanguage);

    SourceTerm newSourceTerm = new SourceTerm();
    newSourceTerm.setLanguageCode(fromLanguage);
    newSourceTerm.setOriginalText(text);
    newSourceTerm.setRomanizedText(romanizedText);

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
      return findSourceTermByText(text);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  private SourceTerm findSourceTermByText(String text) {
    return sourceTermRepository
        .findByOriginalText(text)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Source term not found after constraint violation for text: " + text));
  }

  private Translation getOrCreateTranslation(
      SourceTerm sourceTerm, String text, String fromLanguage, String toLanguage) {
    Optional<Translation> existingTranslation = sourceTerm.getTranslation(toLanguage);
    if (existingTranslation.isPresent()) {
      logger.debug("Found existing translation for text '{}' to {}", text, toLanguage);
      return existingTranslation.get();
    }

    return createTranslation(sourceTerm, text, fromLanguage, toLanguage);
  }

  private Translation createTranslation(
      SourceTerm sourceTerm, String text, String fromLanguage, String toLanguage) {
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
      return findTranslation(sourceTerm.getId(), toLanguage, text);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  private Translation findTranslation(Long sourceTermId, String toLanguage, String text) {
    SourceTerm refreshedTerm =
        sourceTermRepository
            .findById(sourceTermId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Source term not found after constraint violation for ID: "
                            + sourceTermId));
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
