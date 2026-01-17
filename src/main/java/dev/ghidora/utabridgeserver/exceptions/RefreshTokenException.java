package dev.ghidora.utabridgeserver.exceptions;

/** Exception for invalid refresh tokens. Results in 401 Unauthorized. */
public class RefreshTokenException extends RuntimeException {
  public RefreshTokenException(String message) {
    super(message);
  }
}
