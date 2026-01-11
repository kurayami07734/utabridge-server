package dev.ghidora.utabridgeserver.configs;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestGoogleTranslateConfig {

  @Bean
  public Translate translate() {
    return Mockito.mock(Translate.class);
  }

  @Bean
  public TranslationServiceClient translationServiceClient() {
    return Mockito.mock(TranslationServiceClient.class);
  }
}
