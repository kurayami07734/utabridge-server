package dev.ghidora.utabridgeserver.utilities;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter for JWT authentication. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  /**
   * Constructs a JwtAuthenticationFilter.
   *
   * @param jwtService JWT service.
   */
  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  /**
   * Extracts JWT token from Authorization header.
   *
   * @param request HTTP request.
   * @return Optional containing JWT token if present.
   */
  private Optional<String> extractJwt(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    return Optional.of(authHeader.substring(7));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var jwt = extractJwt(request);

    if (jwt.isPresent()) {
      try {
        String userId = jwtService.getSubject(jwt.get());
        var authentication =
            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception e) {
        // Invalid JWT - don't set authentication, continue filter chain
      }
    }

    doFilter(request, response, filterChain);
  }
}
