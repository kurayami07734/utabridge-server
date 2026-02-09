package dev.ghidora.utabridgeserver.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.exceptions.GlobalExceptionHandler;
import dev.ghidora.utabridgeserver.exceptions.TranslationServiceException;
import dev.ghidora.utabridgeserver.exceptions.UnsupportedLanguageException;
import dev.ghidora.utabridgeserver.services.LocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class LocalizeControllerTest {

  private MockMvc mockMvc;
  private LocalizationService localizationService;
  private ObjectMapper objectMapper;

  private record LocalizeRequest(String text, String language) {}

  @BeforeEach
  void setUp() {
    localizationService = mock(LocalizationService.class);
    LocalizeController localizeController = new LocalizeController(localizationService);
    objectMapper = new ObjectMapper();
    mockMvc =
        standaloneSetup(localizeController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void localize_ValidRequest_Returns200() throws Exception {
    String text = "Hello";
    String language = "ja";
    String translated = "こんにちは";
    String romanized = "Konnichiwa";

    LocalizeRequest request = new LocalizeRequest(text, language);
    LocalizeResponse response = new LocalizeResponse(translated, romanized);

    given(localizationService.localize(text, language)).willReturn(response);

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.translatedText").value(translated))
        .andExpect(jsonPath("$.romanizedText").value(romanized));
  }

  @Test
  void localize_ValidParams_ReturnsCorrectResponse() throws Exception {
    String text = "Good morning";
    String language = "fr";
    String translated = "Bonjour";
    String romanized = "Good morning";

    LocalizeRequest request = new LocalizeRequest(text, language);
    LocalizeResponse response = new LocalizeResponse(translated, romanized);

    given(localizationService.localize(text, language)).willReturn(response);

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.translatedText").value(translated))
        .andExpect(jsonPath("$.romanizedText").value(romanized));
  }

  @Test
  void localize_MissingText_ReturnsBadRequest() throws Exception {
    String invalidJson = "{\"language\": \"ja\"}";

    mockMvc
        .perform(post("/api/localize").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void localize_ServiceCalledWithCorrectParams() throws Exception {
    String text = "Hello";
    String language = "ja";
    LocalizeRequest request = new LocalizeRequest(text, language);
    LocalizeResponse response = new LocalizeResponse("こんにちは", "Konnichiwa");

    given(localizationService.localize(text, language)).willReturn(response);

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(localizationService).localize(text, language);
  }

  @Test
  void localize_EmptyBody_ReturnsBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/localize").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void localize_UnsupportedLanguage_Returns400WithUnsupportedLanguageErrorType() throws Exception {
    String text = "Hello";
    String language = "en";
    LocalizeRequest request = new LocalizeRequest(text, language);

    given(localizationService.localize(text, language))
        .willThrow(new UnsupportedLanguageException("unknown", true));

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("UNSUPPORTED_LANGUAGE"))
        .andExpect(jsonPath("$.message").value("Language 'unknown' is not supported for source"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.path").value("/api/localize"));
  }

  @Test
  void localize_TranslationServiceUnavailable_Returns503WithServiceUnavailableErrorType()
      throws Exception {
    String text = "Hello";
    String language = "ja";
    LocalizeRequest request = new LocalizeRequest(text, language);

    given(localizationService.localize(text, language))
        .willThrow(
            new TranslationServiceException(
                "Translation service is temporarily unavailable. Please try again later.",
                true,
                new Exception("Service unavailable")));

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("TRANSLATION_SERVICE_UNAVAILABLE"))
        .andExpect(
            jsonPath("$.message")
                .value("Translation service is temporarily unavailable. Please try again later."))
        .andExpect(jsonPath("$.status").value(503))
        .andExpect(jsonPath("$.path").value("/api/localize"));
  }
}
