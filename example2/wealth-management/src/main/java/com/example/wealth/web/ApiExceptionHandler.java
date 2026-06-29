package com.example.wealth.web;

import com.example.wealth.dto.ErrorResponse;
import com.example.wealth.service.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, List.of(ex.getMessage()), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    var messages = ex.getBindingResult().getFieldErrors().stream()
        .map(this::formatFieldError)
        .toList();
    return error(HttpStatus.BAD_REQUEST, messages, request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, List.of("Invalid value for parameter " + ex.getName()), request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> error(HttpStatus status, List<String> messages, String path) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), messages, path));
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + " " + error.getDefaultMessage();
  }
}
