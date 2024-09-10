package com.mongodbdemo.authservice.service;

import com.mongodbdemo.authservice.dto.AuthResponse;
import com.mongodbdemo.authservice.dto.AuthValidationRequest;
import com.mongodbdemo.authservice.dto.UserCredentialCreateRequestDTO;
import com.mongodbdemo.authservice.entity.RefreshToken;
import com.mongodbdemo.authservice.entity.UserCredential;
import com.mongodbdemo.authservice.exception.DuplicateUserException;
import com.mongodbdemo.authservice.repository.RefreshTokenRepository;
import com.mongodbdemo.authservice.repository.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUserShouldSaveUserWhenUserDoesNotExist() {
        UserCredentialCreateRequestDTO userCredential = new UserCredentialCreateRequestDTO();
        userCredential.setUsername("test@example.com");
        userCredential.setPassword("password");

        when(userCredentialRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.saveUser(userCredential);

        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
    }

    @Test
    void saveUserShouldSaveUserWhenUserDoesNotExistWithRoles() {
        UserCredentialCreateRequestDTO userCredential = new UserCredentialCreateRequestDTO();
        userCredential.setUsername("test@example.com");
        userCredential.setPassword("password");
        userCredential.setRoles(Set.of("ROLE"));

        when(userCredentialRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.saveUser(userCredential);

        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
    }


    @Test
    void saveUserShouldThrowDuplicateUserExceptionWhenUserAlreadyExists() {
        UserCredentialCreateRequestDTO userCredential = new UserCredentialCreateRequestDTO();
        userCredential.setUsername("test@example.com");

        when(userCredentialRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(new UserCredential()));

        assertThrows(DuplicateUserException.class, () -> authService.saveUser(userCredential));
        verify(userCredentialRepository, times(0)).save(any(UserCredential.class));
    }

    @Test
    void generateTokenShouldReturnAuthResponseForExistingUser() {
        UserCredential userCredential = new UserCredential();
        userCredential.setUsername("test@example.com");
        userCredential.setRoles(new HashSet<>());

        when(userCredentialRepository.findByUsername(anyString())).thenReturn(Optional.of(userCredential));
        when(jwtService.generateAccessToken(any(UserCredential.class))).thenReturn("accessToken");

        AuthResponse response = authService.generateToken("test@example.com");

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
    }

    @Test
    void generateTokenShouldThrowResponseStatusExceptionWhenUserNotFound() {
        when(userCredentialRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.generateToken("nonexistent@example.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void validateShouldCallJwtServiceValidateToken() {
        AuthValidationRequest request = new AuthValidationRequest();
        request.setAccessToken("token");
        request.setRoles(List.of());

        authService.validate(request);

        verify(jwtService, times(1)).validateToken(request);
    }

    @Test
    void getNewAccessTokenShouldReturnAuthResponseWhenRefreshTokenIsValid() {
        RefreshToken refreshToken = new RefreshToken("refreshToken", "userId", Instant.now().plusSeconds(1000));
        UserCredential userCredential = new UserCredential();
        userCredential.setId("userId");
        userCredential.setRoles(new HashSet<>());

        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.of(refreshToken));
        when(userCredentialRepository.findById(anyString())).thenReturn(Optional.of(userCredential));
        when(jwtService.generateAccessToken(any(UserCredential.class))).thenReturn("newAccessToken");

        AuthResponse response = authService.getNewAccessToken("refreshToken");

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
    }

    @Test
    void getNewAccessTokenShouldThrowResponseStatusExceptionWhenRefreshTokenIsExpired() {
        RefreshToken refreshToken = new RefreshToken("refreshToken", "userId", Instant.now().minusSeconds(1));

        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.of(refreshToken));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.getNewAccessToken("refreshToken");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Refresh token expired", exception.getReason());
    }

    @Test
    void getNewAccessTokenShouldThrowResponseStatusExceptionWhenRefreshTokenIsInvalid() {
        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.getNewAccessToken("invalidRefreshToken");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid refresh token", exception.getReason());
    }

    @Test
    void getNewAccessTokenShouldThrowResponseStatusExceptionWhenUserNotFound() {
        RefreshToken refreshToken = new RefreshToken("refreshToken", "userId", Instant.now().plusSeconds(1000));

        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.of(refreshToken));
        when(userCredentialRepository.findById(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.getNewAccessToken("refreshToken");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }









}
