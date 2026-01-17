package dev.ghidora.utabridgeserver.exceptions;

/** Exception for invalid or expired JWT tokens. Results in 401 Unauthorized. */
public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message);
  }
}
