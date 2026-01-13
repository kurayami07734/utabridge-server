package dev.ghidora.utabridgeserver.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Sha256HashGeneratorTest {

  @Test
  void hashString_shouldReturnExpectedHashForKnownInput() {
    // Known vector: "hello world" -> SHA-256 -> Hex
    String input = "hello world";
    String expectedHash = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9";

    String result = Sha256HashGenerator.hashString(input);

    assertThat(result).isEqualTo(expectedHash);
  }

  @Test
  void hashString_shouldBeDeterministic() {
    String input = "test-consistency";
    assertThat(Sha256HashGenerator.hashString(input))
        .isEqualTo(Sha256HashGenerator.hashString(input));
  }
}
