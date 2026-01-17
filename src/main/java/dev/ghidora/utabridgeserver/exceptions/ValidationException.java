package dev.ghidora.utabridgeserver.exceptions;

/** Exception for validation errors. Results in 400 Bad Request. */
public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
