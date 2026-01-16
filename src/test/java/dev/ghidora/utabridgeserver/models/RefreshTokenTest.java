package dev.ghidora.utabridgeserver.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  @Test
  void setHashedToken_UpdatesValue() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setHashedToken("hashed-token-value");

    assertEquals("hashed-token-value", refreshToken.getHashedToken());
  }

  @Test
  void setUser_BidirectionalRelationship() {
    RefreshToken refreshToken = new RefreshToken();
    User user = new User();
    user.setEmail("test@example.com");

    refreshToken.setUser(user);

    assertEquals(user, refreshToken.getUser());
  }

  @Test
  void getUser_ReturnsCorrectUser() {
    RefreshToken refreshToken = new RefreshToken();
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");

    refreshToken.setUser(user);

    assertEquals("test@example.com", refreshToken.getUser().getEmail());
    assertEquals("Test User", refreshToken.getUser().getName());
  }

  @Test
  void setRevoked_UpdatesValue() {
    RefreshToken refreshToken = new RefreshToken();

    assertFalse(refreshToken.isRevoked());

    refreshToken.setRevoked(true);

    assertTrue(refreshToken.isRevoked());
  }

  @Test
  void setExpiresAt_UpdatesValue() {
    RefreshToken refreshToken = new RefreshToken();
    Instant expiresAt = Instant.now().plusSeconds(3600);

    refreshToken.setExpiresAt(expiresAt);

    assertEquals(expiresAt, refreshToken.getExpiresAt());
  }

  @Test
  void setExpiresAt_FutureDate_IsAfterNow() {
    RefreshToken refreshToken = new RefreshToken();
    Instant futureDate = Instant.now().plusSeconds(86400);

    refreshToken.setExpiresAt(futureDate);

    assertTrue(refreshToken.getExpiresAt().isAfter(Instant.now()));
  }

  @Test
  void constructor_InitializesFields() {
    RefreshToken refreshToken = new RefreshToken();

    assertNull(refreshToken.getId());
    assertNull(refreshToken.getHashedToken());
    assertNull(refreshToken.getUser());
    assertFalse(refreshToken.isRevoked());
    assertNull(refreshToken.getExpiresAt());
  }

  @Test
  void allSetters_UpdateValues() {
    RefreshToken refreshToken = new RefreshToken();
    User user = new User();
    Instant expiresAt = Instant.now().plusSeconds(7200);

    refreshToken.setHashedToken("new-hashed-token");
    refreshToken.setUser(user);
    refreshToken.setRevoked(true);
    refreshToken.setExpiresAt(expiresAt);

    assertEquals("new-hashed-token", refreshToken.getHashedToken());
    assertEquals(user, refreshToken.getUser());
    assertTrue(refreshToken.isRevoked());
    assertEquals(expiresAt, refreshToken.getExpiresAt());
  }
}
