package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.dtos.LocalizeResponse;
import dev.ghidora.utabridgeserver.services.LocalizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for translation endpoints. */
@RestController
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
  @PostMapping("/api/localize")
  public LocalizeResponse getTranslation(@Valid @RequestBody LocalizeRequest payload) {
    return localizationService.localize(
        payload.text(), payload.fromLanguage(), payload.toLanguage());
  }
}
