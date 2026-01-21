package dev.ghidora.utabridgeserver.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierTest {

  @Mock private GoogleIdTokenVerifier verifier;

  private GoogleTokenVerifier googleTokenVerifier;

  @BeforeEach
  void setUp() throws Exception {
    googleTokenVerifier = new GoogleTokenVerifier("test-client-id");
    ReflectionTestUtils.setField(googleTokenVerifier, "verifier", verifier);
  }

  @Test
  void getProvider_ReturnsGoogle() {
    assert googleTokenVerifier.getProvider() == IdentityProvider.GOOGLE;
  }

  @Test
  void verifyToken_ValidToken_ReturnsVerifiedUser() throws Exception {
    String token = "valid-google-token";
    String email = "test@example.com";
    String name = "Test User";
    String pictureUrl = "https://example.com/picture.jpg";
    String subjectId = "123456789";

    Payload payload = new Payload();
    payload.setEmail(email);
    payload.set("name", name);
    payload.set("picture", pictureUrl);
    payload.setSubject(subjectId);

    GoogleIdToken idToken = mock(GoogleIdToken.class);
    when(idToken.getPayload()).thenReturn(payload);
    when(verifier.verify(token)).thenReturn(idToken);

    IdentityTokenVerifier.VerifiedUser result = googleTokenVerifier.verifyToken(token);

    assert result.email().equals(email);
    assert result.name().equals(name);
    assert result.pictureUrl().equals(pictureUrl);
    assert result.providerId().equals(subjectId);
  }

  @Test
  void verifyToken_NullToken_ThrowsException() {
    assertThrows(GeneralSecurityException.class, () -> googleTokenVerifier.verifyToken(null));
  }

  @Test
  void verifyToken_InvalidToken_ThrowsException() throws Exception {
    String token = "invalid-token";
    when(verifier.verify(token)).thenReturn(null);

    assertThrows(GeneralSecurityException.class, () -> googleTokenVerifier.verifyToken(token));
  }

  @Test
  void verifyToken_MalformedToken_ThrowsException() throws Exception {
    String token = "malformed-token";
    when(verifier.verify(token)).thenThrow(new GeneralSecurityException("Invalid token format"));

    assertThrows(GeneralSecurityException.class, () -> googleTokenVerifier.verifyToken(token));
  }

  @Test
  void verifyToken_ExpiredToken_ThrowsException() throws Exception {
    String token = "expired-token";
    when(verifier.verify(token)).thenThrow(new GeneralSecurityException("Token expired"));

    assertThrows(GeneralSecurityException.class, () -> googleTokenVerifier.verifyToken(token));
  }
}
