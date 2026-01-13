package dev.ghidora.utabridgeserver.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
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

    // Act
    userService.getOrCreateUser(email, "name", "url", "123", IdentityProvider.GOOGLE);

    // Assert
    verify(userRepository).save(any(User.class));
  }
}
