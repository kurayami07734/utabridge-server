package dev.ghidora.utabridgeserver.exceptions;

import dev.ghidora.utabridgeserver.dtos.ErrorResponse;
import dev.ghidora.utabridgeserver.enums.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Global exception handler for the application. */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles InvalidTokenException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidToken(
      InvalidTokenException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.INVALID_TOKEN,
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles RefreshTokenException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleRefreshToken(
      RefreshTokenException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.INVALID_REFRESH_TOKEN,
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles ValidationException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      ValidationException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.VALIDATION_ERROR,
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles ResourceNotFoundException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.RESOURCE_NOT_FOUND,
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles ForbiddenOperationException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenOperation(
      ForbiddenOperationException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.FORBIDDEN_OPERATION,
            ex.getMessage(),
            HttpStatus.FORBIDDEN.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handles IllegalArgumentException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.VALIDATION_ERROR,
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles MethodArgumentNotValidException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.VALIDATION_ERROR,
            message,
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles GeneralSecurityException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(GeneralSecurityException.class)
  public ResponseEntity<ErrorResponse> handleGeneralSecurity(
      GeneralSecurityException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.INVALID_TOKEN,
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles IOException.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<ErrorResponse> handleIoException(
      IOException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.INTERNAL_ERROR,
            "An internal error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Handles generic Exception.
   *
   * @param ex The exception.
   * @param request The HTTP request.
   * @return Error response.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            ErrorType.INTERNAL_ERROR,
            "An internal error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
