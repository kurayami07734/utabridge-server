package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.dtos.Credentials;
import dev.ghidora.utabridgeserver.dtos.LoginResponse;
import dev.ghidora.utabridgeserver.dtos.UserDto;
import dev.ghidora.utabridgeserver.models.RefreshToken;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import dev.ghidora.utabridgeserver.utilities.JwtService;
import dev.ghidora.utabridgeserver.utilities.RefreshTokenGenerator;
import dev.ghidora.utabridgeserver.utilities.Sha256HashGenerator;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling authentication logic. */
@Service
@Transactional
public class AuthService {
  private final IdentityTokenVerifier identityTokenVerifier;
  private final JwtService jwtService;
  private final UserService userService;
  private final RefreshTokenRepository refreshTokenRepository;
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  /**
   * Constructs an AuthService.
   *
   * @param identityTokenVerifier Verifier for identity tokens.
   * @param userService Service for user management.
   * @param jwtService Service for JWT operations.
   * @param refreshTokenRepository Repository for refresh tokens.
   */
  public AuthService(
      IdentityTokenVerifier identityTokenVerifier,
      JwtService jwtService,
      UserService userService,
      RefreshTokenRepository refreshTokenRepository) {
    this.identityTokenVerifier = identityTokenVerifier;
    this.jwtService = jwtService;
    this.userService = userService;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  private String getRefreshToken(User user) {
    logger.debug("Generating new refresh token for user ID: {}", user.getId());
    var token = RefreshTokenGenerator.generate();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setHashedToken(Sha256HashGenerator.hashString(token));
    refreshToken.setUser(user);
    refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
    refreshTokenRepository.save(refreshToken);
    logger.debug("Successfully saved new refresh token for user ID: {}", user.getId());
    return token;
  }

  /**
   * Authenticates user and returns login response. NOTE: This method will create a new user if not
   * already present.
   *
   * @param token third party token
   * @return LoginResponse containing tokens and user details
   * @throws GeneralSecurityException if security error occurs
   * @throws IOException if io error occurs
   */
  public LoginResponse login(String token) throws GeneralSecurityException, IOException {
    logger.debug("Attempting to log in user with a third-party token.");
    var verifiedUser = identityTokenVerifier.verifyToken(token);
    logger.debug("Token verified with provider: {}", identityTokenVerifier.getProvider());

    User user =
        userService.getOrCreateUser(
            verifiedUser.email(),
            verifiedUser.name(),
            verifiedUser.pictureUrl(),
            verifiedUser.providerId(),
            identityTokenVerifier.getProvider());

    var authToken = jwtService.generateToken(user.getId().toString());
    var refreshToken = getRefreshToken(user);
    var userDto = new UserDto(user.getId(), user.getName(), user.getPictureUrl());

    logger.info("Login successful for user: {}", user.getId());
    return new LoginResponse(authToken, refreshToken, userDto);
  }

  /**
   * Refreshes the authentication credentials using a valid refresh token.
   *
   * @param token The raw refresh token.
   * @return A new set of credentials (JWT + new refresh token).
   * @throws GeneralSecurityException If the token is invalid or revoked.
   * @throws IOException If an IO error occurs.
   */
  public Credentials refreshCredentials(String token) throws GeneralSecurityException, IOException {
    logger.debug("Attempting to refresh credentials.");
    String hashedToken = Sha256HashGenerator.hashString(token);

    Optional<RefreshToken> refreshToken = refreshTokenRepository.findByHashedToken(hashedToken);

    if (refreshToken.isEmpty() || refreshToken.get().isRevoked()) {
      logger.warn("Attempt to refresh credentials with invalid or revoked token.");
      throw new GeneralSecurityException("Invalid refresh token!");
    }

    User user = refreshToken.get().getUser();
    logger.debug("Found valid refresh token for user ID: {}", user.getId());

    refreshToken.get().setRevoked(true);
    refreshTokenRepository.save(refreshToken.get());
    logger.debug("Revoked old refresh token for user ID: {}", user.getId());

    var authToken = jwtService.generateToken(user.getId().toString());
    var newRefreshToken = getRefreshToken(user);

    logger.info("Successfully refreshed credentials for user ID: {}", user.getId());
    return new Credentials(authToken, newRefreshToken);
  }
}
