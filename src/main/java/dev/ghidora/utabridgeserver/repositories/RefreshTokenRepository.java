package dev.ghidora.utabridgeserver.repositories;

import dev.ghidora.utabridgeserver.models.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing {@link RefreshToken} entities. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByHashedToken(String hashedToken);

  void deleteByExpiresAtBefore(Instant now);

  void deleteByIsRevokedTrue();
}
