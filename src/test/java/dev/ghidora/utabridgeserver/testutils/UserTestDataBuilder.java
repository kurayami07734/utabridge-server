package dev.ghidora.utabridgeserver.testutils;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import dev.ghidora.utabridgeserver.models.User;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public class UserTestDataBuilder {

  public static User createTestUser() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setPictureUrl("https://example.com/avatar.jpg");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);
    user.setLastActiveAt(Instant.now());
    return user;
  }

  public static User createTestUserWithPreferences() {
    User user = createTestUser();
    Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "ROMANIZATION");
    user.setPreferences(preferences);
    return user;
  }

  public static Map<String, String> createValidPreferences() {
    return Map.of(
        "PRIMARY_TEXT_TYPE", "TRANSLATION"
        // Add more preferences when they are implemented
        );
  }

  public static Map<String, String> createInvalidPreferences() {
    return Map.of("PRIMARY_TEXT_TYPE", "INVALID_VALUE");
  }

  public static Map<String, String> createEmptyPreferences() {
    return Map.of();
  }

  public static Map<String, String> createMixedPreferences() {
    return Map.of(
        "PRIMARY_TEXT_TYPE", "ROMANIZATION"
        // Add more preferences when they are implemented
        );
  }

  public static Map<UserPreferenceType, String> createUserPreferenceMap() {
    Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "ROMANIZATION");
    return preferences;
  }
}
