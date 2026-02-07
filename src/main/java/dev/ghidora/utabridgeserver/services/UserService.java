package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import dev.ghidora.utabridgeserver.exceptions.ResourceNotFoundException;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing user data. */
@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  @PersistenceContext private EntityManager entityManager;
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
    logger.debug("Attempting to get or create user");

    return userRepository
        .findByEmail(email)
        .orElseGet(() -> createUser(email, name, pictureUrl, providerId, provider));
  }

  private User createUser(
      String email, String name, String pictureUrl, String providerId, IdentityProvider provider) {
    logger.debug("Creating new user");
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    user.setPictureUrl(pictureUrl);
    user.setProviderId(providerId);
    user.setProvider(provider);
    user.setLastActiveAt(Instant.now());
    user.initializeDefaultPreferences();

    try {
      User newUser = userRepository.saveAndFlush(user);
      logger.info("Successfully created new user with ID: {}", newUser.getId());
      return newUser;
    } catch (DataIntegrityViolationException ex) {
      return findUserByEmail(email);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  private User findUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "User not found after constraint violation for email: " + email));
  }

  /**
   * Updates a user's preferences.
   *
   * @param userId The ID of the user to update.
   * @param preferences A map of preferences to update.
   * @return The updated user.
   * @throws IllegalArgumentException if a preference is invalid.
   * @throws ResourceNotFoundException if the user is not found.
   */
  public User updatePreferences(Long userId, Map<String, String> preferences)
      throws IllegalArgumentException, ResourceNotFoundException {
    logger.debug("Attempting to update preferences for user ID: {}", userId);
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (preferences != null) {
      preferences.forEach(
          (key, value) -> {
            logger.debug("Updating preference for user {}: {} = {}", userId, key, value);
            var prefKey = UserPreferenceType.valueOf(key.toUpperCase());
            user.addPreference(prefKey, value);
          });
    }

    User updatedUser = userRepository.save(user);
    logger.info("Successfully updated preferences for user ID: {}", userId);

    return updatedUser;
  }
}
