package dev.ghidora.utabridgeserver.repositories;

import dev.ghidora.utabridgeserver.models.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for User entity. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  @Modifying
  @Query("UPDATE User u SET u.lastActiveAt = :time WHERE u.id = :id")
  void updateLastActiveAt(@Param("id") Long id, @Param("time") Instant time);
}
