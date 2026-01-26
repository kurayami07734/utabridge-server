package dev.ghidora.utabridgeserver.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import dev.ghidora.utabridgeserver.exceptions.ResourceNotFoundException;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  void getOrCreateUser_ExistingUser_ReturnsUser() {
    // Arrange
    String email = "test@example.com";
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", 1L);
    existingUser.setEmail(email);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // Act
    userService.getOrCreateUser(email, "name", "url", "123", IdentityProvider.GOOGLE);

    // Assert
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void getOrCreateUser_NewUser_CreatesAndReturnsUser() {
    // Arrange
    String email = "new@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    userService.getOrCreateUser(email, "name", "url", "123", IdentityProvider.GOOGLE);

    // Assert
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updatePreferences_ValidUserAndPreferences_UpdatesSuccessfully() {
    // Arrange
    Long userId = 1L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", userId);
    existingUser.setEmail("test@example.com");
    existingUser.setName("Test User");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);

    // Act
    User result = userService.updatePreferences(userId, preferences);

    // Assert
    verify(userRepository).findById(userId);
    verify(userRepository).save(existingUser);
    assertEquals(
        "TRANSLATION", existingUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void updatePreferences_UserNotFound_ThrowsResourceNotFoundException() {
    // Arrange
    Long userId = 999L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updatePreferences(userId, preferences));
    assertEquals("User not found", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePreferences_InvalidPreferenceValue_ThrowsIllegalArgumentException() {
    // Arrange
    Long userId = 1L;
    Map<String, String> invalidPreferences = Map.of("PRIMARY_TEXT_TYPE", "INVALID_VALUE");
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", userId);
    existingUser.setEmail("test@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.updatePreferences(userId, invalidPreferences));
    assertTrue(
        exception
            .getMessage()
            .contains("Invalid value 'INVALID_VALUE' for preference PRIMARY_TEXT_TYPE"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePreferences_InvalidPreferenceKey_ThrowsIllegalArgumentException() {
    // Arrange
    Long userId = 1L;
    Map<String, String> invalidPreferences = Map.of("INVALID_KEY", "TRANSLATION");
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", userId);
    existingUser.setEmail("test@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.updatePreferences(userId, invalidPreferences));
    assertTrue(exception.getMessage().contains("No enum constant"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePreferences_NullPreferences_HandlesGracefully() {
    // Arrange
    Long userId = 1L;
    Map<String, String> nullPreferences = null;
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", userId);
    existingUser.setEmail("test@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);

    // Act
    User result = userService.updatePreferences(userId, nullPreferences);

    // Assert
    verify(userRepository).findById(userId);
    verify(userRepository).save(existingUser);
  }

  @Test
  void updatePreferences_MultiplePreferences_UpdatesSuccessfully() {
    // Arrange
    Long userId = 1L;
    Map<String, String> preferences =
        Map.of(
            "PRIMARY_TEXT_TYPE", "TRANSLATION"
            // Add more preferences when they are implemented
            );
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", userId);
    existingUser.setEmail("test@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);

    // Act
    User result = userService.updatePreferences(userId, preferences);

    // Assert
    verify(userRepository).findById(userId);
    verify(userRepository).save(existingUser);
    assertEquals(
        "TRANSLATION", existingUser.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE));
  }

  @Test
  void getOrCreateUser_NewUser_InitializesDefaultPreferences() {
    // Arrange
    String email = "new@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    userService.getOrCreateUser(email, "name", "url", "123", IdentityProvider.GOOGLE);

    // Assert
    verify(userRepository)
        .save(
            argThat(
                user ->
                    user.getPreferences().containsKey(UserPreferenceType.PRIMARY_TEXT_TYPE)
                        && "ROMANIZATION"
                            .equals(
                                user.getPreferences().get(UserPreferenceType.PRIMARY_TEXT_TYPE))));
  }
}
