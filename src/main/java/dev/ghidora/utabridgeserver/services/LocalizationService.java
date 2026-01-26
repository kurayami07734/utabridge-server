package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    logger.debug(
        "Attempting to localize text '{}' from {} to {}", text, fromLanguage, toLanguage);
    Optional<SourceTerm> sourceTerm = sourceTermRepository.findByOriginalText(text);

    if (sourceTerm.isEmpty()) {
      logger.info("Source term '{}' not found in cache. Creating new entry.", text);
      SourceTerm newSourceTerm = createNewSourceTerm(text, fromLanguage, toLanguage);
      sourceTermRepository.save(newSourceTerm);
      return new LocalizeResponse(
          newSourceTerm.getTranslation(toLanguage).get().getTranslatedText(),
          newSourceTerm.getRomanizedText());
    }

    logger.info("Source term '{}' found in cache. Processing existing entry.", text);
    return handleExistingSourceTerm(sourceTerm.get(), text, fromLanguage, toLanguage);
  }

  private SourceTerm createNewSourceTerm(String text, String fromLanguage, String toLanguage) {
    logger.debug("Creating new source term for text '{}'", text);
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
    logger.debug(
        "Successfully created new source term and translation for text '{}' to language {}",
        text,
        toLanguage);
    return newSourceTerm;
  }

  private LocalizeResponse handleExistingSourceTerm(
      SourceTerm existingTerm, String text, String fromLanguage, String toLanguage) {
    Optional<Translation> translation = existingTerm.getTranslation(toLanguage);

    if (translation.isPresent()) {
      logger.debug(
          "Translation for source term '{}' to language {} found in cache.",
          text,
          toLanguage);
      return new LocalizeResponse(
          translation.get().getTranslatedText(), existingTerm.getRomanizedText());
    }

    logger.info(
        "Translation for source term '{}' to language {} not found in cache. Creating new translation.",
        text,
        toLanguage);
    String translatedText = translationService.translateText(text, fromLanguage, toLanguage);
    Translation newTrans = new Translation();
    newTrans.setLanguageCode(toLanguage);
    newTrans.setTranslatedText(translatedText);
    newTrans.setSourceTerm(existingTerm);

    existingTerm.addTranslation(newTrans);
    sourceTermRepository.save(existingTerm);
    logger.debug("Successfully created and saved new translation for text '{}'", text);

    return new LocalizeResponse(translatedText, existingTerm.getRomanizedText());
  }
}
