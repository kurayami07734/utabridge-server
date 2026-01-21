package dev.ghidora.utabridgeserver.utilities;

import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ActivityTrackingFilter extends OncePerRequestFilter {
  private static final Duration THROTTLE_DURATION = Duration.ofMinutes(5);
  private static final String[] PUBLIC_PATHS = {
    "/api/auth/login",
    "/api/auth/refresh",
    "/api/health",
    "/api/dev",
    "/api/docs.html",
    "/v3/api-docs",
    "/api/swagger-ui"
  };

  private final UserRepository userRepository;

  public ActivityTrackingFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    filterChain.doFilter(request, response);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return;
    }

    Object principal = auth.getPrincipal();
    if (principal == null || "anonymousUser".equals(principal)) {
      return;
    }

    Long userId = Long.parseLong(principal.toString());
    updateLastActiveIfNeeded(userId);
  }

  void updateLastActiveIfNeeded(Long userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      return;
    }

    Instant lastActive = user.get().getLastActiveAt();
    if (lastActive == null
        || Duration.between(lastActive, Instant.now()).compareTo(THROTTLE_DURATION) >= 0) {
      userRepository.updateLastActiveAt(userId, Instant.now());
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    for (String publicPath : PUBLIC_PATHS) {
      if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
        return true;
      }
    }
    return false;
  }
}
