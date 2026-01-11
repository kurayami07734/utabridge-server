package dev.ghidora.utabridge_server.controllers;

import dev.ghidora.utabridge_server.enums.IdentityProvider;
import dev.ghidora.utabridge_server.repositories.UserRepository;
import dev.ghidora.utabridge_server.services.AuthService;
import dev.ghidora.utabridge_server.services.IdentityTokenVerifier;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final IdentityTokenVerifier identityTokenVerifier;
    private final AuthService authService;

    @Autowired
    public AuthController(IdentityTokenVerifier identityTokenVerifier, AuthService authService) {
        this.identityTokenVerifier = identityTokenVerifier;
        this.authService = authService;
    }

    public record AuthRequest(
            @NotBlank String token,
            @NotBlank IdentityProvider provider
    ) {
    }

    public record AuthResponse(String token) {
    }

    @PostMapping()
    public ResponseEntity<AuthResponse> handleAuth(@RequestBody AuthRequest payload) {
        // TODO: respect provider when adding support for discord login
        try {
            var verifiedUser = identityTokenVerifier.verifyToken(payload.token());

            var user = authService.getOrCreateUser(
                    verifiedUser.email(),
                    verifiedUser.name(),
                    verifiedUser.pictureUrl(),
                    verifiedUser.providerId()
            );

            return ResponseEntity.ok().body(new AuthResponse(payload.token()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
