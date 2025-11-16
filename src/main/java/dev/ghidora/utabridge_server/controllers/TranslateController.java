package dev.ghidora.utabridge_server.controllers;

import dev.ghidora.utabridge_server.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslateController {

    private final TranslationService translationService;

    @Autowired
    public TranslateController(TranslationService translationService) {
        this.translationService = translationService;
    }

    public record TranslateResponse(String translatedText, String romanizedText) {}

    @GetMapping("/api/translate")
    public TranslateResponse getTranslation(@RequestParam String text,
                                 @RequestParam String from,
                                 @RequestParam String to) {
        var translated = translationService.translateText(text, from, to);
        var romanized = translationService.romanizeText(text, from);
        return new TranslateResponse(translated, romanized);
    }
}
