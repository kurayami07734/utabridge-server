package dev.ghidora.utabridgeserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception for invalid refresh tokens. Results in 401 Unauthorized. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class RefreshTokenException extends RuntimeException {
  public RefreshTokenException(String message) {
    super(message);
  }
}
