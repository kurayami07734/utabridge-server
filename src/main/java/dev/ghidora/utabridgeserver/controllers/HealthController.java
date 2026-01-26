package dev.ghidora.utabridgeserver.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for health checks. */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Endpoints for service health checks")
public class HealthController {

  private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

  /**
   * Response payload for health check.
   *
   * @param health The health status.
   */
  public record HealthStatus(String health) {}

  @Operation(
      summary = "Check service health",
      description =
          "Returns the health status of the service. Used for monitoring and load balancer health"
              + " checks.")
  @ApiResponse(
      responseCode = "200",
      description = "Service is healthy",
      content =
          @Content(
              mediaType = "application/json",
              examples = @ExampleObject(name = "Healthy", value = "{\"health\": \"ok\"}")))
  @GetMapping
  public HealthStatus getHealth() {
    logger.trace("Health check endpoint was called");
    return new HealthStatus("ok");
  }
}
