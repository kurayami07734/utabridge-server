package dev.ghidora.utabridgeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Main application class for Utabridge Server. */
@SpringBootApplication
@EnableScheduling
public class UtabridgeServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(UtabridgeServerApplication.class, args);
  }
}
