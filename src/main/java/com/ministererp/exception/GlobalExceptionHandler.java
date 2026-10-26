package com.ministererp.exception;

import com.ministererp.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
          .getAllErrors()
          .forEach(error -> {
              String fieldName =
                      ((FieldError) error).getField();
              String errorMessage =
                      error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
          });

        ApiResponse<Map<String, String>> response =
                ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .errorCode("VALIDATION_ERROR")
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ── Handle Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        "NOT_FOUND"));
    }

    // ── Handle Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleUnauthorized(
            UnauthorizedException ex,
            WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        ex.getErrorCode()));
    }

    // ── Handle Access Denied (Wrong Role)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        "You do not have permission " +
                        "to access this resource",
                        "ACCESS_DENIED"));
    }

    // ── Handle Bad Credentials (Wrong Password)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Invalid username or password",
                        "BAD_CREDENTIALS"));
    }

    // ── Handle Disabled Account
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleDisabledAccount(DisabledException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Your account has been disabled. " +
                        "Please contact administrator",
                        "ACCOUNT_DISABLED"));
    }

    // ── Handle Authentication Exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleAuthenticationException(
            AuthenticationException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Authentication failed: " +
                        ex.getMessage(),
                        "AUTH_FAILED"));
    }

    // ── Handle Illegal Argument
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        "BAD_REQUEST"));
    }

    // ── Handle All Other Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>>
    handleGlobalException(
            Exception ex,
            WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. " +
                        "Please try again later",
                        "INTERNAL_ERROR"));
    }
}