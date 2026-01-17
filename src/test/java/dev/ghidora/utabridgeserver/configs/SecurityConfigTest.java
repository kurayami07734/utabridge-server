package dev.ghidora.utabridgeserver.configs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ghidora.utabridgeserver.utilities.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtService jwtService;

  @Test
  void health_IsPublic_Returns200() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk());
  }

  @Test
  void swaggerJson_IsPublic_Returns200() throws Exception {
    MvcResult result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
    String content = result.getResponse().getContentAsString();
    org.junit.jupiter.api.Assertions.assertTrue(content.contains("openapi"));
  }

  @Test
  void localize_WithoutJwt_Returns401() throws Exception {
    String validJson = "{\"text\":\"Hello\",\"fromLanguage\":\"en\",\"toLanguage\":\"ja\"}";

    mockMvc
        .perform(post("/api/localize").contentType(MediaType.APPLICATION_JSON).content(validJson))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void localize_WithInvalidJwt_Returns401() throws Exception {
    String invalidJwt = "invalid.jwt.token";
    String validJson = "{\"text\":\"Hello\",\"fromLanguage\":\"en\",\"toLanguage\":\"ja\"}";

    mockMvc
        .perform(
            post("/api/localize")
                .header("Authorization", "Bearer " + invalidJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
        .andExpect(status().isUnauthorized());
  }
}
