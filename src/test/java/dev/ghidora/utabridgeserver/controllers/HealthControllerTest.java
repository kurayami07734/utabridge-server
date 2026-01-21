package dev.ghidora.utabridgeserver.controllers;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ghidora.utabridgeserver.repositories.UserRepository;
import dev.ghidora.utabridgeserver.utilities.JwtService;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
class HealthControllerTestConfig {
  @Bean
  JwtService jwtService() {
    return mock(JwtService.class);
  }

  @Bean
  RateLimiterRegistry rateLimiterRegistry() {
    return mock(RateLimiterRegistry.class);
  }

  @Bean
  UserRepository userRepository() {
    return mock(UserRepository.class);
  }
}

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(HealthControllerTestConfig.class)
class HealthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void getHealth_ReturnsOkStatus() throws Exception {
    mockMvc
        .perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.health").value("ok"));
  }

  @Test
  void getHealth_ReturnsCorrectResponseFormat() throws Exception {
    mockMvc
        .perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty())
        .andExpect(jsonPath("$.health").isNotEmpty());
  }
}
