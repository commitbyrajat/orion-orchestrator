package com.example.msme.exception;

import com.example.msme.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(InvalidPanException.class)
  ResponseEntity<ErrorResponse> invalidPan(InvalidPanException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorResponse> validation(ConstraintViolationException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> messages = ex.getBindingResult().getFieldErrors().stream()
        .map(this::formatFieldError)
        .toList();
    return error(HttpStatus.BAD_REQUEST, messages, request.getRequestURI());
  }

  @ExceptionHandler(InvalidRequestException.class)
  ResponseEntity<ErrorResponse> invalidRequest(InvalidRequestException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(PanNotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(PanNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(MissingDataException.class)
  ResponseEntity<ErrorResponse> missingData(MissingDataException ex, HttpServletRequest request) {
    return error(HttpStatus.UNPROCESSABLE_ENTITY, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> internal(Exception ex, HttpServletRequest request) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Unexpected server error"), request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> error(HttpStatus status, List<String> messages, String path) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), messages, path));
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + " " + error.getDefaultMessage();
  }
}
