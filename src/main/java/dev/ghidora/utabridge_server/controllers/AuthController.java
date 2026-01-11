package dev.ghidora.utabridge_server.controllers;

import dev.ghidora.utabridge_server.enums.IdentityProvider;
import dev.ghidora.utabridge_server.services.AuthService;
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
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record AuthRequest(@NotBlank String token, @NotBlank IdentityProvider provider) {}

    public record AuthResponse(String token) {}

    @PostMapping()
    public ResponseEntity<AuthResponse> handleAuth(@RequestBody AuthRequest payload) {
        try {
            var token = authService.createToken(payload.token());
            return ResponseEntity.ok().body(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
