package dev.ghidora.utabridgeserver.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Utility class for generating SHA-256 hashes of strings. */
public class Sha256HashGenerator {
  /**
   * Hashes a string using the SHA-256 algorithm and returns the result as a hex string.
   *
   * @param input The string to hash.
   * @return The hex-encoded SHA-256 hash.
   */
  public static String hashString(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(encodedHash);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not found", e);
    }
  }
}
