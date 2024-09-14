package com.mongodbdemo.authservice.service;

import com.mongodbdemo.authservice.dto.AuthValidationRequest;
import com.mongodbdemo.authservice.entity.UserCredential;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = Mockito.spy(new JwtService());
        jwtService.secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    }

    @Test
    void generateAccessTokenShouldReturnTokenWhenUserHasRoles() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Set.of("ROLE_USER"));

        String token = jwtService.generateAccessToken(userCredential);

        assertNotNull(token);
    }

    @Test
    void generateAccessTokenShouldReturnTokenWhenUserHasNoRoles() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Collections.emptySet());

        String token = jwtService.generateAccessToken(userCredential);

        assertNotNull(token);
    }

    @Test
    void validateTokenShouldNotThrowExceptionWhenTokenIsValidAndRolesMatch() {
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(Set.of("ROLE_USER"));
        String token = jwtService.generateAccessToken(userCredential);

        List<String> requiredRoles = List.of("ROLE_USER");

        AuthValidationRequest request = new AuthValidationRequest();
        request.setAccessToken(token);
        request.setRoles(requiredRoles);


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
        String token = jwtService.generateAccessToken(userCredential);

        Claims claims = jwtService.parseClaims(token);

        assertNotNull(claims);
        assertEquals("userId", claims.getSubject());
    }


}

