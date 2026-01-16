package dev.ghidora.utabridgeserver.models;

import static org.junit.jupiter.api.Assertions.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
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
  void constructor_InitializesFields() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getName());
    assertNull(user.getPictureUrl());
    assertNull(user.getProviderId());
    assertNull(user.getProvider());
  }

  @Test
  void allSetters_UpdateValues() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setPictureUrl("https://example.com/avatar.jpg");
    user.setProviderId("12345");
    user.setProvider(IdentityProvider.GOOGLE);

    assertEquals("test@example.com", user.getEmail());
    assertEquals("Test User", user.getName());
    assertEquals("https://example.com/avatar.jpg", user.getPictureUrl());
    assertEquals("12345", user.getProviderId());
    assertEquals(IdentityProvider.GOOGLE, user.getProvider());
  }
}
