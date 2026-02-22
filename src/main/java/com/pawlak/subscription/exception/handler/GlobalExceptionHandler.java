package com.pawlak.subscription.exception.handler;

import com.pawlak.subscription.exception.base.BusinessException;
import com.pawlak.subscription.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException exception) {
        return buildError(exception.getMessage(), exception.getHttpStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException exception) {
        return buildError(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<?>> handleDisabledException(DisabledException exception) {
        return buildError(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> Objects.requireNonNull(e.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return buildError(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidEnumValue(InvalidFormatException exception) {
        String message = "Invalid value provided";
        if (exception.getTargetType().isEnum() && !exception.getPath().isEmpty()) {
            String fieldName = exception.getPath().getFirst().getFieldName();
            message = String.format("Invalid value '%s' for field '%s'", exception.getValue(), fieldName);
        }
        return buildError(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.error("Data integrity violation", exception);
        return buildError("Duplicate entry error", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);
        return buildError("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<?>> buildError(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}