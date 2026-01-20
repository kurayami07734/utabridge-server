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
    var token = RefreshTokenGenerator.generate();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setHashedToken(Sha256HashGenerator.hashString(token));
    refreshToken.setUser(user);
    refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
    refreshTokenRepository.save(refreshToken);
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
    var verifiedUser = identityTokenVerifier.verifyToken(token);

    User user =
        userService.getOrCreateUser(
            verifiedUser.email(),
            verifiedUser.name(),
            verifiedUser.pictureUrl(),
            verifiedUser.providerId(),
            identityTokenVerifier.getProvider());

    var authToken = jwtService.generateToken(user.getId().toString());
    var refreshToken = getRefreshToken(user);
    var userDto = new UserDto(user.getName(), user.getPictureUrl());

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
    String hashedToken = Sha256HashGenerator.hashString(token);

    Optional<RefreshToken> refreshToken = refreshTokenRepository.findByHashedToken(hashedToken);

    if (refreshToken.isEmpty() || refreshToken.get().isRevoked()) {
      throw new GeneralSecurityException("Invalid refresh token!");
    }

    refreshToken.get().setRevoked(true);
    refreshTokenRepository.save(refreshToken.get());

    var authToken = jwtService.generateToken(refreshToken.get().getUser().getId().toString());
    var newRefreshToken = getRefreshToken(refreshToken.get().getUser());

    return new Credentials(authToken, newRefreshToken);
  }
}
