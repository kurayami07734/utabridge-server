package dev.ghidora.utabridgeserver.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LocalizationServiceTest {

  @Mock private SourceTermRepository sourceTermRepository;

  @Mock private TranslationService translationService;

  @Mock private LanguageDetectionService languageDetectionService;

  @Mock private EntityManager entityManager;

  @InjectMocks private LocalizationService localizationService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(localizationService, "entityManager", entityManager);
  }

  private static final String TEXT = "Hello";
  private static final String FROM_LANG = "en";
  private static final String TO_LANG = "ja";
  private static final String TRANSLATED_TEXT = "こんにちは";
  private static final String ROMANIZED_TEXT = "Konnichiwa";

  @Test
  void localize_NewTerm_CreatesAndSaves() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, FROM_LANG));
    when(translationService.romanizeText(TEXT, FROM_LANG))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, FROM_LANG));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.of(FROM_LANG));
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    LocalizeResponse response = localizationService.localize(TEXT, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository).saveAndFlush(any(SourceTerm.class));
    verify(translationService).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService).romanizeText(TEXT, FROM_LANG);
  }

  @Test
  void localize_CachedTranslation_ReturnsCached() {
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    Translation cachedTranslation = new Translation();
    cachedTranslation.setLanguageCode(TO_LANG);
    cachedTranslation.setTranslatedText(TRANSLATED_TEXT);
    existingTerm.addTranslation(cachedTranslation);

    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    LocalizeResponse response = localizationService.localize(TEXT, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository, never()).saveAndFlush(any(SourceTerm.class));
  }

  @Test
  void localize_VerifyRomanizationSaved() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, FROM_LANG));
    when(translationService.romanizeText(TEXT, FROM_LANG))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, FROM_LANG));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.of(FROM_LANG));
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getRomanizedText().equals(ROMANIZED_TEXT);
  }

  @Test
  void localize_VerifyCorrectTranslationSaved() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, FROM_LANG));
    when(translationService.romanizeText(TEXT, FROM_LANG))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, FROM_LANG));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.of(FROM_LANG));
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    Optional<Translation> translation = savedTerm.getTranslation(TO_LANG);
    assert translation.isPresent();
    assert translation.get().getTranslatedText().equals(TRANSLATED_TEXT);
    assert translation.get().getLanguageCode().equals(TO_LANG);
  }

  @Test
  void localize_VerifySourceTermFieldsSet() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, FROM_LANG));
    when(translationService.romanizeText(TEXT, FROM_LANG))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, FROM_LANG));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.of(FROM_LANG));
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getOriginalText().equals(TEXT);
    assert savedTerm.getLanguageCode().equals(FROM_LANG);
  }

  @Test
  void localize_RaceConditionSourceTermNotFound_ThrowsIllegalStateException() {
    when(translationService.romanizeText(TEXT, FROM_LANG))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, FROM_LANG));
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, FROM_LANG));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.of(FROM_LANG));
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> localizationService.localize(TEXT, TO_LANG));
    assertTrue(exception.getMessage().contains("Source term not found after constraint violation"));
  }

  @Test
  void localize_AutoDetectLanguage_CallsOverloadMethods() {
    String detectedLang = "es";
    when(translationService.translateText(TEXT, TO_LANG))
        .thenReturn(new TranslationService.TranslatedText(TRANSLATED_TEXT, detectedLang));
    when(translationService.romanizeText(TEXT))
        .thenReturn(new TranslationService.RomanizedText(ROMANIZED_TEXT, detectedLang));
    when(languageDetectionService.detectLanguage(TEXT)).thenReturn(Optional.empty());
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    LocalizeResponse response = localizationService.localize(TEXT, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(translationService).translateText(TEXT, TO_LANG);
    verify(translationService).romanizeText(TEXT);
  }
}
