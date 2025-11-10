package dev.ghidora.utabridge_server.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    public record HealthStatus(String health) {}

    @GetMapping
    public HealthStatus getHealth() {
        return new HealthStatus("ok");
    }
}
