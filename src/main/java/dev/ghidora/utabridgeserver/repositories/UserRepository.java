package dev.ghidora.utabridgeserver.repositories;

import dev.ghidora.utabridgeserver.models.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for User entity. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
