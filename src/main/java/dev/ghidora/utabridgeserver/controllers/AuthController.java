package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.services.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for authentication endpoints. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Request payload for authentication.
   *
   * @param token The identity token.
   * @param provider The identity provider.
   */
  public record AuthRequest(@NotBlank String token, @NotBlank IdentityProvider provider) {}

  /**
   * Response payload for authentication.
   *
   * @param token The generated JWT.
   */
  public record AuthResponse(String token) {}

  /**
   * Handles authentication requests.
   *
   * @param payload The authentication request payload.
   * @return The response entity containing the JWT or an error.
   */
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
