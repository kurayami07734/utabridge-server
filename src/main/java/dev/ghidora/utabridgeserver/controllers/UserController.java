package dev.ghidora.utabridgeserver.controllers;

import dev.ghidora.utabridgeserver.exceptions.ForbiddenOperationException;
import dev.ghidora.utabridgeserver.exceptions.ValidationException;
import dev.ghidora.utabridgeserver.models.User;
import dev.ghidora.utabridgeserver.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for user-related operations. */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for interacting with user records")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {
  private final UserService userService;
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Represents the request body for updating user preferences.
   *
   * @param preferences A map of user preferences to update.
   */
  public record UpdateUser(
      @Schema(
              description = "Configures user preferences",
              example = "{\"PRIMARY_TEXT_TYPE\": \"ROMANIZATION\"}")
          @NotNull
          Map<String, String> preferences) {}

  /**
   * Updates a user's preferences.
   *
   * @param authUserId The ID of the authenticated user.
   * @param userId The ID of the user to update.
   * @param body The request body containing the updated preferences.
   * @return The updated user object.
   */
  @Operation(summary = "Update user settings", description = "Modify authorized user preferences")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User preferences updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "User Updated",
                            value =
                                "{\"preferences\": {\"PRIMARY_TEXT_TYPE\": \"ROMANIZATION\"}}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Validation Error",
                            value =
                                "{\"error\": \"VALIDATION_ERROR\", \"message\": \"preferences:"
                                    + " must not be null\", \"status\": 400}"))),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or invalid JWT token",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Unauthorized",
                            value =
                                "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication"
                                    + " required\", \"status\": 401}"))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden operation",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Forbidden",
                            value =
                                "{\"error\": \"FORBIDDEN_OPERATION\", \"message\": \"Only allowed"
                                    + " to update preference for self\", \"status\": 403}")))
      })
  @PatchMapping("/{userId}")
  public ResponseEntity<User> updateUser(
      @AuthenticationPrincipal String authUserId,
      @PathVariable("userId") Long userId,
      @Valid @RequestBody UpdateUser body) {
    logger.debug(
        "User {} attempting to update preferences for user {} with body: {}",
        authUserId,
        userId,
        body);
    if (!Long.valueOf(authUserId).equals(userId)) {
      throw new ForbiddenOperationException("Only allowed to update preference for self");
    }
    try {
      var user = userService.updatePreferences(userId, body.preferences);
      logger.info("User {} preferences updated successfully", userId);
      return ResponseEntity.ok(user);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(e.getMessage());
    }
  }
}
