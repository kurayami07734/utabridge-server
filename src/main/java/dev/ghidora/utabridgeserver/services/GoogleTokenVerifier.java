package dev.ghidora.utabridgeserver.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** IdentityTokenVerifier implementation for Google. */
@Service
@Primary
public class GoogleTokenVerifier implements IdentityTokenVerifier {
  private final GoogleIdTokenVerifier verifier;

  /**
   * Constructs a GoogleTokenVerifier.
   *
   * @param clientId The Google Client ID.
   * @throws GeneralSecurityException If security setup fails.
   * @throws IOException If IO setup fails.
   */
  public GoogleTokenVerifier(@Value("${gcp.client-id}") String clientId)
      throws GeneralSecurityException, IOException {
    verifier =
        new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(clientId))
            .build();
  }

  @Override
  public IdentityProvider getProvider() {
    return IdentityProvider.GOOGLE;
  }

  @Override
  public VerifiedUser verifyToken(String token) throws GeneralSecurityException, IOException {
    if (token == null) {
      throw new GeneralSecurityException("Token cannot be null");
    }

    GoogleIdToken idToken = verifier.verify(token);

    if (idToken == null) {
      throw new GeneralSecurityException("Invalid google token!");
    }

    GoogleIdToken.Payload payload = idToken.getPayload();

    return new VerifiedUser(
        (String) payload.get("name"),
        payload.getEmail(),
        (String) payload.get("picture"),
        payload.getSubject());
  }
}
