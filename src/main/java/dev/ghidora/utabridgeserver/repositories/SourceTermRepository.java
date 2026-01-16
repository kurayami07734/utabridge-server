package dev.ghidora.utabridgeserver.repositories;

import dev.ghidora.utabridgeserver.models.SourceTerm;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for SourceTerm entity. */
@Repository
public interface SourceTermRepository extends JpaRepository<SourceTerm, Long> {
  @EntityGraph(attributePaths = {"translationMap"})
  Optional<SourceTerm> findByOriginalText(String originalText);
}
