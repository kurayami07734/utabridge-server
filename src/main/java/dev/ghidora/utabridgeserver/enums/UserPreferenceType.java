package dev.ghidora.utabridgeserver.enums;

import java.util.Set;

public enum UserPreferenceType {
  PRIMARY_TEXT_TYPE("ROMANIZATION", Set.of("ROMANIZATION", "TRANSLATION"));

  private final String defaultValue;
  private final Set<String> validValues;

  UserPreferenceType(String defaultValue, Set<String> validValues) {
    this.defaultValue = defaultValue;
    this.validValues = validValues;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean isValid(String value) {
    return validValues.contains(value);
  }
}
