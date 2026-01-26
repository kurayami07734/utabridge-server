package dev.ghidora.utabridgeserver.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.dtos.Credentials;
import dev.ghidora.utabridgeserver.dtos.LoginResponse;
import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.models.RefreshToken;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import dev.ghidora.utabridgeserver.utilities.JwtService;
import dev.ghidora.utabridgeserver.utilities.Sha256HashGenerator;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private IdentityTokenVerifier identityTokenVerifier;
  @Mock private UserService userService;
  @Mock private JwtService jwtService;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthService authService;

  @Test
  void login_ValidToken_ReturnsLoginResponse() throws Exception {
    // Arrange
    String mockToken = "google-token";
    String email = "test@example.com";
    String name = "Test User";
    Long id = 1L;
    String pictureUrl = "https://example.com/avatar.jpg";
    var mockIdentity = new IdentityTokenVerifier.VerifiedUser(name, email, pictureUrl, "123");

    when(identityTokenVerifier.verifyToken(mockToken)).thenReturn(mockIdentity);
    when(identityTokenVerifier.getProvider()).thenReturn(IdentityProvider.GOOGLE);

    User user = new User();
    ReflectionTestUtils.setField(user, "id", id);
    ReflectionTestUtils.setField(user, "name", name);
    ReflectionTestUtils.setField(user, "pictureUrl", pictureUrl);

    when(userService.getOrCreateUser(
            eq(email), eq(name), eq(pictureUrl), eq("123"), eq(IdentityProvider.GOOGLE)))
        .thenReturn(user);

    when(jwtService.generateToken("1")).thenReturn("signed-jwt");

    // Act
    LoginResponse result = authService.login(mockToken);

    // Assert
    assertThat(result.authToken()).isEqualTo("signed-jwt");
    assertThat(result.refreshToken()).isNotEmpty();
    assertThat(result.user()).isNotNull();
    assertThat(result.user().id()).isEqualTo(id);
    assertThat(result.user().name()).isEqualTo(name);
    assertThat(result.user().pictureUrl()).isEqualTo(pictureUrl);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void refreshCredentials_ValidToken_ReturnsNewCredentials() throws Exception {
    // Arrange
    String rawToken = "raw-refresh-token";
    String hashedToken = Sha256HashGenerator.hashString(rawToken);

    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setHashedToken(hashedToken);
    refreshToken.setUser(user);
    refreshToken.setRevoked(false);

    when(refreshTokenRepository.findByHashedToken(hashedToken))
        .thenReturn(Optional.of(refreshToken));
    when(jwtService.generateToken("1")).thenReturn("new-jwt");

    // Act
    Credentials result = authService.refreshCredentials(rawToken);

    // Assert
    assertThat(result.authToken()).isEqualTo("new-jwt");
    assertThat(result.refreshToken()).isNotEmpty();

    // Verify old token revoked
    assertThat(refreshToken.isRevoked()).isTrue();
    verify(refreshTokenRepository).save(refreshToken);

    // Verify new token saved (one for update, one for new)
    verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
  }

  @Test
  void refreshCredentials_InvalidToken_ThrowsException() {
    String rawToken = "invalid-token";
    String hashedToken = Sha256HashGenerator.hashString(rawToken);

    when(refreshTokenRepository.findByHashedToken(hashedToken)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refreshCredentials(rawToken))
        .isInstanceOf(GeneralSecurityException.class)
        .hasMessage("Invalid refresh token!");
  }

  @Test
  void refreshCredentials_RevokedToken_ThrowsException() {
    String rawToken = "revoked-token";
    String hashedToken = Sha256HashGenerator.hashString(rawToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRevoked(true);

    when(refreshTokenRepository.findByHashedToken(hashedToken))
        .thenReturn(Optional.of(refreshToken));

    assertThatThrownBy(() -> authService.refreshCredentials(rawToken))
        .isInstanceOf(GeneralSecurityException.class)
        .hasMessage("Invalid refresh token!");
  }
}
