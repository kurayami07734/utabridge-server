package dev.ghidora.utabridgeserver.models;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/** User entity. */
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true)
  private String pictureUrl;

  @Column(nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IdentityProvider provider;

  @Column(nullable = true)
  private Instant lastActiveAt;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "user_preferences", joinColumns = @JoinColumn(name = "user_id"))
  @MapKeyEnumerated(EnumType.STRING)
  @MapKeyColumn(name = "preference_key")
  @Column(name = "preference_value")
  private Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPictureUrl() {
    return pictureUrl;
  }

  public void setPictureUrl(String pictureUrl) {
    this.pictureUrl = pictureUrl;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public IdentityProvider getProvider() {
    return provider;
  }

  public void setProvider(IdentityProvider provider) {
    this.provider = provider;
  }

  public Instant getLastActiveAt() {
    return lastActiveAt;
  }

  public Map<UserPreferenceType, String> getPreferences() {
    return preferences;
  }

  public void setPreferences(Map<UserPreferenceType, String> preferences) {
    this.preferences = preferences;
  }

  /**
   * Adds a preference to the user.
   *
   * @param key The preference key.
   * @param value The preference value.
   * @throws IllegalArgumentException if the value is invalid for the given key.
   */
  public void addPreference(UserPreferenceType key, String value) throws IllegalArgumentException {
    if (!key.isValid(value)) {
      throw new IllegalArgumentException("Invalid value '" + value + "' for preference " + key);
    }
    this.preferences.put(key, value);
  }

  /** Initializes default preferences for the user if they are not already set. */
  public void initializeDefaultPreferences() {
    for (UserPreferenceType pref : UserPreferenceType.values()) {
      preferences.putIfAbsent(pref, pref.getDefaultValue());
    }
  }

  public void setLastActiveAt(Instant lastActiveAt) {
    this.lastActiveAt = lastActiveAt;
  }
}
