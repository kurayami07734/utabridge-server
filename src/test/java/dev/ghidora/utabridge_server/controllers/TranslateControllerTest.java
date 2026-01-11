package dev.ghidora.utabridge_server.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ghidora.utabridge_server.services.TranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TranslateController.class)
class TranslateControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TranslationService translationService;

    @Test
    void getTranslation_ValidParams_ReturnsTranslation() throws Exception {
        // Arrange
        String text = "Hello";
        String from = "en";
        String to = "ja";
        String translated = "こんにちは";
        String romanized = "Konnichiwa";

        given(translationService.translateText(text, from, to)).willReturn(translated);
        given(translationService.romanizeText(text, from)).willReturn(romanized);

        // Act & Assert
        mockMvc.perform(
                        get("/api/translate")
                                .param("text", text)
                                .param("from", from)
                                .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translatedText").value(translated))
                .andExpect(jsonPath("$.romanizedText").value(romanized));
    }

    @Test
    void getTranslation_MissingParams_ReturnsBadRequest() throws Exception {
        // Act & Assert
        // Missing 'to' parameter
        mockMvc.perform(get("/api/translate").param("text", "Hello").param("from", "en"))
                .andExpect(status().isBadRequest());
    }
}
