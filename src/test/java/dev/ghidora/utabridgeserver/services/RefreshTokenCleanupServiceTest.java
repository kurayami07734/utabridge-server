package dev.ghidora.utabridgeserver.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import dev.ghidora.utabridgeserver.repositories.RefreshTokenRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokenCleanupService cleanupService;

  @Test
  void removeExpiredRefreshTokens_shouldDeleteExpiredAndRevokedTokens() {
    // Act
    cleanupService.removeExpiredRefreshTokens();

    // Assert
    verify(refreshTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    verify(refreshTokenRepository).deleteByIsRevokedTrue();
  }
}
