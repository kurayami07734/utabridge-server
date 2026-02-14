package dev.ghidora.utabridgeserver.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ghidora.utabridgeserver.enums.UserPreferenceType;
import dev.ghidora.utabridgeserver.exceptions.GlobalExceptionHandler;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.services.UserService;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  private RequestPostProcessor withUser(String userId) {
    return request -> {
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(auth);
      request.setUserPrincipal(auth);
      return request;
    };
  }

  @Test
  void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
    Long userId = 1L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");
    Map<UserPreferenceType, String> userPreferences = new EnumMap<>(UserPreferenceType.class);
    userPreferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    User updatedUser = new User();
    ReflectionTestUtils.setField(updatedUser, "id", userId);
    updatedUser.setName("Test User");
    updatedUser.setPictureUrl("https://example.com/avatar.jpg");
    updatedUser.setEmail("test@example.com");
    updatedUser.setPreferences(userPreferences);

    given(userService.updatePreferences(eq(userId), eq(preferences))).willReturn(updatedUser);

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("preferences", preferences))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.pictureUrl").value("https://example.com/avatar.jpg"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.preferences.PRIMARY_TEXT_TYPE").value("TRANSLATION"));
  }

  @Test
  void updateUser_UnauthorizedUser_ReturnsForbidden() throws Exception {
    Long authUserId = 1L;
    Long targetUserId = 2L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");

    mockMvc
        .perform(
            patch("/api/users/{userId}", targetUserId)
                .with(withUser(authUserId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("preferences", preferences))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("FORBIDDEN_OPERATION"))
        .andExpect(jsonPath("$.message").value("Only allowed to update preference for self"));
  }

  @Test
  void updateUser_InvalidPreferenceValue_ReturnsBadRequest() throws Exception {
    Long userId = 1L;
    Map<String, String> invalidPreferences = Map.of("PRIMARY_TEXT_TYPE", "INVALID_VALUE");

    given(userService.updatePreferences(eq(userId), eq(invalidPreferences)))
        .willThrow(new IllegalArgumentException("Invalid preference value: INVALID_VALUE"));

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("preferences", invalidPreferences))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Invalid preference value: INVALID_VALUE"));
  }

  @Test
  void updateUser_UserNotFound_ReturnsInternalServerError() throws Exception {
    Long userId = 999L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");

    given(userService.updatePreferences(eq(userId), eq(preferences)))
        .willThrow(new RuntimeException("User not found"));

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("preferences", preferences))))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }

  @Test
  void updateUser_NullPreferences_ReturnsBadRequest() throws Exception {
    Long userId = 1L;

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void updateUser_EmptyPreferences_ReturnsBadRequest() throws Exception {
    Long userId = 1L;
    Map<String, String> emptyPreferences = Map.of();

    given(userService.updatePreferences(eq(userId), eq(emptyPreferences)))
        .willThrow(new IllegalArgumentException("Invalid preferences"));

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("preferences", emptyPreferences))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Invalid preferences"));
  }

  @Test
  void updateUser_MultiplePreferences_ReturnsUpdatedUser() throws Exception {
    Long userId = 1L;
    Map<String, String> preferences = Map.of("PRIMARY_TEXT_TYPE", "TRANSLATION");
    Map<UserPreferenceType, String> userPreferences = new EnumMap<>(UserPreferenceType.class);
    userPreferences.put(UserPreferenceType.PRIMARY_TEXT_TYPE, "TRANSLATION");
    User updatedUser = new User();
    ReflectionTestUtils.setField(updatedUser, "id", userId);
    updatedUser.setName("Test User");
    updatedUser.setPictureUrl("https://example.com/avatar.jpg");
    updatedUser.setEmail("test@example.com");
    updatedUser.setPreferences(userPreferences);

    given(userService.updatePreferences(eq(userId), eq(preferences))).willReturn(updatedUser);

    mockMvc
        .perform(
            patch("/api/users/{userId}", userId)
                .with(withUser(userId.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("preferences", preferences))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.pictureUrl").value("https://example.com/avatar.jpg"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.preferences.PRIMARY_TEXT_TYPE").value("TRANSLATION"));
  }
}
