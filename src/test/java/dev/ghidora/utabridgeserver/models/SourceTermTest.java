package dev.ghidora.utabridgeserver.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SourceTermTest {

  @Test
  void addTranslation_BidirectionalSet_SetsSourceTermOnTranslation() {
    SourceTerm sourceTerm = new SourceTerm();
    Translation translation = new Translation();
    translation.setLanguageCode("ja");
    translation.setTranslatedText("こんにちは");

    sourceTerm.addTranslation(translation);

    assert translation.getSourceTerm() == sourceTerm;
  }

  @Test
  void addTranslation_AddsToMap() {
    SourceTerm sourceTerm = new SourceTerm();
    Translation translation = new Translation();
    translation.setLanguageCode("ja");
    translation.setTranslatedText("こんにちは");

    sourceTerm.addTranslation(translation);

    Optional<Translation> retrieved = sourceTerm.getTranslation("ja");
    assert retrieved.isPresent();
    assert retrieved.get() == translation;
  }

  @Test
  void getTranslation_ExistingLang_ReturnsTranslation() {
    SourceTerm sourceTerm = new SourceTerm();
    Translation translation = new Translation();
    translation.setLanguageCode("ja");
    translation.setTranslatedText("こんにちは");
    sourceTerm.addTranslation(translation);

    Optional<Translation> result = sourceTerm.getTranslation("ja");

    assert result.isPresent();
    assert result.get().getTranslatedText().equals("こんにちは");
  }

  @Test
  void getTranslation_NonExistingLang_ReturnsEmpty() {
    SourceTerm sourceTerm = new SourceTerm();

    Optional<Translation> result = sourceTerm.getTranslation("fr");

    assert result.isEmpty();
  }

  @Test
  void addTranslation_MultipleTranslations_DifferentLanguages() {
    SourceTerm sourceTerm = new SourceTerm();
    Translation translationJa = new Translation();
    translationJa.setLanguageCode("ja");
    translationJa.setTranslatedText("こんにちは");

    Translation translationFr = new Translation();
    translationFr.setLanguageCode("fr");
    translationFr.setTranslatedText("Bonjour");

    sourceTerm.addTranslation(translationJa);
    sourceTerm.addTranslation(translationFr);

    assert sourceTerm.getTranslation("ja").isPresent();
    assert sourceTerm.getTranslation("fr").isPresent();
    assert sourceTerm.getTranslation("de").isEmpty();
  }

  @Test
  void addTranslation_ReplacesExistingTranslation_SameLanguage() {
    SourceTerm sourceTerm = new SourceTerm();
    Translation translation1 = new Translation();
    translation1.setLanguageCode("ja");
    translation1.setTranslatedText("古い翻訳");

    Translation translation2 = new Translation();
    translation2.setLanguageCode("ja");
    translation2.setTranslatedText("新しい翻訳");

    sourceTerm.addTranslation(translation1);
    sourceTerm.addTranslation(translation2);

    Optional<Translation> result = sourceTerm.getTranslation("ja");
    assert result.isPresent();
    assert result.get().getTranslatedText().equals("新しい翻訳");
  }

  @Test
  void setOriginalText_UpdatesValue() {
    SourceTerm sourceTerm = new SourceTerm();
    sourceTerm.setOriginalText("Hello World");

    assert sourceTerm.getOriginalText().equals("Hello World");
  }

  @Test
  void setRomanizedText_UpdatesValue() {
    SourceTerm sourceTerm = new SourceTerm();
    sourceTerm.setRomanizedText("Konnichiwa");

    assert sourceTerm.getRomanizedText().equals("Konnichiwa");
  }

  @Test
  void setLanguageCode_UpdatesValue() {
    SourceTerm sourceTerm = new SourceTerm();
    sourceTerm.setLanguageCode("en");

    assert sourceTerm.getLanguageCode().equals("en");
  }

  @Test
  void constructor_InitializesEmptyLists() {
    SourceTerm sourceTerm = new SourceTerm();

    assertNotNull(sourceTerm.getTags());
    assertTrue(sourceTerm.getTags().isEmpty());
  }
}
