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
import dev.ghidora.utabridgeserver.services.LocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class LocalizeControllerTest {

  private MockMvc mockMvc;
  private LocalizationService localizationService;
  private ObjectMapper objectMapper;

  private record LocalizeRequest(String text, String fromLanguage, String toLanguage) {}

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
    String from = "en";
    String to = "ja";
    String translated = "こんにちは";
    String romanized = "Konnichiwa";

    LocalizeRequest request = new LocalizeRequest(text, from, to);
    LocalizeResponse response = new LocalizeResponse(translated, romanized);

    given(localizationService.localize(text, from, to)).willReturn(response);

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
    String from = "en";
    String to = "fr";
    String translated = "Bonjour";
    String romanized = "Good morning";

    LocalizeRequest request = new LocalizeRequest(text, from, to);
    LocalizeResponse response = new LocalizeResponse(translated, romanized);

    given(localizationService.localize(text, from, to)).willReturn(response);

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
    String invalidJson = "{\"fromLanguage\": \"en\", \"toLanguage\": \"ja\"}";

    mockMvc
        .perform(post("/api/localize").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void localize_ServiceCalledWithCorrectParams() throws Exception {
    String text = "Hello";
    String from = "en";
    String to = "ja";
    LocalizeRequest request = new LocalizeRequest(text, from, to);
    LocalizeResponse response = new LocalizeResponse("こんにちは", "Konnichiwa");

    given(localizationService.localize(text, from, to)).willReturn(response);

    mockMvc
        .perform(
            post("/api/localize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(localizationService).localize(text, from, to);
  }

  @Test
  void localize_EmptyBody_ReturnsBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/localize").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}
