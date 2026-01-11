package dev.ghidora.utabridge_server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ghidora.utabridge_server.enums.IdentityProvider;
import dev.ghidora.utabridge_server.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleAuth_ValidToken_ReturnsToken() throws Exception {
        // Arrange
        String tokenIn = "valid-google-token";
        String tokenOut = "generated-jwt-token";
        AuthController.AuthRequest request = new AuthController.AuthRequest(tokenIn, IdentityProvider.GOOGLE);

        given(authService.createToken(tokenIn)).willReturn(tokenOut);

        // Act & Assert
        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(tokenOut));
    }

    @Test
    void handleAuth_InvalidToken_ReturnsBadRequest() throws Exception {
        // Arrange
        String tokenIn = "invalid-token";
        AuthController.AuthRequest request = new AuthController.AuthRequest(tokenIn, IdentityProvider.GOOGLE);

        given(authService.createToken(tokenIn)).willThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
