package dev.ghidora.utabridge_server.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridge_server.enums.IdentityProvider;
import dev.ghidora.utabridge_server.models.User;
import dev.ghidora.utabridge_server.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private IdentityTokenVerifier identityTokenVerifier;

    @Mock private UserRepository userRepository;

    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    @Test
    void createToken_ExistingUser_ReturnsJwt() throws Exception {
        // Arrange
        String mockToken = "google-token";
        String email = "test@example.com";
        // VerifiedUser(String name, String email, String pictureUrl, String providerId)
        var mockIdentity = new IdentityTokenVerifier.VerifiedUser("Test User", email, "url", "123");

        when(identityTokenVerifier.verifyToken(mockToken)).thenReturn(mockIdentity);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken("1")).thenReturn("signed-jwt");

        // Act
        String result = authService.createToken(mockToken);

        // Assert
        assertEquals("signed-jwt", result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createToken_NewUser_CreatesUserAndReturnsJwt() throws Exception {
        // Arrange
        String mockToken = "google-token";
        String email = "new@example.com";
        var mockIdentity = new IdentityTokenVerifier.VerifiedUser("New User", email, "url", "456");

        when(identityTokenVerifier.verifyToken(mockToken)).thenReturn(mockIdentity);
        when(identityTokenVerifier.getProvider()).thenReturn(IdentityProvider.GOOGLE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(email);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("2")).thenReturn("signed-jwt");

        // Act
        String result = authService.createToken(mockToken);

        // Assert
        assertEquals("signed-jwt", result);
        verify(userRepository).save(any(User.class));
    }
}
