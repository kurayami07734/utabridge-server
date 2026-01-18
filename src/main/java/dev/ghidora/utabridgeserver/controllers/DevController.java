package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.dtos.Credentials;
import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.models.RefreshToken;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import dev.ghidora.utabridgeserver.services.UserService;
import dev.ghidora.utabridgeserver.utilities.JwtService;
import dev.ghidora.utabridgeserver.utilities.RefreshTokenGenerator;
import dev.ghidora.utabridgeserver.utilities.Sha256HashGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@Tag(name = "Development", description = "Development-only endpoints for testing")
public class DevController {
  private final UserService userService;
  private final JwtService jwtService;
  private final RefreshTokenRepository refreshTokenRepository;

  public DevController(
      UserService userService,
      JwtService jwtService,
      RefreshTokenRepository refreshTokenRepository) {
    this.userService = userService;
    this.jwtService = jwtService;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public record DevTokenRequest(String email, String name) {}

  @Operation(summary = "Generate test tokens (development only)")
  @PostMapping("/token")
  public Credentials generateToken(@RequestBody DevTokenRequest request) {
    User user =
        userService.getOrCreateUser(
            request.email(), request.name(), null, "dev-test-provider", IdentityProvider.GOOGLE);

    String authToken = jwtService.generateToken(user.getId().toString());
    String refreshTokenValue = RefreshTokenGenerator.generate();
    String hashedToken = Sha256HashGenerator.hashString(refreshTokenValue);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setHashedToken(hashedToken);
    refreshToken.setExpiresAt(Instant.now().plusSeconds(86400));
    refreshTokenRepository.save(refreshToken);

    return new Credentials(authToken, refreshTokenValue);
  }
}
