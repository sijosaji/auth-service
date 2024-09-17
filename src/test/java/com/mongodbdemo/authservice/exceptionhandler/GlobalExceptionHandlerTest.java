package com.mongodbdemo.authservice.exceptionhandler;

import com.mongodbdemo.authservice.exception.DuplicateUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleDuplicateUserExceptionShouldReturnConflict() {
        // Arrange
        DuplicateUserException ex = new DuplicateUserException("User already exists");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDuplicateUserException(ex, request);

        // Assert
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("error", "User already exists");
        expectedBody.put("status", HttpStatus.CONFLICT);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void handleResponseStatusExceptionShouldReturnCustomError() {
        // Arrange
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleResponseStatusException(ex, request);

        // Assert
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("error", "Forbidden");
        expectedBody.put("status", HttpStatus.FORBIDDEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    public void handleValidationExceptionShouldReturnBadRequestWithErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        var fieldError = mock(org.springframework.validation.FieldError.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(fieldError.getField()).thenReturn("fieldName");
        when(fieldError.getDefaultMessage()).thenReturn("errorMessage");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("fieldName", "errorMessage"), response.getBody());
    }
}
