package dev.ghidora.utabridgeserver.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entity representing a translation of a source term in a specific language. */
@Entity
@Table(name = "translations")
public class Translation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String languageCode;

  @Column(nullable = false)
  private String translatedText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_term_id")
  private SourceTerm sourceTerm;

  public Long getId() {
    return id;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public String getTranslatedText() {
    return translatedText;
  }

  public void setTranslatedText(String translatedText) {
    this.translatedText = translatedText;
  }

  public SourceTerm getSourceTerm() {
    return sourceTerm;
  }

  public void setSourceTerm(SourceTerm sourceTerm) {
    this.sourceTerm = sourceTerm;
  }
}
