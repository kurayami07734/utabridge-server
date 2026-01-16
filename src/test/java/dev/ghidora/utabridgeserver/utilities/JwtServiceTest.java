package dev.ghidora.utabridgeserver.utilities;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  private static final String SECRET_KEY =
      "c2VjcmV0S2V5Rm9ySnNvbldlYnRva2VuU2lnbmluZ0FuZEV4cGlyYXRpb25TaG91bGRCZUF0TGVhc3QyNTZCaXRz";
  private static final long VALIDITY_DURATION_MS = 3600000L;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET_KEY, VALIDITY_DURATION_MS);
  }

  @Test
  void generateToken_CreatesValidToken() {
    String subject = "user123";
    String token = jwtService.generateToken(subject);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  void extractClaim_ExtractsSubject() {
    String subject = "user456";
    String token = jwtService.generateToken(subject);

    String extractedSubject = jwtService.extractClaim(token, Claims::getSubject);

    assertEquals(subject, extractedSubject);
  }

  @Test
  void extractClaim_ExtractsExpiration() {
    String subject = "user789";
    String token = jwtService.generateToken(subject);

    Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void extractClaim_ExtractsIssuedAt() {
    String subject = "user101";
    String token = jwtService.generateToken(subject);

    Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

    assertNotNull(issuedAt);
    assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
  }

  @Test
  void isValidToken_ValidToken_ReturnsTrue() {
    String subject = "valid-user";
    String token = jwtService.generateToken(subject);

    boolean isValid = jwtService.isValidToken(token, subject);

    assertTrue(isValid);
  }

  @Test
  void isValidToken_WrongSubject_ReturnsFalse() {
    String originalSubject = "original-user";
    String wrongSubject = "wrong-user";
    String token = jwtService.generateToken(originalSubject);

    boolean isValid = jwtService.isValidToken(token, wrongSubject);

    assertFalse(isValid);
  }

  @Test
  void generateToken_DifferentSubjects_ProduceDifferentTokens() {
    String token1 = jwtService.generateToken("user1");
    String token2 = jwtService.generateToken("user2");

    assertNotEquals(token1, token2);
  }

  @Test
  void isValidToken_TokenForSameSubject_ReturnsTrue() {
    String subject = "test-subject";
    String token = jwtService.generateToken(subject);

    assertTrue(jwtService.isValidToken(token, subject));
  }

  @Test
  void isValidToken_EmptySubject_ReturnsFalseForNonEmptyToken() {
    String token = jwtService.generateToken("some-user");

    assertFalse(jwtService.isValidToken(token, ""));
  }

  @Test
  void extractClaim_InvalidToken_ThrowsException() {
    String invalidToken = "invalid.token.here";

    assertThrows(Exception.class, () -> jwtService.extractClaim(invalidToken, Claims::getSubject));
  }
}
