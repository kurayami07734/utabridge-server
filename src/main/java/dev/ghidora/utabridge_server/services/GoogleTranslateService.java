package dev.ghidora.utabridge_server.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.RomanizeTextRequest;
import com.google.cloud.translate.v3.RomanizeTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class GoogleTranslateService implements TranslationService {

    private final Translate translate;
    private final TranslationServiceClient translationServiceClient;
    private final String projectId;

    @Autowired
    public GoogleTranslateService(Translate translate,
                                  TranslationServiceClient translationServiceClient,
                                  @Value("${gcp.project-id}") String projectId) {
        this.translate = translate;
        this.translationServiceClient = translationServiceClient;
        this.projectId = projectId;
    }

    @Override
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        Translation translation = translate.translate(
                text,
                TranslateOption.sourceLanguage(sourceLanguage),
                TranslateOption.targetLanguage(targetLanguage)
        );
        return translation.getTranslatedText();
    }

    @Override
    public String romanizeText(String text, String sourceLanguage) {
        // Location is always "global" for this API
        LocationName parent = LocationName.of(projectId, "global");

        RomanizeTextRequest request = RomanizeTextRequest.newBuilder()
                .setParent(parent.toString())
                .addContents(text)
                .setSourceLanguageCode(sourceLanguage)
                .build();

        RomanizeTextResponse response = translationServiceClient.romanizeText(request);

        return response.getRomanizations(0).getRomanizedText();
    }
}
