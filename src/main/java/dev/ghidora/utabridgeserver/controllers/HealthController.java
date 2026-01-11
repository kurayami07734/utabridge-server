package dev.ghidora.utabridgeserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for health checks. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

  /**
   * Response payload for health check.
   *
   * @param health The health status.
   */
  public record HealthStatus(String health) {}

  @GetMapping
  public HealthStatus getHealth() {
    return new HealthStatus("ok");
  }
}
