package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.dtos.Credentials;
import dev.ghidora.utabridgeserver.exceptions.InvalidTokenException;
import dev.ghidora.utabridgeserver.exceptions.RefreshTokenException;
import dev.ghidora.utabridgeserver.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for authentication endpoints. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {
  private final AuthService authService;
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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
   * @throws InvalidTokenException if the identity token is invalid
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Authenticate user",
      description =
          "Exchanges a third-party identity token (e.g., Google ID token) for JWT credentials."
              + " If the user does not exist, a new account is created.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful authentication",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Success",
                            value =
                                "{\"authToken\": \"eyJhbGciOiJIUzI1NiIs...\", \"refreshToken\":"
                                    + " \"abc123...\", \"user\": {\"name\": \"John Doe\","
                                    + " \"pictureUrl\": \"https://example.com/avatar.jpg\"}}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired identity token",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Invalid Token",
                            value =
                                "{\"error\": \"INVALID_TOKEN\", \"message\": \"Invalid or expired"
                                    + " identity token\", \"status\": 401}")))
      })
  @PostMapping("/login")
  public ResponseEntity<?> handleLogin(@RequestBody LoginRequest payload)
      throws InvalidTokenException, IOException {
    logger.info("Login attempt received");
    try {
      var loginResponse = authService.login(payload.token());
      logger.info("Login successful for user: {}", loginResponse.user());
      return ResponseEntity.ok().body(loginResponse);
    } catch (GeneralSecurityException e) {
      throw new InvalidTokenException("Invalid or expired identity token");
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
   * @throws RefreshTokenException if the refresh token is invalid
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Refresh JWT token",
      description =
          "Exchanges a valid refresh token for new JWT credentials. "
              + "The old refresh token is revoked and a new one is issued.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful token refresh",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Success",
                            value =
                                "{\"authToken\": \"eyJhbGciOiJIUzI1NiIs...\", \"refreshToken\":"
                                    + " \"newrefresh123...\"}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or revoked refresh token",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Invalid Refresh Token",
                            value =
                                "{\"error\": \"INVALID_REFRESH_TOKEN\", \"message\": \"Invalid or"
                                    + " revoked refresh token\", \"status\": 401}")))
      })
  @PostMapping("/refresh")
  public ResponseEntity<Credentials> handleRefreshToken(@RequestBody RefreshRequest payload)
      throws RefreshTokenException, IOException {
    logger.info("Token refresh attempt received");
    try {
      var token = authService.refreshCredentials(payload.token());
      logger.info("Token refresh successful");
      return ResponseEntity.ok().body(token);
    } catch (GeneralSecurityException e) {
      throw new RefreshTokenException("Invalid or revoked refresh token");
    }
  }
}
