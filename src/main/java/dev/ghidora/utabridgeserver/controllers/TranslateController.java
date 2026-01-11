package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for translation endpoints. */
@RestController
public class TranslateController {

  private final TranslationService translationService;

  @Autowired
  public TranslateController(TranslationService translationService) {
    this.translationService = translationService;
  }

  /**
   * Response payload for translation.
   *
   * @param translatedText The translated text.
   * @param romanizedText The romanized text.
   */
  public record TranslateResponse(String translatedText, String romanizedText) {}

  /**
   * Handles translation requests.
   *
   * @param text The text to translate.
   * @param from The source language code.
   * @param to The target language code.
   * @return The translation response.
   */
  @GetMapping("/api/translate")
  public TranslateResponse getTranslation(
      @RequestParam String text, @RequestParam String from, @RequestParam String to) {
    var translated = translationService.translateText(text, from, to);
    var romanized = translationService.romanizeText(text, from);
    return new TranslateResponse(translated, romanized);
  }
}
