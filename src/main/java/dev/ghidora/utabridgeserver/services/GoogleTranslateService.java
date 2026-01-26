package dev.ghidora.utabridgeserver.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.RomanizeTextRequest;
import com.google.cloud.translate.v3.RomanizeTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Implementation of TranslationService using Google Cloud Translate. */
@Service
@Primary
public class GoogleTranslateService implements TranslationService {

  private final Translate translate;
  private final TranslationServiceClient translationServiceClient;
  private final String projectId;
  private static final Logger logger = LoggerFactory.getLogger(GoogleTranslateService.class);

  /**
   * Constructs a GoogleTranslateService.
   *
   * @param translate The Google Translate client.
   * @param translationServiceClient The Google Translation Service Client (v3).
   * @param projectId The GCP Project ID.
   */
  @Autowired
  public GoogleTranslateService(
      Translate translate,
      TranslationServiceClient translationServiceClient,
      @Value("${gcp.project-id}") String projectId) {
    this.translate = translate;
    this.translationServiceClient = translationServiceClient;
    this.projectId = projectId;
  }

  @Override
  public String translateText(String text, String sourceLanguage, String targetLanguage) {
    logger.debug(
        "Calling Google Translate API to translate text from {} to {}",
        sourceLanguage,
        targetLanguage);
    Translation translation =
        translate.translate(
            text,
            TranslateOption.sourceLanguage(sourceLanguage),
            TranslateOption.targetLanguage(targetLanguage));
    String translatedText = translation.getTranslatedText();
    logger.debug("Successfully translated text. Result: '{}'", translatedText);
    return translatedText;
  }

  @Override
  public String romanizeText(String text, String sourceLanguage) {
    logger.debug("Calling Google Translate API to romanize text from {}", sourceLanguage);
    // Location is always "global" for this API
    LocationName parent = LocationName.of(projectId, "global");

    RomanizeTextRequest request =
        RomanizeTextRequest.newBuilder()
            .setParent(parent.toString())
            .addContents(text)
            .setSourceLanguageCode(sourceLanguage)
            .build();

    RomanizeTextResponse response = translationServiceClient.romanizeText(request);

    String romanizedText = response.getRomanizations(0).getRomanizedText();
    logger.debug("Successfully romanized text. Result: '{}'", romanizedText);
    return romanizedText;
  }
}
