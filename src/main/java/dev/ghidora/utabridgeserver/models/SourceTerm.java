package dev.ghidora.utabridgeserver.models;

import dev.ghidora.utabridgeserver.enums.TextTag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Entity representing a source term with its translations. */
@Entity
@Table(name = "source_terms")
public class SourceTerm {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String originalText;

  @Column(nullable = false)
  private String languageCode;

  @Column(nullable = false)
  private String romanizedText;

  @ElementCollection
  @CollectionTable(name = "source_term_tags", joinColumns = @JoinColumn(name = "source_term_id"))
  @Column(name = "tag")
  @Enumerated(EnumType.STRING)
  private List<TextTag> tags = new ArrayList<>();

  @OneToMany(mappedBy = "sourceTerm", cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey(name = "languageCode")
  private final Map<String, Translation> translationMap = new HashMap<>();

  public void addTranslation(Translation translation) {
    translationMap.put(translation.getLanguageCode(), translation);
    translation.setSourceTerm(this);
  }

  public Optional<Translation> getTranslation(String langCode) {
    return Optional.ofNullable(translationMap.get(langCode));
  }

  public Long getId() {
    return id;
  }

  public String getOriginalText() {
    return originalText;
  }

  public void setOriginalText(String originalText) {
    this.originalText = originalText;
  }

  public String getRomanizedText() {
    return romanizedText;
  }

  public void setRomanizedText(String romanizedText) {
    this.romanizedText = romanizedText;
  }

  public List<TextTag> getTags() {
    return tags;
  }

  public void setTags(List<TextTag> tags) {
    this.tags = tags;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }
}
