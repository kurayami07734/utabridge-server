package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.dtos.Credentials;
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
   */
  public record LoginRequest(@NotBlank String token) {}

  /**
   * Handles login requests.
   *
   * @param payload The authentication request payload.
   * @return The response entity containing the JWT or an error.
   */
  @PostMapping("/login")
  public ResponseEntity<Credentials> handleLogin(@RequestBody LoginRequest payload) {
    try {
      var credentials = authService.getLoginCredentials(payload.token());
      return ResponseEntity.ok().body(credentials);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Request payload for refreshing tokens.
   *
   * @param token The refresh token.
   */
  public record RefreshRequest(@NotBlank String token) {}

  /**
   * Handles refresh token requests.
   *
   * @param payload The refresh token request payload.
   * @return The response entity containing the JWT or an error.
   */
  @PostMapping("/refresh")
  public ResponseEntity<Credentials> handleRefreshToken(@RequestBody RefreshRequest payload) {
    try {
      var token = authService.refreshCredentials(payload.token());
      return ResponseEntity.ok().body(token);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
