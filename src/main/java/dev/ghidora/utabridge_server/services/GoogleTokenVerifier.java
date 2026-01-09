package dev.ghidora.utabridge_server.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.ghidora.utabridge_server.enums.IdentityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Primary
public class GoogleTokenVerifier implements IdentityTokenVerifier {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${gcp.client-id}") String clientId) throws GeneralSecurityException, IOException {
        verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }


    @Override
    public IdentityProvider getProvider() {
        return IdentityProvider.GOOGLE;
    }

    @Override
    public VerifiedUser verifyToken(String token) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(token);

        if (token == null) {
            throw new GeneralSecurityException("Invalid google token!");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        return new VerifiedUser(
                (String) payload.get("given_name"),
                payload.getEmail(),
                (String) payload.get("picture"),
                payload.getSubject()
        );
    }

}
