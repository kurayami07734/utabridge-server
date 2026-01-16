package dev.ghidora.utabridgeserver.enums;

/** Enumeration of text tags for categorizing source terms. */
public enum TextTag {
  SONG("song"),
  ALBUM("album"),
  ARTIST("artist");

  private final String value;

  TextTag(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
