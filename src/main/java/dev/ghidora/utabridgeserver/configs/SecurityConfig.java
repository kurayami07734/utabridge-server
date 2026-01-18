package dev.ghidora.utabridgeserver.configs;

import dev.ghidora.utabridgeserver.utilities.JwtAuthenticationFilter;
import dev.ghidora.utabridgeserver.utilities.RateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RateLimitFilter rateLimitFilter;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.rateLimitFilter = rateLimitFilter;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configure(http))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      response.setContentType("application/json");
                      response
                          .getWriter()
                          .write(
                              "{\"error\":\"UNAUTHORIZED\",\"message\":\"Authentication"
                                  + " required\"}");
                    }))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/login", "/api/health", "/api/auth/refresh")
                    .permitAll()
                    .requestMatchers("/api/dev/**")
                    .permitAll()
                    .requestMatchers("/api/docs.html", "/v3/api-docs/**", "/api/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

    return http.build();
  }
}
