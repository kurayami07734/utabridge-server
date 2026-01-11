package dev.ghidora.utabridge_server.services;

import dev.ghidora.utabridge_server.enums.IdentityProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface IdentityTokenVerifier {
    public record VerifiedUser(String name, String email, String pictureUrl, String providerId) {}

    public IdentityProvider getProvider();

    /**
     * Verifies the token and transforms it into a standard VerifiedUser.
     *
     * @param token The raw token string from the frontend.
     * @throws GeneralSecurityException If signature verification fails.
     * @throws IOException If network requests fail.
     * @return VerifiedUser The standardized user details.
     */
    public VerifiedUser verifyToken(String token) throws IOException, GeneralSecurityException;
}
