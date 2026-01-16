package dev.ghidora.utabridgeserver.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TranslationTest {

  @Test
  void setSourceTerm_SetsRelationship() {
    Translation translation = new Translation();
    SourceTerm sourceTerm = new SourceTerm();

    translation.setSourceTerm(sourceTerm);

    assert translation.getSourceTerm() == sourceTerm;
  }

  @Test
  void getSourceTerm_ReturnsCorrectTerm() {
    Translation translation = new Translation();
    SourceTerm sourceTerm = new SourceTerm();
    sourceTerm.setOriginalText("Hello");

    translation.setSourceTerm(sourceTerm);

    assert translation.getSourceTerm().getOriginalText().equals("Hello");
  }

  @Test
  void setTranslatedText_UpdatesValue() {
    Translation translation = new Translation();
    translation.setTranslatedText("Bonjour");

    assert translation.getTranslatedText().equals("Bonjour");
  }

  @Test
  void setLanguageCode_UpdatesValue() {
    Translation translation = new Translation();
    translation.setLanguageCode("fr");

    assert translation.getLanguageCode().equals("fr");
  }

  @Test
  void constructor_InitializesFields() {
    Translation translation = new Translation();

    assert translation.getId() == null;
    assert translation.getTranslatedText() == null;
    assert translation.getLanguageCode() == null;
    assert translation.getSourceTerm() == null;
  }

  @Test
  void bidirectionalRelationship_Consistency() {
    Translation translation = new Translation();
    SourceTerm sourceTerm = new SourceTerm();
    translation.setLanguageCode("ja");
    translation.setTranslatedText("こんにちは");

    sourceTerm.addTranslation(translation);

    assert translation.getSourceTerm() == sourceTerm;
    assert sourceTerm.getTranslation("ja").get() == translation;
  }
}
