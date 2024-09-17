package com.mongodbdemo.authservice.exceptionhandler;

import com.mongodbdemo.authservice.exception.DuplicateUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for handling exceptions thrown in the AuthController.
 * Provides methods to handle specific exceptions and return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link DuplicateUserException} thrown by the AuthController.
     *
     * @param ex the exception to be handled.
     * @param request the current web request.
     * @return a {@link ResponseEntity} containing the error message and HTTP status 409 Conflict.
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Object> handleDuplicateUserException(DuplicateUserException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("status", HttpStatus.CONFLICT);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link ResponseStatusException} thrown by the AuthController.
     *
     * @param ex the exception to be handled.
     * @param request the current web request.
     * @return a {@link ResponseEntity} containing the error message and the status code from the exception.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getReason());
        body.put("status", ex.getStatusCode());
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when a request body fails validation.
     *
     * @param ex the exception to be handled.
     * @return a {@link ResponseEntity} containing a map of field names and their corresponding error messages, with HTTP status 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
