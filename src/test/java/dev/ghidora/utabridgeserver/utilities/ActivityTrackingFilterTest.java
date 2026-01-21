package dev.ghidora.utabridgeserver.utilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.ghidora.utabridgeserver.enums.IdentityProvider;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityTrackingFilterTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private ActivityTrackingFilter activityTrackingFilter;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");
    testUser.setProviderId("12345");
    testUser.setProvider(IdentityProvider.GOOGLE);
    testUser.setLastActiveAt(null);
  }

  @Test
  void shouldNotFilter_PublicPaths() {
    assertTrue(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/auth/login")));
    assertTrue(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/auth/refresh")));
    assertTrue(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/dev/test")));
    assertTrue(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/docs.html")));
    assertTrue(activityTrackingFilter.shouldNotFilter(createMockRequest("/v3/api-docs")));
    assertTrue(
        activityTrackingFilter.shouldNotFilter(createMockRequest("/api/swagger-ui/index.html")));
  }

  @Test
  void shouldNotFilter_ProtectedPaths() {
    assertFalse(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/localize")));
    assertFalse(activityTrackingFilter.shouldNotFilter(createMockRequest("/api/user")));
  }

  @Test
  void updateLastActiveIfNeeded_FirstUpdate_WithNullLastActive() {
    testUser.setLastActiveAt(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    doNothing().when(userRepository).updateLastActiveAt(eq(1L), any(Instant.class));

    activityTrackingFilter.updateLastActiveIfNeeded(1L);

    verify(userRepository).findById(1L);
    verify(userRepository).updateLastActiveAt(eq(1L), any(Instant.class));
  }

  @Test
  void updateLastActiveIfNeeded_StaleData_Updates() {
    testUser.setLastActiveAt(Instant.now().minusSeconds(400));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    doNothing().when(userRepository).updateLastActiveAt(eq(1L), any(Instant.class));

    activityTrackingFilter.updateLastActiveIfNeeded(1L);

    verify(userRepository).findById(1L);
    verify(userRepository).updateLastActiveAt(eq(1L), any(Instant.class));
  }

  @Test
  void updateLastActiveIfNeeded_RecentData_SkipsUpdate() {
    testUser.setLastActiveAt(Instant.now().minusSeconds(60));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    activityTrackingFilter.updateLastActiveIfNeeded(1L);

    verify(userRepository).findById(1L);
    verify(userRepository, never()).updateLastActiveAt(any(), any());
  }

  @Test
  void updateLastActiveIfNeeded_UserNotFound_DoesNotThrow() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> activityTrackingFilter.updateLastActiveIfNeeded(999L));

    verify(userRepository).findById(999L);
    verify(userRepository, never()).updateLastActiveAt(any(), any());
  }

  private jakarta.servlet.http.HttpServletRequest createMockRequest(String path) {
    jakarta.servlet.http.HttpServletRequest request =
        mock(jakarta.servlet.http.HttpServletRequest.class);
    when(request.getServletPath()).thenReturn(path);
    return request;
  }
}
