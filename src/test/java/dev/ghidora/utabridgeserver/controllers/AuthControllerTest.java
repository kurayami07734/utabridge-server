package dev.ghidora.utabridgeserver.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ghidora.utabridgeserver.dtos.Credentials;
import dev.ghidora.utabridgeserver.exceptions.GlobalExceptionHandler;
import dev.ghidora.utabridgeserver.services.AuthService;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerTest {

  private MockMvc mockMvc;
  private AuthService authService;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    AuthController authController = new AuthController(authService);
    objectMapper = new ObjectMapper();
    mockMvc =
        standaloneSetup(authController).setControllerAdvice(new GlobalExceptionHandler()).build();
  }

  @Test
  void handleLogin_ValidToken_ReturnsCredentials() throws Exception {
    // Arrange
    String tokenIn = "valid-google-token";
    String authToken = "generated-jwt-token";
    String refreshToken = "refresh-token";
    AuthController.LoginRequest request = new AuthController.LoginRequest(tokenIn);

    given(authService.getLoginCredentials(tokenIn))
        .willReturn(new Credentials(authToken, refreshToken));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authToken").value(authToken))
        .andExpect(jsonPath("$.refreshToken").value(refreshToken));
  }

  @Test
  void handleLogin_InvalidToken_ReturnsUnauthorized() throws Exception {
    // Arrange
    String tokenIn = "invalid-token";
    AuthController.LoginRequest request = new AuthController.LoginRequest(tokenIn);

    given(authService.getLoginCredentials(tokenIn))
        .willThrow(new GeneralSecurityException("Invalid token"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void handleRefreshToken_ValidToken_ReturnsNewCredentials() throws Exception {
    // Arrange
    String refreshTokenIn = "valid-refresh-token";
    String newAuthToken = "new-jwt-token";
    String newRefreshToken = "new-refresh-token";
    AuthController.RefreshRequest request = new AuthController.RefreshRequest(refreshTokenIn);

    given(authService.refreshCredentials(refreshTokenIn))
        .willReturn(new Credentials(newAuthToken, newRefreshToken));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authToken").value(newAuthToken))
        .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));
  }

  @Test
  void handleRefreshToken_InvalidToken_ReturnsUnauthorized() throws Exception {
    // Arrange
    String refreshTokenIn = "invalid-refresh-token";
    AuthController.RefreshRequest request = new AuthController.RefreshRequest(refreshTokenIn);

    given(authService.refreshCredentials(refreshTokenIn))
        .willThrow(new GeneralSecurityException("Invalid token"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
