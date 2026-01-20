package dev.ghidora.utabridgeserver.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for OpenAPI/Swagger documentation. */
@Configuration
public class OpenApiConfig {

  /**
   * Creates custom OpenAPI configuration with JWT security scheme.
   *
   * @return Configured OpenAPI instance.
   */
  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("UtaBridge API")
                .description(
                    "API for bridging language gaps in songs. Provides translation and romanization"
                        + " services for song lyrics with user authentication.")
                .version("0.1.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")));
  }
}
