package dev.ghidora.utabridgeserver.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserPreferenceTypeTest {

  @Test
  void getDefaultValue_PrimaryTextType_ReturnsRomanization() {
    assertEquals("ROMANIZATION", UserPreferenceType.PRIMARY_TEXT_TYPE.getDefaultValue());
  }

  @Test
  void isValid_ValidValue_ReturnsTrue() {
    assertTrue(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid("ROMANIZATION"));
    assertTrue(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid("TRANSLATION"));
  }

  @Test
  void isValid_InvalidValue_ReturnsFalse() {
    assertFalse(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid("INVALID_VALUE"));
    assertFalse(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid("romanization")); // case sensitive
    assertFalse(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid("translation")); // case sensitive
    assertFalse(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid(""));
    assertFalse(UserPreferenceType.PRIMARY_TEXT_TYPE.isValid(null));
  }

  @Test
  void values_ContainsAllExpectedPreferences() {
    UserPreferenceType[] preferences = UserPreferenceType.values();
    assertEquals(1, preferences.length);
    assertEquals(UserPreferenceType.PRIMARY_TEXT_TYPE, preferences[0]);
  }

  @Test
  void valueOf_ValidPreferenceName_ReturnsCorrectEnum() {
    assertEquals(
        UserPreferenceType.PRIMARY_TEXT_TYPE, UserPreferenceType.valueOf("PRIMARY_TEXT_TYPE"));
  }

  @Test
  void valueOf_InvalidPreferenceName_ThrowsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class, () -> UserPreferenceType.valueOf("INVALID_PREFERENCE"));
  }

  @Test
  void valueOf_CaseSensitive_ThrowsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class, () -> UserPreferenceType.valueOf("primary_text_type"));
  }

  @Test
  void enumProperties_AreCorrectlySet() {
    UserPreferenceType preference = UserPreferenceType.PRIMARY_TEXT_TYPE;
    assertEquals("ROMANIZATION", preference.getDefaultValue());
    assertTrue(preference.isValid("ROMANIZATION"));
    assertTrue(preference.isValid("TRANSLATION"));
    assertFalse(preference.isValid("OTHER"));
  }
}
