package dev.ghidora.utabridgeserver.configs;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.v3.TranslationServiceClient;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configuration for Google Cloud Translate. */
@Configuration
@Profile("!test")
public class GoogleTranslateConfig {

  /**
   * Creates the "Basic" client. It will automatically find your credentials from the
   * GOOGLE_APPLICATION_CREDENTIALS environment variable.
   */
  @Bean
  public Translate translate() {
    return TranslateOptions.getDefaultInstance().getService();
  }

  /**
   * Creates the "Advanced" v3 client. This is needed for romanization. It also finds credentials
   * automatically.
   */
  @Bean
  public TranslationServiceClient translationServiceClient() throws IOException {
    return TranslationServiceClient.create();
  }
}
