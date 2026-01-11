package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.stereotype.Service;

/** Service for handling authentication logic. */
@Service
public class AuthService {
  private final IdentityTokenVerifier identityTokenVerifier;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  /**
   * Constructs an AuthService.
   *
   * @param identityTokenVerifier Verifier for identity tokens.
   * @param userRepository Repository for user data.
   * @param jwtService Service for JWT operations.
   */
  public AuthService(
      IdentityTokenVerifier identityTokenVerifier,
      UserRepository userRepository,
      JwtService jwtService) {
    this.identityTokenVerifier = identityTokenVerifier;
    this.userRepository = userRepository;
    this.jwtService = jwtService;
  }

  /**
   * Fetch a user or create one if not present in the database.
   *
   * @param email email
   * @param name name
   * @param pictureUrl pictureUrl
   * @param providerId providerId
   * @return User
   */
  private User getOrCreateUser(String email, String name, String pictureUrl, String providerId) {
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () -> {
              User user = new User();
              user.setEmail(email);
              user.setName(name);
              user.setPictureUrl(pictureUrl);
              user.setProviderId(providerId);
              user.setProvider(identityTokenVerifier.getProvider());
              return userRepository.save(user);
            });
  }

  /**
   * Creates a JWT for the user NOTE: This method will create a new user if not already present.
   *
   * @param token third party token
   * @return String signed JWT
   * @throws GeneralSecurityException if security error occurs
   * @throws IOException if io error occurs
   */
  public String createToken(String token) throws GeneralSecurityException, IOException {
    var verifiedUser = identityTokenVerifier.verifyToken(token);
    User user =
        getOrCreateUser(
            verifiedUser.email(),
            verifiedUser.name(),
            verifiedUser.pictureUrl(),
            verifiedUser.providerId());
    return jwtService.generateToken(user.getId().toString());
  }
}
