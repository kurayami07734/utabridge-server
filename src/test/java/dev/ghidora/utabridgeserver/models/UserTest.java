package dev.ghidora.utabridgeserver.models;

import static org.junit.jupiter.api.Assertions.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void setEmail_UpdatesValue() {
    User user = new User();
    user.setEmail("test@example.com");

    assertEquals("test@example.com", user.getEmail());
  }

  @Test
  void setName_UpdatesValue() {
    User user = new User();
    user.setName("Test User");

    assertEquals("Test User", user.getName());
  }

  @Test
  void setPictureUrl_UpdatesValue() {
    User user = new User();
    user.setPictureUrl("https://example.com/avatar.jpg");

    assertEquals("https://example.com/avatar.jpg", user.getPictureUrl());
  }

  @Test
  void setPictureUrl_CanBeNull() {
    User user = new User();
    user.setPictureUrl(null);

    assertNull(user.getPictureUrl());
  }

  @Test
  void setProviderId_UpdatesValue() {
    User user = new User();
    user.setProviderId("123456789");

    assertEquals("123456789", user.getProviderId());
  }

  @Test
  void setProvider_UpdatesValue() {
    User user = new User();
    user.setProvider(IdentityProvider.GOOGLE);

    assertEquals(IdentityProvider.GOOGLE, user.getProvider());
  }

  @Test
  void setProvider_DiscordProvider() {
    User user = new User();
    user.setProvider(IdentityProvider.DISCORD);

    assertEquals(IdentityProvider.DISCORD, user.getProvider());
  }

  @Test
  void setLastActiveAt_UpdatesValue() {
    User user = new User();
    Instant now = Instant.now();
    user.setLastActiveAt(now);

    assertEquals(now, user.getLastActiveAt());
  }

  @Test
  void setLastActiveAt_CanBeNull() {
    User user = new User();
    user.setLastActiveAt(null);

    assertNull(user.getLastActiveAt());
  }

  @Test
  void constructor_InitializesFields() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getName());
    assertNull(user.getPictureUrl());
    assertNull(user.getProviderId());
    assertNull(user.getProvider());
    assertNull(user.getLastActiveAt());
  }

  @Test
  void allSetters_UpdateValues() {
    User user = new User();
    Instant now = Instant.now();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setPictureUrl("https://example.com/avatar.jpg");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);
    user.setLastActiveAt(now);

    assertEquals("test@example.com", user.getEmail());
    assertEquals("Test User", user.getName());
    assertEquals("https://example.com/avatar.jpg", user.getPictureUrl());
    assertEquals("12345", user.getProviderId());
    assertEquals(IdentityProvider.GOOGLE, user.getProvider());
    assertEquals(now, user.getLastActiveAt());
  }

  @Test
  void addPreference_ValidValue_AddsSuccessfully() {
    User user = new User();

    user.addPreference(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");

    assertEquals("TRANSLATION", user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void addPreference_InvalidValue_ThrowsIllegalArgumentException() {
    User user = new User();

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> user.addPreference(UserPreferenceType.PRIMARY_TEXT_TYPE, "INVALID_VALUE"));

    assertTrue(
        exception
            .getMessage()
            .contains("Invalid value 'INVALID_VALUE' for preference PRIMARY_TEXT_TYPE"));
    assertTrue(user.getPreferences().isEmpty());
  }

  @Test
  void addPreference_NullValue_ThrowsIllegalArgumentException() {
    User user = new User();

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> user.addPreference(UserPreferenceType.PRIMARY_TEXT_TYPE, null));

    assertTrue(
        exception.getMessage().contains("Invalid value 'null' for preference PRIMARY_TEXT_TYPE"));
    assertTrue(user.getPreferences().isEmpty());
  }

  @Test
  void initializeDefaultPreferences_SetsAllDefaults() {
    User user = new User();

    user.initializeDefaultPreferences();

    assertEquals(1, user.getPreferences().size());
    assertEquals("ROMANIZATION", user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void initializeDefaultPreferences_WithExistingPreferences_DoesNotOverwrite() {
    User user = new User();

    // Set existing preference
    user.getPreferences().put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");

    user.initializeDefaultPreferences();

    assertEquals(1, user.getPreferences().size());
    assertEquals("TRANSLATION", user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void setPreferences_WithNullMap_HandlesGracefully() {
    User user = new User();

    user.setPreferences(null);

    assertNull(user.getPreferences());
  }

  @Test
  void setPreferences_WithValidMap_UpdatesPreferences() {
    User user = new User();

    EnumMap<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");

    user.setPreferences(preferences);

    assertEquals(preferences, user.getPreferences());
    assertEquals("TRANSLATION", user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void getPreferences_ReturnsCorrectPreferences() {
    User user = new User();

    EnumMap<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    user.setPreferences(preferences);

    Map<UserPreferenceType, String> retrievedPreferences = user.getPreferences();

    assertEquals(preferences, retrievedPreferences);
    assertEquals("TRANSLATION", retrievedPreferences.get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void preferences_DefaultToEmptyMap() {
    User user = new User();

    Map<UserPreferenceType, String> preferences = user.getPreferences();

    assertNotNull(preferences);
    assertTrue(preferences.isEmpty());
  }

  @Test
  void addPreference_MultiplePreferences_AddsAllSuccessfully() {
    User user = new User();

    user.addPreference(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    user.addPreference(
        UserPreferenceType.PRIMARY_TEXT_TYPE, "ROMANIZATION"); // Overwrite same preference

    assertEquals(1, user.getPreferences().size());
    assertEquals("ROMANIZATION", user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }
}
