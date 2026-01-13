package dev.ghidora.utabridgeserver.utilities;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

/** Utility for generating secure random refresh tokens. */
@Service
public class RefreshTokenGenerator {
  private static final SecureRandom secureRandom = new SecureRandom();
  private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

  /**
   * Generates a random 32 byte token using secure random.
   *
   * @return String refresh token
   */
  public static String generate() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return base64Encoder.encodeToString(bytes);
  }
}
