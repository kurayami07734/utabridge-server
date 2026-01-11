package dev.ghidora.utabridgeserver.enums;

/** Enum representing supported identity providers. */
public enum IdentityProvider {
  /** Google identity provider. */
  GOOGLE("google"),
  /** Discord identity provider. */
  DISCORD("discord");

  private final String value;

  IdentityProvider(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Converts a string value to an IdentityProvider.
   *
   * @param text The string value.
   * @return The corresponding IdentityProvider.
   * @throws IllegalArgumentException If no provider matches the value.
   */
  public static IdentityProvider fromString(String text) throws IllegalArgumentException {
    for (var provider : IdentityProvider.values()) {
      if (text.equals(provider.getValue())) {
        return provider;
      }
    }

    throw new IllegalArgumentException("Unknown provider: " + text);
  }
}
