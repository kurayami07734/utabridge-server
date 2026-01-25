package dev.ghidora.utabridgeserver.enums;

/** Enumeration of error types returned by the API. */
public enum ErrorType {
  INVALID_TOKEN,
  INVALID_REFRESH_TOKEN,
  VALIDATION_ERROR,
  TOO_MANY_REQUESTS,
  RESOURCE_NOT_FOUND,
  FORBIDDEN_OPERATION,
  INTERNAL_ERROR
}
