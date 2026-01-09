package dev.ghidora.utabridge_server.controllers;

import dev.ghidora.utabridge_server.enums.IdentityProvider;
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

    @Autowired
    public AuthController(IdentityTokenVerifier identityTokenVerifier) {
        this.identityTokenVerifier = identityTokenVerifier;
    }

    public record AuthRequest(
            @NotBlank String token,
            @NotBlank IdentityProvider provider
    ) {}

    public record AuthResponse(String token) {}

    @PostMapping()
    public ResponseEntity<AuthResponse> handleAuth(@RequestBody AuthRequest payload) {
        // TODO: respect provider when adding support for discord login
        try {
            var verifiedUser = identityTokenVerifier.verifyToken(payload.token());
            return ResponseEntity.ok().body(new AuthResponse(payload.token()));
        } catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
