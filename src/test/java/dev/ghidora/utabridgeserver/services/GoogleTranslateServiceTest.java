package dev.ghidora.utabridgeserver.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.api.gax.rpc.ResourceExhaustedException;
import com.google.api.gax.rpc.StatusCode;
import com.google.api.gax.rpc.UnavailableException;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.Romanization;
import com.google.cloud.translate.v3.RomanizeTextRequest;
import com.google.cloud.translate.v3.RomanizeTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import dev.ghidora.utabridgeserver.exceptions.TranslationServiceException;
import dev.ghidora.utabridgeserver.exceptions.UnsupportedLanguageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleTranslateServiceTest {

  @Mock private Translate translate;

  @Mock private TranslationServiceClient translationServiceClient;

  @Mock private Translation translation;

  private GoogleTranslateService googleTranslateService;

  private static final String PROJECT_ID = "test-project";

  @BeforeEach
  void setUp() {
    googleTranslateService =
        new GoogleTranslateService(translate, translationServiceClient, PROJECT_ID);
  }

  @Test
  void translateText_CallsGoogleTranslate() {
    String text = "Hello";
    String sourceLang = "en";
    String targetLang = "ja";
    String expectedTranslation = "こんにちは";

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenReturn(translation);
    when(translation.getTranslatedText()).thenReturn(expectedTranslation);
    when(translation.getSourceLanguage()).thenReturn(sourceLang);

    TranslationService.TranslatedText result =
        googleTranslateService.translateText(text, sourceLang, targetLang);

    assertEquals(expectedTranslation, result.translatedText());
    assertEquals(sourceLang, result.detectedLanguage());
    verify(translate)
        .translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang));
  }

  @Test
  void translateText_ReturnsTranslatedText() {
    String text = "Good morning";
    String sourceLang = "en";
    String targetLang = "fr";
    String expectedTranslation = "Bonjour";

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenReturn(translation);
    when(translation.getTranslatedText()).thenReturn(expectedTranslation);
    when(translation.getSourceLanguage()).thenReturn(sourceLang);

    TranslationService.TranslatedText result =
        googleTranslateService.translateText(text, sourceLang, targetLang);

    assertEquals(expectedTranslation, result.translatedText());
  }

  @Test
  void romanizeText_CallsRomanizationApi() {
    String text = "こんにちは";
    String sourceLang = "ja";
    String expectedRomanized = "Konnichiwa";

    Romanization romanization = mock(Romanization.class);
    when(romanization.getRomanizedText()).thenReturn(expectedRomanized);

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizations(0)).thenReturn(romanization);
    when(response.getRomanizationsList())
        .thenReturn(java.util.Collections.singletonList(romanization));

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(expectedRomanized, result.romanizedText());
    assertEquals(sourceLang, result.detectedLanguage());
  }

  @Test
  void romanizeText_ReturnsRomanizedText() {
    String text = "नमस्ते";
    String sourceLang = "hi";
    String expectedRomanized = "namaste";

    Romanization romanization = mock(Romanization.class);
    when(romanization.getRomanizedText()).thenReturn(expectedRomanized);

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizations(0)).thenReturn(romanization);
    when(response.getRomanizationsList())
        .thenReturn(java.util.Collections.singletonList(romanization));

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(expectedRomanized, result.romanizedText());
  }

  @Test
  void romanizeText_KoreanText_ReturnsRomanized() {
    String text = "안녕하세요";
    String sourceLang = "ko";
    String expectedRomanized = "annyeonghaseyo";

    Romanization romanization = mock(Romanization.class);
    when(romanization.getRomanizedText()).thenReturn(expectedRomanized);

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizations(0)).thenReturn(romanization);
    when(response.getRomanizationsList())
        .thenReturn(java.util.Collections.singletonList(romanization));

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(expectedRomanized, result.romanizedText());
  }

  @Test
  void romanizeText_JapaneseText_ReturnsRomanized() {
    String text = "雪ノ下雪乃";
    String sourceLang = "ja";
    String expectedRomanized = "Yukinoshita Yukino";

    Romanization romanization = mock(Romanization.class);
    when(romanization.getRomanizedText()).thenReturn(expectedRomanized);

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizations(0)).thenReturn(romanization);
    when(response.getRomanizationsList())
        .thenReturn(java.util.Collections.singletonList(romanization));

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(expectedRomanized, result.romanizedText());
  }

  @Test
  void romanizeText_EnglishText_ReturnsOriginalText() {
    String text = "Hello World";
    String sourceLang = "en";

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(text, result.romanizedText());
    assertEquals("en", result.detectedLanguage());
    verifyNoInteractions(translationServiceClient);
  }

  @Test
  void romanizeText_EmptyRomanizationsList_ReturnsOriginalText() {
    String text = "こんにちは";
    String sourceLang = "ja";

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizationsList()).thenReturn(java.util.Collections.emptyList());

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text, sourceLang);

    assertEquals(text, result.romanizedText());
    assertEquals("en", result.detectedLanguage());
  }

  @Test
  void translateText_DifferentLanguages_ProduceCorrectTranslation() {
    String text = "Thank you";
    String sourceLang = "en";
    String targetLang = "de";
    String expectedTranslation = "Danke";

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenReturn(translation);
    when(translation.getTranslatedText()).thenReturn(expectedTranslation);
    when(translation.getSourceLanguage()).thenReturn(sourceLang);

    TranslationService.TranslatedText result =
        googleTranslateService.translateText(text, sourceLang, targetLang);

    assertEquals(expectedTranslation, result.translatedText());
  }

  @Test
  void
      translateText_InvalidArgumentExceptionWithSourceLanguage_ThrowsUnsupportedLanguageException() {
    String text = "Hello";
    String sourceLang = "xx";
    String targetLang = "en";

    InvalidArgumentException exception =
        new InvalidArgumentException(
            "source language not supported",
            new Exception("source language not supported"),
            mock(StatusCode.class),
            false);

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenThrow(exception);

    UnsupportedLanguageException thrownException =
        assertThrows(
            UnsupportedLanguageException.class,
            () -> googleTranslateService.translateText(text, sourceLang, targetLang));

    assertEquals(sourceLang, thrownException.getUnsupportedLanguage());
    assertTrue(thrownException.isSourceLanguage());
  }

  @Test
  void
      translateText_InvalidArgumentExceptionWithTargetLanguage_ThrowsUnsupportedLanguageException() {
    String text = "Hello";
    String sourceLang = "en";
    String targetLang = "invalid";

    InvalidArgumentException exception =
        new InvalidArgumentException(
            "target language is invalid",
            new Exception("target language is invalid"),
            mock(StatusCode.class),
            false);

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenThrow(exception);

    UnsupportedLanguageException thrownException =
        assertThrows(
            UnsupportedLanguageException.class,
            () -> googleTranslateService.translateText(text, sourceLang, targetLang));

    assertEquals(targetLang, thrownException.getUnsupportedLanguage());
    assertFalse(thrownException.isSourceLanguage());
  }

  @Test
  void translateText_ResourceExhaustedException_ThrowsTranslationServiceException() {
    String text = "Hello";
    String sourceLang = "en";
    String targetLang = "ja";

    ResourceExhaustedException exception =
        new ResourceExhaustedException(
            "Quota exceeded", new Exception("Quota exceeded"), mock(StatusCode.class), false);

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenThrow(exception);

    TranslationServiceException thrownException =
        assertThrows(
            TranslationServiceException.class,
            () -> googleTranslateService.translateText(text, sourceLang, targetLang));

    assertTrue(thrownException.isRetryable());
    assertTrue(thrownException.getMessage().contains("quota exceeded"));
  }

  @Test
  void translateText_UnavailableException_ThrowsTranslationServiceExceptionWithRetryableTrue() {
    String text = "Hello";
    String sourceLang = "en";
    String targetLang = "ja";

    UnavailableException exception =
        new UnavailableException(
            "Service unavailable",
            new Exception("Service unavailable"),
            mock(StatusCode.class),
            true);

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenThrow(exception);

    TranslationServiceException thrownException =
        assertThrows(
            TranslationServiceException.class,
            () -> googleTranslateService.translateText(text, sourceLang, targetLang));

    assertTrue(thrownException.isRetryable());
    assertTrue(thrownException.getMessage().contains("temporarily unavailable"));
  }

  @Test
  void
      translateText_PermissionDeniedException_ThrowsTranslationServiceExceptionWithRetryableFalse() {
    String text = "Hello";
    String sourceLang = "en";
    String targetLang = "ja";

    PermissionDeniedException exception =
        new PermissionDeniedException(
            "Permission denied", new Exception("Permission denied"), mock(StatusCode.class), false);

    when(translate.translate(
            text,
            TranslateOption.targetLanguage(targetLang),
            TranslateOption.sourceLanguage(sourceLang)))
        .thenThrow(exception);

    TranslationServiceException thrownException =
        assertThrows(
            TranslationServiceException.class,
            () -> googleTranslateService.translateText(text, sourceLang, targetLang));

    assertFalse(thrownException.isRetryable());
    assertTrue(thrownException.getMessage().contains("Authentication failed"));
  }

  @Test
  void translateText_AutoDetectSourceLanguage_TranslatesSuccessfully() {
    String text = "Hello";
    String targetLang = "ja";
    String expectedTranslation = "こんにちは";
    String detectedLang = "en";

    when(translate.translate(text, TranslateOption.targetLanguage(targetLang)))
        .thenReturn(translation);
    when(translation.getTranslatedText()).thenReturn(expectedTranslation);
    when(translation.getSourceLanguage()).thenReturn(detectedLang);

    TranslationService.TranslatedText result =
        googleTranslateService.translateText(text, targetLang);

    assertEquals(expectedTranslation, result.translatedText());
    assertEquals(detectedLang, result.detectedLanguage());
    verify(translate).translate(text, TranslateOption.targetLanguage(targetLang));
  }

  @Test
  void romanizeText_AutoDetectSourceLanguage_RomanizesSuccessfully() {
    String text = "こんにちは";
    String expectedRomanized = "Konnichiwa";
    String detectedLang = "ja";

    Romanization romanization = mock(Romanization.class);
    when(romanization.getRomanizedText()).thenReturn(expectedRomanized);
    when(romanization.getDetectedLanguageCode()).thenReturn(detectedLang);

    RomanizeTextResponse response = mock(RomanizeTextResponse.class);
    when(response.getRomanizations(0)).thenReturn(romanization);
    when(response.getRomanizationsList())
        .thenReturn(java.util.Collections.singletonList(romanization));

    when(translationServiceClient.romanizeText(any(RomanizeTextRequest.class)))
        .thenReturn(response);

    TranslationService.RomanizedText result = googleTranslateService.romanizeText(text);

    assertEquals(expectedRomanized, result.romanizedText());
    assertEquals(detectedLang, result.detectedLanguage());
  }
}
