package dev.ghidora.utabridgeserver.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.models.SourceTerm;
import dev.ghidora.utabridgeserver.models.Translation;
import dev.ghidora.utabridgeserver.repositories.SourceTermRepository;
import java.util.Optional;
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

  @InjectMocks private LocalizationService localizationService;

  private static final String TEXT = "Hello";
  private static final String FROM_LANG = "en";
  private static final String TO_LANG = "ja";
  private static final String TRANSLATED_TEXT = "こんにちは";
  private static final String ROMANIZED_TEXT = "Konnichiwa";

  @Test
  void localize_NewTerm_CreatesAndSaves() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository).saveAndFlush(any(SourceTerm.class));
    verify(translationService).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService).romanizeText(TEXT, FROM_LANG);
  }

  @Test
  void localize_ExistingTermNoTargetTranslation_AddsTranslation() {
    // Arrange - simulate race condition on source term creation, then add translation
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(TRANSLATED_TEXT) // First call for source term creation
        .thenReturn(TRANSLATED_TEXT); // Second call for translation creation
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    // First saveAndFlush fails (source term already exists)
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"))
        .thenAnswer(inv -> inv.getArgument(0)); // Second call succeeds (adding translation)
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository, times(2)).saveAndFlush(any(SourceTerm.class));
    verify(translationService, times(2)).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService).romanizeText(TEXT, FROM_LANG);
  }

  @Test
  void localize_CachedTranslation_ReturnsCached() {
    // Arrange - simulate race condition where everything already exists
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    Translation cachedTranslation = new Translation();
    cachedTranslation.setLanguageCode(TO_LANG);
    cachedTranslation.setTranslatedText(TRANSLATED_TEXT);
    existingTerm.addTranslation(cachedTranslation);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    // First saveAndFlush fails (source term already exists), translation exists in memory so no
    // second save
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    // Only one saveAndFlush call - for source term (which fails), translation check is in-memory
    verify(sourceTermRepository).saveAndFlush(any(SourceTerm.class));
    // translateText is called once during the failed source term creation attempt
    verify(translationService).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService).romanizeText(TEXT, FROM_LANG);
  }

  @Test
  void localize_VerifyRomanizationSaved() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getRomanizedText().equals(ROMANIZED_TEXT);
  }

  @Test
  void localize_VerifyCorrectTranslationSaved() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    Optional<Translation> translation = savedTerm.getTranslation(TO_LANG);
    assert translation.isPresent();
    assert translation.get().getTranslatedText().equals(TRANSLATED_TEXT);
    assert translation.get().getLanguageCode().equals(TO_LANG);
  }

  @Test
  void localize_VerifySourceTermFieldsSet() {
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.saveAndFlush(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getOriginalText().equals(TEXT);
    assert savedTerm.getLanguageCode().equals(FROM_LANG);
  }

  @Test
  void localize_ExistingTerm_VerifyTranslationSavedToExistingTerm() {
    // Arrange - simulate race condition on source term
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    // First saveAndFlush fails (source term exists), second succeeds (adding translation)
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"))
        .thenAnswer(inv -> inv.getArgument(0));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    verify(sourceTermRepository, times(2)).saveAndFlush(sourceTermCaptor.capture());
    SourceTerm savedTerm = sourceTermCaptor.getAllValues().get(1); // Second save
    Optional<Translation> translation = savedTerm.getTranslation(TO_LANG);
    assert translation.isPresent();
    assert translation.get().getTranslatedText().equals(TRANSLATED_TEXT);
  }

  @Test
  void localize_RaceConditionOnSourceTerm_ReturnsExistingSourceTerm() {
    // Arrange - simulate race condition where source term creation fails
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    Translation existingTranslation = new Translation();
    existingTranslation.setLanguageCode(TO_LANG);
    existingTranslation.setTranslatedText(TRANSLATED_TEXT);
    existingTerm.addTranslation(existingTranslation);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    // Act
    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    // Assert - should return existing term without error
    assertNotNull(response);
    assertEquals(TRANSLATED_TEXT, response.translatedText());
    assertEquals(ROMANIZED_TEXT, response.romanizedText());
    verify(sourceTermRepository).saveAndFlush(any(SourceTerm.class));
    verify(sourceTermRepository).findByOriginalText(TEXT);
  }

  @Test
  void localize_RaceConditionOnTranslation_ReturnsExistingTranslation() {
    // Arrange - source term creation fails, then translation addition also has race condition
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    Translation existingTranslation = new Translation();
    existingTranslation.setLanguageCode(TO_LANG);
    existingTranslation.setTranslatedText(TRANSLATED_TEXT);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(TRANSLATED_TEXT) // First call for source term
        .thenReturn(TRANSLATED_TEXT); // Second call for translation
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    // First call fails (source term exists), second call also fails (translation exists)
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"))
        .thenThrow(new DataIntegrityViolationException("Translation constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    // First call returns term without translation (simulating race)
    // Second call returns term with translation (after refresh)
    SourceTerm termWithTranslation = new SourceTerm();
    termWithTranslation.setOriginalText(TEXT);
    termWithTranslation.setLanguageCode(FROM_LANG);
    termWithTranslation.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(termWithTranslation, "id", 1L);
    termWithTranslation.addTranslation(existingTranslation);

    when(sourceTermRepository.findById(1L))
        .thenReturn(Optional.of(existingTerm)) // First call - no translation yet
        .thenReturn(Optional.of(termWithTranslation)); // After concurrent insert

    // Act
    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    // Assert
    assertNotNull(response);
    assertEquals(TRANSLATED_TEXT, response.translatedText());
    verify(sourceTermRepository, atLeastOnce()).findById(1L);
  }

  @Test
  void localize_RaceConditionSourceTermNotFound_ThrowsIllegalStateException() {
    // Arrange - race condition but source term cannot be found
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> localizationService.localize(TEXT, FROM_LANG, TO_LANG));
    assertTrue(exception.getMessage().contains("Source term not found after constraint violation"));
  }

  @Test
  void localize_RaceConditionTranslationNotFound_ThrowsIllegalStateException() {
    // Arrange - translation race condition but translation cannot be found after refresh
    // Note: In the actual code, if the translation is in memory it won't try to save again
    // This test simulates a scenario where the constraint violation happens during translation save
    // but after refresh, the translation still can't be found (edge case)
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(existingTerm, "id", 1L);

    // Create a version of the term that will be returned by findById (without translation)
    SourceTerm refreshedTerm = new SourceTerm();
    refreshedTerm.setOriginalText(TEXT);
    refreshedTerm.setLanguageCode(FROM_LANG);
    refreshedTerm.setRomanizedText(ROMANIZED_TEXT);
    ReflectionTestUtils.setField(refreshedTerm, "id", 1L);

    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG))
        .thenReturn(TRANSLATED_TEXT) // First call for source term
        .thenReturn(TRANSLATED_TEXT); // Second call for translation
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    // First save fails (source term exists), second save also fails (translation race)
    when(sourceTermRepository.saveAndFlush(any(SourceTerm.class)))
        .thenThrow(new DataIntegrityViolationException("Unique constraint violation"))
        .thenThrow(new DataIntegrityViolationException("Translation constraint violation"));
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));
    // findById returns the refreshed term without translation (edge case)
    when(sourceTermRepository.findById(1L)).thenReturn(Optional.of(refreshedTerm));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> localizationService.localize(TEXT, FROM_LANG, TO_LANG));
    assertTrue(exception.getMessage().contains("Translation not found after constraint violation"));
  }
}
