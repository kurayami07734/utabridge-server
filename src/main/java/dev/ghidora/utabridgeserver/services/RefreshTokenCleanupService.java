package dev.ghidora.utabridgeserver.services;

import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Service responsible for cleaning up expired or revoked refresh tokens. */
@Service
@Transactional
public class RefreshTokenCleanupService {
  private final RefreshTokenRepository refreshTokenRepository;
  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

  public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /** Run cleanup every day at midnight. */
  @Scheduled(cron = "0 0 0 * * *")
  void removeExpiredRefreshTokens() {
    logger.info("Starting expired refresh token cleanup task.");
    Instant now = Instant.now();
    long expiredCount = refreshTokenRepository.deleteByExpiresAtBefore(now);
    logger.info("Removed {} expired refresh tokens.", expiredCount);
    long revokedCount = refreshTokenRepository.deleteByIsRevokedTrue();
    logger.info("Removed {} explicitly revoked refresh tokens.", revokedCount);
    logger.info("Finished expired refresh token cleanup task.");
  }
}
