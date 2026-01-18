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

@ControllerAdvice
public class GlobalExceptionHandler {

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
