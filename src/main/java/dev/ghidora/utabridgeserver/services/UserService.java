package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing user data. */
@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Fetch a user or create one if not present in the database.
   *
   * @param email email
   * @param name name
   * @param pictureUrl pictureUrl
   * @param providerId providerId
   * @param provider identity provider
   * @return User
   */
  public User getOrCreateUser(
      String email, String name, String pictureUrl, String providerId, IdentityProvider provider) {
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () -> {
              User user = new User();
              user.setEmail(email);
              user.setName(name);
              user.setPictureUrl(pictureUrl);
              user.setProviderId(providerId);
              user.setProvider(provider);
              user.setLastActiveAt(Instant.now());
              return userRepository.save(user);
            });
  }
}
