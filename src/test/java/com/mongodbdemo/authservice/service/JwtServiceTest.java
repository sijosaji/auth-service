package com.mongodbdemo.authservice.service;

import com.mongodbdemo.authservice.dto.AuthValidationRequest;
import com.mongodbdemo.authservice.entity.UserCredential;
import com.mongodbdemo.authservice.repository.UserCredentialRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;  // This will inject mocks into the service

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = Mockito.spy(jwtService);
        jwtService.secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    }

    @Test
    void generateAccessTokenShouldReturnTokenWhenUserHasRoles() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Set.of("ROLE_USER"));
        userCredential.setUsername("test");
        String token = jwtService.generateAccessToken(userCredential);

        assertNotNull(token);
    }

    @Test
    void generateAccessTokenShouldReturnTokenWhenUserHasNoRoles() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Collections.emptySet());
        userCredential.setUsername("test");

        String token = jwtService.generateAccessToken(userCredential);

        assertNotNull(token);
    }

    @Test
    void validateTokenShouldNotThrowExceptionWhenTokenIsValidAndRolesMatch() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Set.of("ROLE_USER"));
        userCredential.setUsername("test");
        String token = jwtService.generateAccessToken(userCredential);

        List<String> requiredRoles = List.of("ROLE_USER");

        AuthValidationRequest request = new AuthValidationRequest();
        request.setAccessToken(token);
        request.setRoles(requiredRoles);

        when(userCredentialRepository.findById(anyString()))
                .thenReturn(Optional.of(userCredential));
        assertDoesNotThrow(() -> jwtService.validateToken(request));
    }

    @Test
    void validateTokenShouldThrowUnauthorizedWhenTokenIsExpired() {
        // Arrange
        String token = "expiredToken"; // Use a real token if possible
        AuthValidationRequest request = new AuthValidationRequest();
        request.setAccessToken(token);
        request.setRoles(List.of("ROLE_USER"));

        // Mocking the behavior of parseClaims to throw ExpiredJwtException
        doThrow(new ExpiredJwtException(null, null, "Token expired", null))
                .when(jwtService).parseClaims(token);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jwtService.validateToken(request);
        });

        assertEquals("Invalid or expired token", exception.getReason());
    }

    @Test
    void validateTokenShouldThrowUnauthorizedWhenTokenSignatureIsInvalid() {
        // Arrange
        String token = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlcyI6WyJBRE1JTiJdLCJzdWIiOiI2NmQ0ODEwN2ZiYjA3YjBkNmJjMzA0ZmIiLCJpYXQiOjE3MjUyMTY1MjgsImV4cCI6MTcyNTIxODMyOH0.h8ue7CULWtOtO31MhajcJEwyjQRAv1SCdDK7e_GQ4fykmvo3B8kNhcuYYzXo4SAa"; // Use a real token if possible
        AuthValidationRequest request = new AuthValidationRequest();
        request.setAccessToken(token);
        request.setRoles(List.of("ROLE_USER"));


        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jwtService.validateToken(request);
        });

        assertEquals("Invalid or expired token", exception.getReason());
    }

    @Test
    void validateRolesShouldThrowForbiddenWhenRequiredRolesAreNotPresent() {
        Claims claims = mock(Claims.class);
        when(claims.get("roles", List.class)).thenReturn(List.of("ROLE_ADMIN"));

        List<String> requiredRoles = List.of("ROLE_USER");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jwtService.validateRoles(claims, requiredRoles);
        });

        assertEquals("Insufficient roles", exception.getReason());
    }

    @Test
    void validateRolesShouldNotThrowExceptionWhenRequiredRolesArePresent() {
        Claims claims = mock(Claims.class);
        when(claims.get("roles", List.class)).thenReturn(List.of("ROLE_USER"));

        List<String> requiredRoles = List.of("ROLE_USER");

        assertDoesNotThrow(() -> jwtService.validateRoles(claims, requiredRoles));
    }

    @Test
    void parseClaimsShouldReturnClaimsWhenTokenIsValid() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setUsername("test");
        String token = jwtService.generateAccessToken(userCredential);

        Claims claims = jwtService.parseClaims(token);

        assertNotNull(claims);
        assertEquals("userId", claims.getSubject());
    }

    @Test
    void validateUserDetailsShouldThrowUnauthorizedWhenUserIsNotPresent() {
        // Arrange
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("userId");

        // Mock repository to return empty Optional
        when(userCredentialRepository.findById("userId")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jwtService.validateUserDetails(claims);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Provided User is not present", exception.getReason());
    }

    @Test
    void validateUserDetailsShouldThrowUnauthorizedWhenUsernameMismatch() {
        // Arrange
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("userId");
        when(claims.get("username")).thenReturn("wrongUsername");

        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setUsername("correctUsername");

        // Mock repository to return the user with the correct username
        when(userCredentialRepository.findById("userId")).thenReturn(Optional.of(userCredential));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jwtService.validateUserDetails(claims);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid User details provided", exception.getReason());
    }


}