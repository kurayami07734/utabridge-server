package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Service responsible for cleaning up expired or revoked refresh tokens. */
@Service
@Transactional
public class RefreshTokenCleanupService {
  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /** Run cleanup every day at midnight. */
  @Scheduled(cron = "0 0 0 * * *")
  void removeExpiredRefreshTokens() {
    Instant now = Instant.now();
    refreshTokenRepository.deleteByExpiresAtBefore(now);
    refreshTokenRepository.deleteByIsRevokedTrue();
  }
}
