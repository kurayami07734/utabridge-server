package dev.ghidora.utabridgeserver.services;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for detecting the language of given text using the Lingua library. */
@Service
public class LanguageDetectionService {

  private LanguageDetector detector;
  private static final Logger logger = LoggerFactory.getLogger(LanguageDetectionService.class);

  /** Initializes the language detector with supported languages. */
  @PostConstruct
  public void init() {
    detector =
        LanguageDetectorBuilder.fromLanguages(Language.CHINESE, Language.JAPANESE, Language.KOREAN)
            .build();
    logger.info("LanguageDetectionService initialized with EN, ZH, JA, KO support");
  }

  /**
   * Detects the language of the given text.
   *
   * @param text The text to detect language for.
   * @return The ISO 639-1 language code (e.g., "en", "ja", "ko", "zh").
   */
  public Optional<String> detectLanguage(String text) {
    if (text == null || text.trim().isEmpty()) {
      return Optional.empty();
    }

    SortedMap<Language, Double> confidenceValues = detector.computeLanguageConfidenceValues(text);

    if (confidenceValues.isEmpty()) {
      logger.warn("Language detection failed (Empty results) for text: '{}'", text);
      return Optional.empty();
    }

    Language bestLanguage = confidenceValues.firstKey();
    Double confidence = confidenceValues.get(bestLanguage);

    if (bestLanguage == Language.UNKNOWN) {
      return Optional.empty();
    }

    if (confidence < 0.5) {
      logger.warn("Confidence too low ({}) for text: '{}'", confidence, text);
      return Optional.empty();
    }

    String languageCode = bestLanguage.getIsoCode639_1().toString().toLowerCase();

    logger.debug("Detected '{}' as language: {} (Confidence: {})", text, languageCode, confidence);
    return Optional.of(languageCode);
  }
}
