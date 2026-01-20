package dev.ghidora.utabridgeserver.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ghidora.utabridgeserver.dtos.ErrorResponse;
import dev.ghidora.utabridgeserver.enums.ErrorType;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter for rate limiting requests. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final RateLimiterRegistry rateLimiterRegistry;
  private final ObjectMapper objectMapper;

  /**
   * Constructs a RateLimitFilter.
   *
   * @param rateLimiterRegistry Rate limiter registry.
   * @param objectMapper Object mapper for JSON responses.
   */
  public RateLimitFilter(RateLimiterRegistry rateLimiterRegistry, ObjectMapper objectMapper) {
    this.rateLimiterRegistry = rateLimiterRegistry;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    String limiterKey =
        (auth != null && auth.isAuthenticated())
            ? "user_id:" + auth.getName()
            : "ip:" + request.getRemoteAddr();

    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterKey);
    if (rateLimiter.acquirePermission()) {
      doFilter(request, response, filterChain);
    } else {
      response.setStatus(429);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ErrorResponse error =
          new ErrorResponse(
              ErrorType.TOO_MANY_REQUESTS, "Too many requests", 429, request.getRequestURI());

      objectMapper.writeValue(response.getWriter(), error);
    }
  }
}
