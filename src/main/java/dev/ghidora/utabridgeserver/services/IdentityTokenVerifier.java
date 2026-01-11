package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** Interface for verifying identity tokens. */
public interface IdentityTokenVerifier {
  /** Verified user details. */
  public record VerifiedUser(String name, String email, String pictureUrl, String providerId) {}

  public IdentityProvider getProvider();

  /**
   * Verifies the token and transforms it into a standard VerifiedUser.
   *
   * @param token The raw token string from the frontend.
   * @return VerifiedUser The standardized user details.
   * @throws GeneralSecurityException If signature verification fails.
   * @throws IOException If network requests fail.
   */
  public VerifiedUser verifyToken(String token) throws IOException, GeneralSecurityException;
}
