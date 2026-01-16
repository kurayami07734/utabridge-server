package dev.ghidora.utabridgeserver.services;

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
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    when(sourceTermRepository.save(any(SourceTerm.class))).thenAnswer(inv -> inv.getArgument(0));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository).save(any(SourceTerm.class));
    verify(translationService).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService).romanizeText(TEXT, FROM_LANG);
  }

  @Test
  void localize_ExistingTermNoTargetTranslation_AddsTranslation() {
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);

    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(sourceTermRepository.save(any(SourceTerm.class))).thenAnswer(inv -> inv.getArgument(0));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository).save(any(SourceTerm.class));
    verify(translationService).translateText(TEXT, FROM_LANG, TO_LANG);
    verify(translationService, never()).romanizeText(any(), any());
  }

  @Test
  void localize_CachedTranslation_ReturnsCached() {
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);

    Translation cachedTranslation = new Translation();
    cachedTranslation.setLanguageCode(TO_LANG);
    cachedTranslation.setTranslatedText(TRANSLATED_TEXT);
    existingTerm.addTranslation(cachedTranslation);

    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));

    LocalizeResponse response = localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    assert response.translatedText().equals(TRANSLATED_TEXT);
    assert response.romanizedText().equals(ROMANIZED_TEXT);
    verify(sourceTermRepository, never()).save(any(SourceTerm.class));
    verify(translationService, never()).translateText(any(), any(), any());
    verify(translationService, never()).romanizeText(any(), any());
  }

  @Test
  void localize_VerifyRomanizationSaved() {
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.save(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getRomanizedText().equals(ROMANIZED_TEXT);
  }

  @Test
  void localize_VerifyCorrectTranslationSaved() {
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.save(sourceTermCaptor.capture()))
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
    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.empty());
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    when(translationService.romanizeText(TEXT, FROM_LANG)).thenReturn(ROMANIZED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.save(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    assert savedTerm.getOriginalText().equals(TEXT);
    assert savedTerm.getLanguageCode().equals(FROM_LANG);
  }

  @Test
  void localize_ExistingTerm_VerifyTranslationSavedToExistingTerm() {
    SourceTerm existingTerm = new SourceTerm();
    existingTerm.setOriginalText(TEXT);
    existingTerm.setLanguageCode(FROM_LANG);
    existingTerm.setRomanizedText(ROMANIZED_TEXT);

    when(sourceTermRepository.findByOriginalText(TEXT)).thenReturn(Optional.of(existingTerm));
    when(translationService.translateText(TEXT, FROM_LANG, TO_LANG)).thenReturn(TRANSLATED_TEXT);
    ArgumentCaptor<SourceTerm> sourceTermCaptor = ArgumentCaptor.forClass(SourceTerm.class);
    when(sourceTermRepository.save(sourceTermCaptor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    localizationService.localize(TEXT, FROM_LANG, TO_LANG);

    SourceTerm savedTerm = sourceTermCaptor.getValue();
    Optional<Translation> translation = savedTerm.getTranslation(TO_LANG);
    assert translation.isPresent();
    assert translation.get().getTranslatedText().equals(TRANSLATED_TEXT);
    assert translation.get().getSourceTerm().equals(existingTerm);
  }
}
