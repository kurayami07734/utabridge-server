package dev.ghidora.utabridgeserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception for validation errors. Results in 400 Bad Request. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
