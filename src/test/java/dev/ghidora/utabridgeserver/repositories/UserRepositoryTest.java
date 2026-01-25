package dev.ghidora.utabridgeserver.repositories;

import static org.junit.jupiter.api.Assertions.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import dev.ghidora.utabridgeserver.models.User;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRepository userRepository;

  @Test
  void saveUser_WithPreferences_PersistedCorrectly() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setPictureUrl("https://example.com/avatar.jpg");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);
    user.setLastActiveAt(Instant.now());

    Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    user.setPreferences(preferences);

    // Act
    User savedUser = userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    // Assert
    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertNotNull(foundUser);
    assertEquals("test@example.com", foundUser.getEmail());
    assertEquals("Test User", foundUser.getName());
    assertNotNull(foundUser.getPreferences());
    assertEquals(
        "TRANSLATION", foundUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void findUser_WithPreferences_LoadedCorrectly() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);

    Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "ROMANIZATION");
    user.setPreferences(preferences);

    // Act
    User savedUser = entityManager.persistAndFlush(user);
    entityManager.clear();

    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

    // Assert
    assertNotNull(foundUser);
    assertNotNull(foundUser.getPreferences());
    assertEquals(1, foundUser.getPreferences().size());
    assertEquals(
        "ROMANIZATION", foundUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void updateUser_PreferencesUpdated_ReflectedInDatabase() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);

    Map<UserPreferenceType, String> initialPreferences = new EnumMap<>(UserPreferenceType.class);
    initialPreferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "ROMANIZATION");
    user.setPreferences(initialPreferences);

    User savedUser = entityManager.persistAndFlush(user);
    entityManager.clear();

    // Act
    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertNotNull(foundUser);

    Map<UserPreferenceType, String> updatedPreferences = new EnumMap<>(UserPreferenceType.class);
    updatedPreferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    foundUser.setPreferences(updatedPreferences);

    userRepository.save(foundUser);
    entityManager.flush();
    entityManager.clear();

    // Assert
    User finalUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertNotNull(finalUser);
    assertEquals(
        "TRANSLATION", finalUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void saveUser_WithoutPreferences_UsesEmptyMap() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);
    // Explicitly not setting preferences to test default behavior

    // Act
    User savedUser = userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    // Assert
    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertNotNull(foundUser);
    assertNotNull(foundUser.getPreferences());
    assertTrue(foundUser.getPreferences().isEmpty());
  }

  @Test
  void findByEmail_ExistingUser_WithPreferences_ReturnsUser() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);

    Map<UserPreferenceType, String> preferences = new EnumMap<>(UserPreferenceType.class);
    preferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    user.setPreferences(preferences);

    entityManager.persistAndFlush(user);
    entityManager.clear();

    // Act
    User foundUser = userRepository.findByEmail("test@example.com").orElse(null);

    // Assert
    assertNotNull(foundUser);
    assertEquals("test@example.com", foundUser.getEmail());
    assertNotNull(foundUser.getPreferences());
    assertEquals(
        "TRANSLATION", foundUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void findByEmail_NonExistentUser_ReturnsEmpty() {
    // Act
    User foundUser = userRepository.findByEmail("nonexistent@example.com").orElse(null);

    // Assert
    assertNull(foundUser);
  }

  @Test
  void updateLastActiveat_UpdatesCorrectly() {
    // Arrange
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);
    user.setLastActiveAt(Instant.now().minusSeconds(3600)); // 1 hour ago

    User savedUser = entityManager.persistAndFlush(user);
    entityManager.clear();

    Instant newActiveTime = Instant.now();

    // Act
    userRepository.updateLastActiveAt(savedUser.getId(), newActiveTime);
    entityManager.flush();
    entityManager.clear();

    // Assert
    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertNotNull(foundUser);
    // Database may truncate nanoseconds, so compare with tolerance
    assertEquals(
        newActiveTime.truncatedTo(java.time.temporal.ChronoUnit.MILLIS),
        foundUser.getLastActiveAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
  }
}
