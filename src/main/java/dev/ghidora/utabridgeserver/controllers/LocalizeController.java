package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.services.LocalizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for translation endpoints. */
@RestController
@Tag(name = "Localization", description = "Endpoints for text translation and romanization")
@SecurityRequirement(name = "bearer-jwt")
public class LocalizeController {
  private final LocalizationService localizationService;

  public LocalizeController(LocalizationService localizationService) {
    this.localizationService = localizationService;
  }

  /** Request payload for localization. */
  public record LocalizeRequest(
      @NotBlank String text, @NotBlank String fromLanguage, @NotBlank String toLanguage) {}

  /**
   * Handles localize requests.
   *
   * @param payload LocalizeRequest
   * @return The translation response.
   */
  @Operation(
      summary = "Translate and romanize text",
      description =
          "Translates text from the source language to the target language and returns the"
              + " romanized version of the source text. Results are cached for performance.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful translation",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Japanese to English",
                            value =
                                "{\"translatedText\": \"Hello\", \"romanizedText\":"
                                    + " \"Konnichiwa\"}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Validation Error",
                            value =
                                "{\"error\": \"VALIDATION_ERROR\", \"message\": \"text: must not be"
                                    + " blank\", \"status\": 400}"))),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or invalid JWT token",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Unauthorized",
                            value =
                                "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication"
                                    + " required\"}"))),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Rate Limited",
                            value =
                                "{\"error\": \"RATE_LIMIT_EXCEEDED\", \"message\": \"Too many"
                                    + " requests\", \"status\": 429}")))
      })
  @PostMapping("/api/localize")
  public LocalizeResponse getTranslation(@Valid @RequestBody LocalizeRequest payload) {
    return localizationService.localize(
        payload.text(), payload.fromLanguage(), payload.toLanguage());
  }
}
