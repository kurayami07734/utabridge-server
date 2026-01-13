package dev.ghidora.utabridgeserver.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class RefreshTokenGeneratorTest {
  private static final Pattern URL_SAFE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");

  @Test
  void generate_shouldReturnTokenWithCorrectLength() {
    String token = RefreshTokenGenerator.generate();

    // Length should be (4/3) * 32 (length of byte array
    assertThat(token).hasSize(43);
  }

  @Test
  void generate_shouldReturnUrlSafeToken() {
    String token = RefreshTokenGenerator.generate();

    assertThat(token).matches(URL_SAFE_PATTERN).doesNotContain("+", "/", "=");
  }

  @Test
  void generate_shouldNotReturnEmpty() {
    String token = RefreshTokenGenerator.generate();

    assertThat(token).isNotNull().isNotEmpty();
  }
}
