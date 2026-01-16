package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling localization and translation persistence. */
@Service
public class LocalizationService {
  private final SourceTermRepository sourceTermRepository;
  private final TranslationService translationService;

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
    Optional<SourceTerm> sourceTerm = sourceTermRepository.findByOriginalText(text);

    if (sourceTerm.isEmpty()) {
      SourceTerm newSourceTerm = createNewSourceTerm(text, fromLanguage, toLanguage);
      sourceTermRepository.save(newSourceTerm);
      return new LocalizeResponse(
          newSourceTerm.getTranslation(toLanguage).get().getTranslatedText(),
          newSourceTerm.getRomanizedText());
    }

    return handleExistingSourceTerm(sourceTerm.get(), text, fromLanguage, toLanguage);
  }

  private SourceTerm createNewSourceTerm(String text, String fromLanguage, String toLanguage) {
    String romanizedText = translationService.romanizeText(text, fromLanguage);

    SourceTerm newSourceTerm = new SourceTerm();
    newSourceTerm.setLanguageCode(fromLanguage);
    newSourceTerm.setOriginalText(text);
    newSourceTerm.setRomanizedText(romanizedText);

    Translation newTrans = new Translation();
    newTrans.setSourceTerm(newSourceTerm);
    newTrans.setLanguageCode(toLanguage);
    newTrans.setTranslatedText(translationService.translateText(text, fromLanguage, toLanguage));

    newSourceTerm.addTranslation(newTrans);
    return newSourceTerm;
  }

  private LocalizeResponse handleExistingSourceTerm(
      SourceTerm existingTerm, String text, String fromLanguage, String toLanguage) {
    Optional<Translation> translation = existingTerm.getTranslation(toLanguage);

    if (translation.isPresent()) {
      return new LocalizeResponse(
          translation.get().getTranslatedText(), existingTerm.getRomanizedText());
    }

    String translatedText = translationService.translateText(text, fromLanguage, toLanguage);
    Translation newTrans = new Translation();
    newTrans.setLanguageCode(toLanguage);
    newTrans.setTranslatedText(translatedText);
    newTrans.setSourceTerm(existingTerm);

    existingTerm.addTranslation(newTrans);
    sourceTermRepository.save(existingTerm);

    return new LocalizeResponse(translatedText, existingTerm.getRomanizedText());
  }
}
